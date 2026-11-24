package au.edu.jcu.zhangfan.primoria.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.feedback.SoundEffects
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.QuizLoadResult
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.model.MasteryLevel
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AnswerFeedback(val chosenIndex: Int, val correct: Boolean, val xpAwarded: Int)

sealed interface QuizUiState {
    data object Loading : QuizUiState

    data class Active(
        val topicTitle: String,
        val questions: List<QuizQuestion>,
        val index: Int = 0,
        val selectedIndex: Int? = null,
        val feedback: AnswerFeedback? = null,
        val notice: String? = null,
        val correctCount: Int = 0,
        val xpEarned: Int = 0,
        val mastery: Double = 0.0,
        val topicAttempts: Int = 0,
        val dailyCapReached: Boolean = false,
        val reportedIds: Set<Long> = emptySet(),
        val submitting: Boolean = false,
        val hapticsEnabled: Boolean = false,
        val fromStudyMaterial: Boolean = false,
        val answers: Map<Int, AnswerFeedback> = emptyMap(),
        val draftSelections: Map<Int, Int> = emptyMap(),
        val reviewSummary: Finished? = null,
        val reviewIndices: List<Int> = emptyList(),
        val submitError: String? = null,
    ) : QuizUiState {
        val question: QuizQuestion get() = questions[index]
        val isLastQuestion: Boolean get() = index == questions.lastIndex
        val progress: Float get() = (index + if (feedback != null) 1 else 0).toFloat() / questions.size
    }

    data class Finished(
        val topicTitle: String,
        val total: Int,
        val correct: Int,
        val xpEarned: Int,
        val mastery: Double,
        val masteryLevel: MasteryLevel,
        val dailyCapReached: Boolean,
        val session: Active? = null,
    ) : QuizUiState

    data class Refused(val reason: String) : QuizUiState
    data class Unavailable(val message: String) : QuizUiState
    data object InvalidTopic : QuizUiState
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val learningRepository: LearningRepository,
    private val settingsRepository: SettingsRepository,
    private val soundEffects: SoundEffects,
) : ViewModel() {

    /** Read by key rather than `toRoute()` so the ViewModel can be unit-tested without Android. */
    private val topic: String = savedStateHandle.get<String>(TOPIC_ARG).orEmpty()
    private val savedTopicId: Long = savedStateHandle.get<Long>("savedTopicId") ?: 0
    private val sessionId = UUID.randomUUID().toString()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true) return
        _uiState.value = QuizUiState.Loading
        loadJob = viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val result = try {
                if (savedTopicId > 0) learningRepository.loadSavedQuiz(savedTopicId, topic, settings)
                else learningRepository.loadQuiz(topic, settings)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                QuizLoadResult.Unavailable("Couldn't prepare your questions. Please try again.")
            }
            _uiState.value = when (result) {
                is QuizLoadResult.Ready -> QuizUiState.Active(
                    topicTitle = result.topicTitle,
                    questions = result.questions,
                    notice = result.notice,
                    hapticsEnabled = settings.haptics,
                    fromStudyMaterial = result.fromStudyMaterial,
                )
                is QuizLoadResult.Refused -> QuizUiState.Refused(result.reason)
                is QuizLoadResult.Unavailable -> QuizUiState.Unavailable(result.message)
                QuizLoadResult.InvalidTopic -> QuizUiState.InvalidTopic
            }
        }
    }

    fun select(optionIndex: Int) = updateActive { state ->
        if (state.feedback != null || state.submitting || optionIndex !in state.question.options.indices) state
        else state.copy(
            selectedIndex = optionIndex,
            draftSelections = state.draftSelections + (state.index to optionIndex),
            submitError = null,
        )
    }

    fun check() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val chosen = state.selectedIndex ?: return
        if (state.feedback != null || state.submitting) return
        _uiState.value = state.copy(submitting = true, submitError = null)

        viewModelScope.launch {
            val result = try {
                learningRepository.submitAnswer(state.question, chosen, sessionId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                updateActive { it.copy(submitting = false, submitError = "Couldn't save your answer. Try checking it again.") }
                return@launch
            }
            val feedback = AnswerFeedback(chosen, result.correct, result.xpAwarded)
            updateActive {
                it.copy(
                    submitting = false,
                    feedback = feedback,
                    answers = it.answers + (state.index to feedback),
                    correctCount = it.correctCount + if (result.correct) 1 else 0,
                    xpEarned = it.xpEarned + result.xpAwarded,
                    mastery = result.mastery,
                    topicAttempts = result.topicAttempts,
                    dailyCapReached = result.dailyCapReached,
                )
            }
            if (settingsRepository.settings.first().soundEffects) soundEffects.playAnswer(result.correct)
        }
    }

    fun previous() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.submitting) return
        val index = if (state.reviewSummary == null) state.index - 1
        else state.reviewIndices.getOrNull(state.reviewIndices.indexOf(state.index) - 1) ?: return
        if (index >= 0) _uiState.value = state.atQuestion(index)
    }

    fun next() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.feedback == null || state.submitting) return
        val summary = state.reviewSummary
        if (summary != null) {
            val nextIndex = state.reviewIndices.getOrNull(state.reviewIndices.indexOf(state.index) + 1)
            if (nextIndex == null) returnToSummary() else _uiState.value = state.atQuestion(nextIndex)
            return
        }
        _uiState.value = if (state.isLastQuestion) {
            state.toSummary()
        } else {
            state.atQuestion(state.index + 1)
        }
    }

    fun openReview(index: Int?) {
        val summary = _uiState.value as? QuizUiState.Finished ?: return
        val session = summary.session ?: return
        val indices = if (index == null) session.answers.filterValues { !it.correct }.keys.sorted()
        else session.answers.keys.sorted().filter { it >= index }
        val first = indices.firstOrNull() ?: return
        _uiState.value = session.atQuestion(first).copy(reviewSummary = summary, reviewIndices = indices)
    }

    fun returnToSummary() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val summary = state.reviewSummary
        if (summary != null) {
            _uiState.value = summary.copy(session = summary.session?.copy(reportedIds = state.reportedIds))
        }
    }

    fun report(question: QuizQuestion, reason: String) {
        viewModelScope.launch {
            learningRepository.reportQuestion(question.id, reason)
            updateActive { it.copy(reportedIds = it.reportedIds + question.id) }
        }
    }

    private inline fun updateActive(crossinline transform: (QuizUiState.Active) -> QuizUiState.Active) {
        _uiState.update { if (it is QuizUiState.Active) transform(it) else it }
    }

    private fun QuizUiState.Active.atQuestion(target: Int): QuizUiState.Active = copy(
        index = target,
        selectedIndex = answers[target]?.chosenIndex ?: draftSelections[target],
        feedback = answers[target],
        submitError = null,
    )

    private fun QuizUiState.Active.toSummary() = QuizUiState.Finished(
        topicTitle = topicTitle,
        total = questions.size,
        correct = correctCount,
        xpEarned = xpEarned,
        mastery = mastery,
        masteryLevel = MasteryLevel.of(mastery, topicAttempts),
        dailyCapReached = dailyCapReached,
        session = this,
    )

    companion object {
        /** Must match the property name in `QuizRoute`. */
        const val TOPIC_ARG = "topic"
    }
}
