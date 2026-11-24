package au.edu.jcu.zhangfan.primoria.data.repository

import androidx.room.withTransaction
import au.edu.jcu.zhangfan.primoria.data.local.AttemptEntity
import au.edu.jcu.zhangfan.primoria.data.local.ContentReportEntity
import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.local.QuestionEntity
import au.edu.jcu.zhangfan.primoria.data.local.TopicMasteryEntity
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.ParsedQuiz
import au.edu.jcu.zhangfan.primoria.data.remote.QuizRemoteDataSource
import au.edu.jcu.zhangfan.primoria.domain.logic.MasteryCalculator
import au.edu.jcu.zhangfan.primoria.domain.logic.OptionShuffler
import au.edu.jcu.zhangfan.primoria.domain.logic.PersonalDataRedactor
import au.edu.jcu.zhangfan.primoria.domain.logic.TopicNormalizer
import au.edu.jcu.zhangfan.primoria.domain.logic.XpPolicy
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface QuizLoadResult {
    data class Ready(
        val topicId: Long,
        val topicTitle: String,
        val questions: List<QuizQuestion>,
        /** Shown when Primoria had to fall back to saved questions. */
        val notice: String? = null,
        val fromStudyMaterial: Boolean = false,
    ) : QuizLoadResult

    /** DeepSeek declined the topic as unsuitable; the reason is shown to the learner. */
    data class Refused(val reason: String) : QuizLoadResult
    data class Unavailable(val message: String) : QuizLoadResult
    data object InvalidTopic : QuizLoadResult
}

data class AnswerResult(
    val correct: Boolean,
    val xpAwarded: Int,
    val mastery: Double,
    /** All answers ever given for this topic, used to judge how much evidence the mastery has. */
    val topicAttempts: Int,
    val dailyCapReached: Boolean,
)

interface LearningRepository {
    fun observeTopics(): Flow<List<TopicSummary>>
    fun libraryTitles(): List<String>
    suspend fun ensureLibrarySeeded()
    suspend fun loadQuiz(rawTopic: String, settings: UserSettings): QuizLoadResult
    suspend fun loadSavedQuiz(topicId: Long, rawTopic: String, settings: UserSettings): QuizLoadResult
    suspend fun createMaterialQuiz(title: String, excerpt: String, settings: UserSettings): QuizLoadResult
    suspend fun submitAnswer(question: QuizQuestion, chosenIndex: Int, sessionId: String): AnswerResult
    suspend fun reportQuestion(questionId: Long, reason: String)
}

