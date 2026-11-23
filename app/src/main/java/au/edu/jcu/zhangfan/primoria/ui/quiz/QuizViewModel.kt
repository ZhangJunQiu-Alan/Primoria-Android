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
    private val sessionId = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _uiState.value = when (val result = learningRepository.loadQuiz(topic, settings)) {
                is QuizLoadResult.Ready -> QuizUiState.Active(
                    topicTitle = result.topicTitle,
                    questions = result.questions,
                    notice = result.notice,
                    hapticsEnabled = settings.haptics,
                )
                is QuizLoadResult.Refused -> QuizUiState.Refused(result.reason)
                is QuizLoadResult.Unavailable -> QuizUiState.Unavailable(result.message)
                QuizLoadResult.InvalidTopic -> QuizUiState.InvalidTopic
            }
        }
    }

    fun select(optionIndex: Int) = updateActive { state ->
        if (state.feedback != null) state else state.copy(selectedIndex = optionIndex)
    }

    fun check() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val chosen = state.selectedIndex ?: return
        if (state.feedback != null || state.submitting) return
        _uiState.value = state.copy(submitting = true)

        viewModelScope.launch {
            val result = learningRepository.submitAnswer(state.question, chosen, sessionId)
            if (settingsRepository.settings.first().soundEffects) soundEffects.playAnswer(result.correct)
            updateActive {
                it.copy(
                    submitting = false,
                    feedback = AnswerFeedback(chosen, result.correct, result.xpAwarded),
                    correctCount = it.correctCount + if (result.correct) 1 else 0,
                    xpEarned = it.xpEarned + result.xpAwarded,
                    mastery = result.mastery,
                    topicAttempts = result.topicAttempts,
                    dailyCapReached = result.dailyCapReached,
                )
            }
        }
    }

    fun next() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.feedback == null) return
        _uiState.value = if (state.isLastQuestion) {
            QuizUiState.Finished(
                topicTitle = state.topicTitle,
                total = state.questions.size,
                correct = state.correctCount,
                xpEarned = state.xpEarned,
                mastery = state.mastery,
                masteryLevel = MasteryLevel.of(state.mastery, state.topicAttempts),
                dailyCapReached = state.dailyCapReached,
            )
        } else {
            state.copy(index = state.index + 1, selectedIndex = null, feedback = null)
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

    companion object {
        /** Must match the property name in `QuizRoute`. */
        const val TOPIC_ARG = "topic"
    }
}
