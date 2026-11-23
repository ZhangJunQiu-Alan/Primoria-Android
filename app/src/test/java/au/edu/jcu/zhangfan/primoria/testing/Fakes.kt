package au.edu.jcu.zhangfan.primoria.testing

import au.edu.jcu.zhangfan.primoria.data.feedback.SoundEffects
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.data.repository.AnswerResult
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.LearningStats
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
import au.edu.jcu.zhangfan.primoria.data.repository.QuizLoadResult
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

class FakeLearningRepository(
    var loadResult: QuizLoadResult = QuizLoadResult.InvalidTopic,
    var answerResult: AnswerResult = AnswerResult(
        correct = true,
        xpAwarded = 10,
        mastery = 0.75,
        topicAttempts = 1,
        dailyCapReached = false,
    ),
    private val library: List<String> = emptyList(),
) : LearningRepository {
    data class LoadRequest(val topic: String, val settings: UserSettings)
    data class SubmittedAnswer(
        val question: QuizQuestion,
        val chosenIndex: Int,
        val sessionId: String,
    )
    data class Report(val questionId: Long, val reason: String)

    val loadRequests = mutableListOf<LoadRequest>()
    val submittedAnswers = mutableListOf<SubmittedAnswer>()
    val reports = mutableListOf<Report>()

    override fun observeTopics() = flowOf(emptyList<TopicSummary>())

    override fun libraryTitles(): List<String> = library

    override suspend fun ensureLibrarySeeded() = Unit

    override suspend fun loadQuiz(rawTopic: String, settings: UserSettings): QuizLoadResult {
        loadRequests += LoadRequest(rawTopic, settings)
        return loadResult
    }

    override suspend fun submitAnswer(
        question: QuizQuestion,
        chosenIndex: Int,
        sessionId: String,
    ): AnswerResult {
        submittedAnswers += SubmittedAnswer(question, chosenIndex, sessionId)
        return answerResult
    }

    override suspend fun reportQuestion(questionId: Long, reason: String) {
        reports += Report(questionId, reason)
    }
}

class FakeSettingsRepository(initialSettings: UserSettings = UserSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initialSettings)

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        settings.update(transform)
    }

    override suspend fun reset() {
        settings.value = UserSettings()
    }
}

class FakeSoundEffects : SoundEffects {
    val answerCalls = mutableListOf<Boolean>()

    override fun playAnswer(correct: Boolean) {
        answerCalls += correct
    }
}

class FakeProgressRepository : ProgressRepository {
    private val stats = flowOf(
        LearningStats(
            totalXp = 0,
            xpToday = 0,
            dailyXpCap = 150,
            streak = StreakSummary(
                weeks = 0,
                activeDaysThisWeek = 0,
                weeklyGoal = 3,
                freezesUsed = 0,
                freezesLeft = 1,
            ),
            week = emptyList(),
            topics = emptyList(),
        ),
    )

    override fun observeStats() = stats
}

class FakeApiKeyProvider(initialKey: String? = null) : ApiKeyProvider {
    private val key = MutableStateFlow(initialKey)
    private val source = MutableStateFlow(if (initialKey == null) KeySource.NONE else KeySource.USER)
    override val keySource = source.asStateFlow()

    override fun currentKey(): String? = key.value

    override fun saveUserKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return
        this.key.value = trimmed
        source.value = KeySource.USER
    }

    override fun clearUserKey() {
        key.value = null
        source.value = KeySource.NONE
    }
}