@Singleton
class DefaultLearningRepository @Inject constructor(
    private val database: PrimoriaDatabase,
    private val remote: QuizRemoteDataSource,
    private val keyProvider: ApiKeyProvider,
    private val library: OfflineLibrary,
    private val clock: Clock,
) : LearningRepository {

    private val topicDao = database.topicDao()
    private val questionDao = database.questionDao()
    private val attemptDao = database.attemptDao()

    override fun observeTopics(): Flow<List<TopicSummary>> =
        topicDao.observeTopicsWithMastery().map { rows ->
            rows.map { row ->
                TopicSummary(
                    id = row.id,
                    title = row.title,
                    mastery = row.mastery ?: 0.0,
                    attempts = row.attempts ?: 0,
                    correct = row.correct ?: 0,
                    lastStudiedAt = row.lastStudiedAt,
                    isStudyMaterial = row.titleKey.startsWith(MATERIAL_KEY_PREFIX),
                )
            }
        }

    override fun libraryTitles(): List<String> = library.topics().map { it.title }

    /** Inserts the bundled library once; safe to call on every launch. */
    override suspend fun ensureLibrarySeeded() {
        if (questionDao.count() > 0) return
        val now = clock.millis()
        database.withTransaction {
            library.topics().forEach { libraryTopic ->
                val topic = topicDao.getOrCreate(libraryTopic.title, TopicNormalizer.key(libraryTopic.title), now)
                questionDao.insertAll(
                    libraryTopic.questions.map {
                        OptionShuffler.shuffle(it).toEntity(topic.id, Difficulty.STANDARD, ContentSource.OFFLINE_LIBRARY, now)
                    },
                )
            }
        }
    }

    override suspend fun loadQuiz(rawTopic: String, settings: UserSettings): QuizLoadResult {
        val title = TopicNormalizer.clean(rawTopic) ?: return QuizLoadResult.InvalidTopic
        val topic = topicDao.getOrCreate(title, TopicNormalizer.key(title), clock.millis())
        val count = settings.questionsPerQuiz

        var notice: String? = null
        if (settings.aiConsent && keyProvider.currentKey() != null) {
            try {
                when (val parsed = remote.generate(topic.title, settings.difficulty, count, settings.ageGroup)) {
                    is ParsedQuiz.Questions -> {
                        val entities = parsed.drafts.map {
                            it.toEntity(topic.id, settings.difficulty, ContentSource.AI_GENERATED, clock.millis())
                        }
                        val ids = questionDao.insertAll(entities)
                        val questions = entities.zip(ids) { entity, id -> entity.copy(id = id).toDomain() }
                        return QuizLoadResult.Ready(topic.id, topic.title, questions)
                    }
                    is ParsedQuiz.Refused -> return QuizLoadResult.Refused(parsed.reason)
                    ParsedQuiz.Invalid -> notice = "DeepSeek's answer didn't pass our quality checks, so you're practising saved questions."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                notice = "Couldn't reach DeepSeek, so you're practising saved questions."
            }
        }

        val saved = questionDao.practiceSet(topic.id, settings.difficulty, count)
            .ifEmpty { questionDao.anyForTopic(topic.id, count) }
        if (saved.isNotEmpty()) {
            return QuizLoadResult.Ready(topic.id, topic.title, saved.map { it.toDomain() }, notice)
        }

        val message = if (!settings.aiConsent) {
            "AI questions are turned off. Pick a topic from the offline library, or turn on AI in Settings."
        } else if (keyProvider.currentKey() == null) {
            "No DeepSeek key is set up. Pick a library topic, or add a key in Settings."
        } else {
            "Couldn't reach DeepSeek and there are no saved questions for \"${topic.title}\" yet. Check your connection and try again."
        }
        return QuizLoadResult.Unavailable(message)
    }

    override suspend fun loadSavedQuiz(topicId: Long, rawTopic: String, settings: UserSettings): QuizLoadResult {
        val topic = topicDao.findById(topicId)
            ?: return QuizLoadResult.Unavailable("This topic is no longer saved on this device.")
        if (!topic.titleKey.startsWith(MATERIAL_KEY_PREFIX)) return loadQuiz(rawTopic, settings)
        val saved = questionDao.practiceSet(topic.id, settings.difficulty, settings.questionsPerQuiz)
            .ifEmpty { questionDao.anyForTopic(topic.id, settings.questionsPerQuiz) }
        return if (saved.isEmpty()) {
            QuizLoadResult.Unavailable("There are no available saved questions for this study material.")
        } else {
            QuizLoadResult.Ready(topic.id, topic.title, saved.map { it.toDomain() }, fromStudyMaterial = true)
        }
    }

    override suspend fun createMaterialQuiz(title: String, excerpt: String, settings: UserSettings): QuizLoadResult {
        if (!settings.aiConsent) return QuizLoadResult.Unavailable("Turn on AI in Settings to generate a quiz from study material.")
        if (keyProvider.currentKey() == null) return QuizLoadResult.Unavailable("Add a DeepSeek key in Settings and try again.")
        val source = PersonalDataRedactor.redact(excerpt.take(MAX_MATERIAL_CHARS + 1)).take(MAX_MATERIAL_CHARS).trim()
        if (source.isBlank()) return QuizLoadResult.InvalidTopic
        val parsed = try {
            remote.generateFromMaterial(source, settings.difficulty, settings.questionsPerQuiz, settings.ageGroup)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return QuizLoadResult.Unavailable("Couldn't reach DeepSeek. Check your connection and try again.")
        }
        return when (parsed) {
            is ParsedQuiz.Refused -> QuizLoadResult.Refused(parsed.reason)
            ParsedQuiz.Invalid -> QuizLoadResult.Unavailable("DeepSeek didn't return usable questions. Try a different excerpt.")
            is ParsedQuiz.Questions -> database.withTransaction {
                val now = clock.millis()
                val label = title.trim().take(80).ifBlank { "Study material" }
                val topic = topicDao.getOrCreate(label, "$MATERIAL_KEY_PREFIX${UUID.randomUUID()}", now)
                val entities = parsed.drafts.map {
                    it.toEntity(topic.id, settings.difficulty, ContentSource.AI_GENERATED, now)
                }
                val ids = questionDao.insertAll(entities)
                QuizLoadResult.Ready(topic.id, topic.title, entities.zip(ids) { entity, id -> entity.copy(id = id).toDomain() },
                    fromStudyMaterial = true)
            }
        }
    }

    override suspend fun submitAnswer(question: QuizQuestion, chosenIndex: Int, sessionId: String): AnswerResult =
        database.withTransaction {
            val now = clock.millis()
            val today = LocalDate.now(clock).toEpochDay()
            val correct = chosenIndex == question.correctIndex
            val firstAttempt = attemptDao.countForQuestion(question.id) == 0
            val xpToday = attemptDao.xpOnDay(today)
            val xp = XpPolicy.award(correct, firstAttempt, question.difficulty, xpToday)

            val previous = attemptDao.mastery(question.topicId)
            val mastery = MasteryCalculator.update(previous?.mastery ?: 0.0, correct, question.difficulty)

            attemptDao.insert(
                AttemptEntity(
                    questionId = question.id,
                    topicId = question.topicId,
                    sessionId = sessionId,
                    chosenIndex = chosenIndex,
                    isCorrect = correct,
                    xpAwarded = xp,
                    answeredAt = now,
                    epochDay = today,
                ),
            )
            attemptDao.upsertMastery(
                TopicMasteryEntity(
                    topicId = question.topicId,
                    mastery = mastery,
                    attempts = (previous?.attempts ?: 0) + 1,
                    correct = (previous?.correct ?: 0) + if (correct) 1 else 0,
                    updatedAt = now,
                ),
            )
            topicDao.markStudied(question.topicId, now)

            AnswerResult(
                correct = correct,
                xpAwarded = xp,
                mastery = mastery,
                topicAttempts = (previous?.attempts ?: 0) + 1,
                dailyCapReached = xpToday + xp >= XpPolicy.DAILY_CAP,
            )
        }

    override suspend fun reportQuestion(questionId: Long, reason: String) {
        database.reportDao().insert(
            ContentReportEntity(questionId = questionId, reason = reason.take(200), createdAt = clock.millis()),
        )
    }

    private companion object {
        const val MATERIAL_KEY_PREFIX = "\u0001material:"
    }
}

internal const val MAX_MATERIAL_CHARS = 12_000

private fun QuestionDraft.toEntity(
    topicId: Long,
    difficulty: Difficulty,
    source: ContentSource,
    now: Long,
) = QuestionEntity(
    topicId = topicId,
    prompt = prompt,
    options = options,
    correctIndex = correctIndex,
    explanation = explanation,
    difficulty = difficulty,
    source = source,
    createdAt = now,
)

internal fun QuestionEntity.toDomain() = QuizQuestion(
    id = id,
    topicId = topicId,
    prompt = prompt,
    options = options,
    correctIndex = correctIndex,
    explanation = explanation,
    difficulty = difficulty,
    source = source,
)
