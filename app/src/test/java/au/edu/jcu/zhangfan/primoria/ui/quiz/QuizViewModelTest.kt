package au.edu.jcu.zhangfan.primoria.ui.quiz

import androidx.lifecycle.SavedStateHandle
import au.edu.jcu.zhangfan.primoria.data.repository.AnswerResult
import au.edu.jcu.zhangfan.primoria.data.repository.QuizLoadResult
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.MasteryLevel
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.testing.FakeLearningRepository
import au.edu.jcu.zhangfan.primoria.testing.FakeSettingsRepository
import au.edu.jcu.zhangfan.primoria.testing.FakeSoundEffects
import au.edu.jcu.zhangfan.primoria.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val question = QuizQuestion(
        id = 42,
        topicId = 7,
        prompt = "What is molten rock below Earth's surface called?",
        options = listOf("Lava", "Magma", "Ash", "Pumice"),
        correctIndex = 1,
        explanation = "Molten rock below the surface is called magma.",
        difficulty = Difficulty.STANDARD,
        source = ContentSource.OFFLINE_LIBRARY,
    )
    private val ready = QuizLoadResult.Ready(
        topicId = question.topicId,
        topicTitle = "Volcanoes",
        questions = listOf(question),
        notice = "Practising saved questions.",
    )
    private val repository = FakeLearningRepository(loadResult = ready)
    private val settings = FakeSettingsRepository()
    private val sound = FakeSoundEffects()

    @Test
    fun `ready result moves from Loading to Active`() = runTest {
        val preferences = UserSettings(haptics = false)
        settings.settings.value = preferences
        val viewModel = createViewModel()
        assertEquals(QuizUiState.Loading, viewModel.uiState.value)

        runCurrent()

        assertEquals(
            QuizUiState.Active("Volcanoes", listOf(question), notice = ready.notice, hapticsEnabled = false),
            viewModel.uiState.value,
        )
        assertEquals(
            listOf(FakeLearningRepository.LoadRequest("Volcanoes", preferences)),
            repository.loadRequests,
        )
    }

    @Test
    fun `refused result shows the refusal reason`() = runTest {
        val reason = "Try a related science topic."
        repository.loadResult = QuizLoadResult.Refused(reason)
        val viewModel = createViewModel()

        runCurrent()

        assertEquals(QuizUiState.Refused(reason), viewModel.uiState.value)
    }

    @Test
    fun `unavailable result shows the repository message`() = runTest {
        val message = "There are no saved questions for this topic."
        repository.loadResult = QuizLoadResult.Unavailable(message)
        val viewModel = createViewModel()

        runCurrent()

        assertEquals(QuizUiState.Unavailable(message), viewModel.uiState.value)
    }

    @Test
    fun `invalid topic result maps to InvalidTopic`() = runTest {
        repository.loadResult = QuizLoadResult.InvalidTopic
        val viewModel = createViewModel()

        runCurrent()

        assertEquals(QuizUiState.InvalidTopic, viewModel.uiState.value)
    }

    @Test
    fun `checking the correct selection submits the answer plays sound and adds XP`() = runTest {
        repository.answerResult = AnswerResult(true, 7, 0.72, 3, false)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.select(question.correctIndex)
        viewModel.check()
        runCurrent()

        val submitted = repository.submittedAnswers.single()
        assertEquals(question, submitted.question)
        assertEquals(question.correctIndex, submitted.chosenIndex)
        assertTrue(submitted.sessionId.isNotBlank())
        assertEquals(listOf(true), sound.answerCalls)
        val state = viewModel.uiState.value as QuizUiState.Active
        assertEquals(AnswerFeedback(question.correctIndex, true, 7), state.feedback)
        assertEquals(1, state.correctCount)
        assertEquals(7, state.xpEarned)
        assertFalse(state.submitting)
    }

    @Test
    fun `checking with sound effects disabled records feedback without playing sound`() = runTest {
        settings.settings.value = UserSettings(soundEffects = false)
        repository.answerResult = AnswerResult(false, 0, 0.25, 1, false)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.select(0)
        viewModel.check()
        runCurrent()

        assertEquals(0, repository.submittedAnswers.single().chosenIndex)
        assertTrue(sound.answerCalls.isEmpty())
        val state = viewModel.uiState.value as QuizUiState.Active
        assertEquals(AnswerFeedback(0, false, 0), state.feedback)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.xpEarned)
    }

    @Test
    fun `checking without a selection has no effect`() = runTest {
        val viewModel = createViewModel()
        runCurrent()
        val initialState = viewModel.uiState.value

        viewModel.check()
        runCurrent()

        assertEquals(initialState, viewModel.uiState.value)
        assertTrue(repository.submittedAnswers.isEmpty())
        assertTrue(sound.answerCalls.isEmpty())
    }

    @Test
    fun `checking again before or after feedback submits just once`() = runTest {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.select(question.correctIndex)

        viewModel.check()
        assertTrue((viewModel.uiState.value as QuizUiState.Active).submitting)
        viewModel.check()
        runCurrent()
        viewModel.check()
        runCurrent()

        assertEquals(1, repository.submittedAnswers.size)
        assertEquals(listOf(true), sound.answerCalls)
        val state = viewModel.uiState.value as QuizUiState.Active
        assertEquals(1, state.correctCount)
        assertEquals(10, state.xpEarned)
    }

    @Test
    fun `selecting another option after feedback leaves the checked answer selected`() = runTest {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.select(question.correctIndex)
        viewModel.check()
        runCurrent()
        val checkedState = viewModel.uiState.value as QuizUiState.Active

        viewModel.select(0)

        assertEquals(question.correctIndex, checkedState.selectedIndex)
        assertEquals(checkedState, viewModel.uiState.value)
    }

    @Test
    fun `next on the final question finishes using mastery and topic attempts`() = runTest {
        val expectedLevels = listOf(7 to MasteryLevel.SECURE, 8 to MasteryLevel.MASTERED)

        for ((attempts, expectedLevel) in expectedLevels) {
            repository.answerResult = AnswerResult(true, 7, 0.9, attempts, true)
            val viewModel = createViewModel()
            runCurrent()
            viewModel.select(question.correctIndex)
            viewModel.check()
            runCurrent()

            viewModel.next()

            val result = viewModel.uiState.value as QuizUiState.Finished
            assertEquals(
                "Mastery level for $attempts topic attempts",
                QuizUiState.Finished("Volcanoes", 1, 1, 7, 0.9, expectedLevel, true),
                result.copy(session = null),
            )
            assertEquals(AnswerFeedback(question.correctIndex, true, 7), result.session?.answers?.get(0))
        }
    }

    @Test
    fun `going back preserves checked answers and unsubmitted choices without earning more XP`() = runTest {
        repository.loadResult = ready.copy(questions = listOf(question, question.copy(id = 43), question.copy(id = 44)))
        val viewModel = createViewModel()
        runCurrent()

        viewModel.select(question.correctIndex)
        viewModel.check()
        runCurrent()
        viewModel.next()
        viewModel.select(2)
        viewModel.previous()

        val restored = viewModel.uiState.value as QuizUiState.Active
        assertEquals(0, restored.index)
        assertEquals(AnswerFeedback(question.correctIndex, true, 10), restored.feedback)
        viewModel.check()
        viewModel.next()
        runCurrent()

        val draft = viewModel.uiState.value as QuizUiState.Active
        assertEquals(1, draft.index)
        assertEquals(2, draft.selectedIndex)
        assertEquals(null, draft.feedback)
        assertEquals(10, draft.xpEarned)
        assertEquals(1, repository.submittedAnswers.size)
        assertEquals(listOf(true), sound.answerCalls)
    }

    @Test
    fun `review missed answers traverses only mistakes and returns to the unchanged score`() = runTest {
        repository.loadResult = ready.copy(questions = List(3) { question.copy(id = 42L + it) })
        val viewModel = createViewModel()
        runCurrent()

        repeat(3) { index ->
            val correct = index == 1
            repository.answerResult = AnswerResult(correct, if (correct) 10 else 0, .25, index + 1, false)
            viewModel.select(if (correct) question.correctIndex else 0)
            viewModel.check()
            runCurrent()
            viewModel.next()
        }
        val summary = viewModel.uiState.value as QuizUiState.Finished
        viewModel.openReview(null)
        assertEquals(0, (viewModel.uiState.value as QuizUiState.Active).index)
        viewModel.next()
        assertEquals(2, (viewModel.uiState.value as QuizUiState.Active).index)
        viewModel.previous()
        assertEquals(0, (viewModel.uiState.value as QuizUiState.Active).index)
        viewModel.next()
        viewModel.next()
        assertEquals(summary, viewModel.uiState.value)
        assertEquals(3, repository.submittedAnswers.size)
    }

    @Test
    fun `report sends the question id and reason and tracks the reported id`() = runTest {
        val viewModel = createViewModel()
        val reason = "The wording is unclear."
        runCurrent()

        viewModel.report(question, reason)
        runCurrent()

        assertEquals(listOf(FakeLearningRepository.Report(question.id, reason)), repository.reports)
        val state = viewModel.uiState.value as QuizUiState.Active
        assertEquals(setOf(question.id), state.reportedIds)
    }

    private fun createViewModel() = QuizViewModel(
        SavedStateHandle(mapOf(QuizViewModel.TOPIC_ARG to "Volcanoes")),
        repository,
        settings,
        sound,
    )
}
