package au.edu.jcu.zhangfan.primoria.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.data.remote.QuizRemoteDataSource
import au.edu.jcu.zhangfan.primoria.domain.logic.XpPolicy
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.testing.FakeDeepSeekApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Exercises the repository against a real (in-memory) Room database and a fake DeepSeek. */
@RunWith(AndroidJUnit4::class)
class LearningRepositoryTest {

    private lateinit var db: PrimoriaDatabase
    private lateinit var api: FakeDeepSeekApi
    private lateinit var repository: DefaultLearningRepository
    private val keyProvider = object : ApiKeyProvider {
        var key: String? = "test-key"
        override val keySource: StateFlow<KeySource> = MutableStateFlow(KeySource.BUILD)
        override fun currentKey(): String? = key
        override fun saveUserKey(key: String) = Unit
        override fun clearUserKey() = Unit
    }
    private val library = object : OfflineLibrary {
        override fun topics() = listOf(
            LibraryTopic(
                "Photosynthesis",
                List(6) { QuestionDraft("Library Q$it", listOf("Right", "No1", "No2", "No3"), 0, "Because.") },
            ),
        )
    }
    private val aiOn = UserSettings(onboardingComplete = true, aiConsent = true, questionsPerQuiz = 3)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PrimoriaDatabase::class.java).build()
        api = FakeDeepSeekApi()
        val clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC)
        repository = DefaultLearningRepository(db, QuizRemoteDataSource(api, "test-model"), keyProvider, library, clock)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun aiQuiz_isStoredAndLabelledAsAiGenerated() = runTest {
        val result = repository.loadQuiz("Volcanoes", aiOn) as QuizLoadResult.Ready

        assertEquals(3, result.questions.size)
        assertTrue(result.questions.all { it.source == ContentSource.AI_GENERATED })
        result.questions.forEach { assertTrue(it.options[it.correctIndex].startsWith("Right")) }
        assertEquals(3, db.questionDao().count())
    }

    @Test
    fun onlyTheCleanedTopicIsSentToDeepSeek() = runTest {
        repository.loadQuiz("Volcanoes email me at kid@school.edu", aiOn)

        val sent = api.requests.single().messages.joinToString(" ") { it.content }
        assertTrue(sent.contains("Volcanoes"))
        assertTrue(!sent.contains("kid@school.edu"))
    }

    @Test
    fun materialQuizRequiresConsentAndCanBeReopenedOffline() = runTest {
        val title = "My local file name"
        val excerpt = "Plants use photosynthesis to convert light into chemical energy. Contact: kid@school.edu."
        val offline = aiOn.copy(aiConsent = false)

        assertTrue(repository.createMaterialQuiz(title, excerpt, offline) is QuizLoadResult.Unavailable)
        assertTrue(api.requests.isEmpty())

        val generated = repository.createMaterialQuiz(title, excerpt, aiOn) as QuizLoadResult.Ready
        val sent = api.requests.single().messages.joinToString(" ") { it.content }
        assertTrue(sent.contains("chemical energy"))
        assertTrue(!sent.contains(title))
        assertTrue(!sent.contains("kid@school.edu"))
        assertTrue(generated.fromStudyMaterial)
        assertEquals(title, generated.topicTitle)

        val reopened = repository.loadSavedQuiz(generated.topicId, title, offline) as QuizLoadResult.Ready
        assertTrue(reopened.fromStudyMaterial)
        assertEquals(generated.questions.map { it.id }.toSet(), reopened.questions.map { it.id }.toSet())
        assertEquals(1, api.requests.size)
    }

    @Test
    fun refusal_isPassedToTheLearnerWithoutFallingBack() = runTest {
        api.reply = FakeDeepSeekApi.REFUSAL

        val result = repository.loadQuiz("Something unsafe", aiOn)

        assertTrue(result is QuizLoadResult.Refused)
    }

    @Test
    fun networkFailure_fallsBackToLibraryQuestionsWithANotice() = runTest {
        repository.ensureLibrarySeeded()
        api.failWith = IOException("offline")

        val result = repository.loadQuiz("photosynthesis", aiOn) as QuizLoadResult.Ready

        assertNotNull(result.notice)
        assertTrue(result.questions.all { it.source == ContentSource.OFFLINE_LIBRARY })
    }

    @Test
    fun withoutConsent_deepSeekIsNeverCalled() = runTest {
        repository.ensureLibrarySeeded()

        val result = repository.loadQuiz("Photosynthesis", aiOn.copy(aiConsent = false))

        assertTrue(result is QuizLoadResult.Ready)
        assertTrue(api.requests.isEmpty())
    }

    @Test
    fun unknownTopicOffline_isUnavailable() = runTest {
        val result = repository.loadQuiz("Quantum chromodynamics", aiOn.copy(aiConsent = false))

        assertTrue(result is QuizLoadResult.Unavailable)
    }

    @Test
    fun xp_isOnlyForCorrectFirstAttempts() = runTest {
        val question = (repository.loadQuiz("Volcanoes", aiOn) as QuizLoadResult.Ready).questions.first()

        val wrong = repository.submitAnswer(question, (question.correctIndex + 1) % 4, "s1")
        val rightAfterWrong = repository.submitAnswer(question, question.correctIndex, "s1")

        assertEquals(0, wrong.xpAwarded)
        assertEquals(0, rightAfterWrong.xpAwarded)
        assertEquals(2, rightAfterWrong.topicAttempts)
    }

    @Test
    fun xp_stopsAtTheDailyCap() = runTest {
        api.reply = FakeDeepSeekApi.quizJson(20)
        val questions = (repository.loadQuiz("Volcanoes", aiOn.copy(questionsPerQuiz = 20)) as QuizLoadResult.Ready).questions

        val total = questions.sumOf { repository.submitAnswer(it, it.correctIndex, "s").xpAwarded }

        assertEquals(XpPolicy.DAILY_CAP, total)
    }

    @Test
    fun reportedQuestions_areNotServedAgain() = runTest {
        repository.ensureLibrarySeeded()
        val offline = aiOn.copy(aiConsent = false, questionsPerQuiz = 10)
        val first = (repository.loadQuiz("Photosynthesis", offline) as QuizLoadResult.Ready).questions
        repository.reportQuestion(first.first().id, "The answer is wrong")

        val second = (repository.loadQuiz("Photosynthesis", offline) as QuizLoadResult.Ready).questions

        assertEquals(first.size - 1, second.size)
        assertTrue(second.none { it.id == first.first().id })
    }
}
