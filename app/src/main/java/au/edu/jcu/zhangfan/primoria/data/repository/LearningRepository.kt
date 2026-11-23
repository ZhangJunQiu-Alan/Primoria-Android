package au.edu.jcu.zhangfan.primoria.data.repository

import androidx.room.withTransaction
import au.edu.jcu.zhangfan.primoria.data.local.AttemptEntity
import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.local.QuestionEntity
import au.edu.jcu.zhangfan.primoria.data.local.TopicMasteryEntity
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.ParsedQuiz
import au.edu.jcu.zhangfan.primoria.data.remote.QuizRemoteDataSource
import au.edu.jcu.zhangfan.primoria.domain.logic.MasteryCalculator
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
import javax.inject.Inject
import javax.inject.Singleton

sealed interface QuizLoadResult {
    data class Ready(
        val topicId: Long,
        val topicTitle: String,
        val questions: List<QuizQuestion>,
        /** Shown when Primoria had to fall back to saved questions. */
        val notice: String? = null,
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
    val dailyCapReached: Boolean,
)

interface LearningRepository {
    fun observeTopics(): Flow<List<TopicSummary>>
    fun libraryTitles(): List<String>
    suspend fun ensureLibrarySeeded()
    suspend fun loadQuiz(rawTopic: String, settings: UserSettings): QuizLoadResult
    suspend fun submitAnswer(question: QuizQuestion, chosenIndex: Int, sessionId: String): AnswerResult
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
                        it.toEntity(topic.id, Difficulty.STANDARD, ContentSource.OFFLINE_LIBRARY, now)
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
                dailyCapReached = xpToday + xp >= XpPolicy.DAILY_CAP,
            )
        }
}

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
