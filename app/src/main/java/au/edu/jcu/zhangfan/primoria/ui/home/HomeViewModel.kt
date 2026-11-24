package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
import au.edu.jcu.zhangfan.primoria.data.repository.QuizLoadResult
import au.edu.jcu.zhangfan.primoria.data.repository.StudyMaterial
import au.edu.jcu.zhangfan.primoria.data.repository.StudyMaterialReadException
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary
import au.edu.jcu.zhangfan.primoria.domain.logic.TopicNormalizer
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val aiEnabled: Boolean = false,
    val streak: StreakSummary? = null,
    val xpToday: Int = 0,
    val dailyXpCap: Int = 0,
    val recentTopics: List<TopicSummary> = emptyList(),
    val libraryTopics: List<String> = emptyList(),
)

internal sealed interface MaterialUiState {
    data object Closed : MaterialUiState
    data object Choose : MaterialUiState
    data object Reading : MaterialUiState
    data class Preview(val source: StudyMaterial, val error: String? = null) : MaterialUiState
    data class Generating(val title: String) : MaterialUiState
    data class Failed(val message: String) : MaterialUiState
    data class Ready(val quiz: QuizLoadResult.Ready) : MaterialUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /**
     * Text-field state is held as Compose state and updated synchronously; routing it through
     * an async Flow drops or reorders characters while the learner types.
     */
    var topicInput by mutableStateOf("")
        private set

    val canStart: Boolean get() = TopicNormalizer.clean(topicInput) != null

    private val libraryTitles = learningRepository.libraryTitles()

    val uiState: StateFlow<HomeUiState> = combine(
        progressRepository.observeStats(),
        settingsRepository.settings,
    ) { stats, settings ->
        HomeUiState(
            loading = false,
            aiEnabled = settings.aiConsent,
            streak = stats.streak,
            xpToday = stats.xpToday,
            dailyXpCap = stats.dailyXpCap,
            recentTopics = stats.topics.filter { it.attempts > 0 || it.isStudyMaterial }.take(MAX_RECENT),
            libraryTopics = libraryTitles,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onTopicInputChange(value: String) {
        topicInput = value.take(TopicNormalizer.MAX_LENGTH)
    }

    /** Returns the cleaned topic to open, or null when the input has no usable text. */
    fun consumeTopic(): String? = TopicNormalizer.clean(topicInput)?.also { topicInput = "" }

    internal var materialState by mutableStateOf<MaterialUiState>(MaterialUiState.Closed)
        private set
    private var materialWork: Job? = null

    internal fun openMaterial() {
        materialState = MaterialUiState.Choose
    }

    internal fun readMaterial(read: suspend () -> StudyMaterial) {
        materialWork?.cancel()
        materialState = MaterialUiState.Reading
        materialWork = viewModelScope.launch {
            materialState = try {
                MaterialUiState.Preview(read())
            } catch (e: CancellationException) {
                throw e
            } catch (e: StudyMaterialReadException) {
                MaterialUiState.Failed(e.message.orEmpty())
            } catch (_: Exception) {
                MaterialUiState.Failed("That file couldn't be read. Choose a different file.")
            }
        }
    }

    internal fun generateMaterialQuiz() {
        val preview = materialState as? MaterialUiState.Preview ?: return
        materialState = MaterialUiState.Generating(preview.source.title)
        materialWork = viewModelScope.launch {
            val result = try {
                learningRepository.createMaterialQuiz(preview.source.title, preview.source.excerpt, settingsRepository.settings.first())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                QuizLoadResult.Unavailable("Couldn't save this quiz. Try again.")
            }
            materialState = when (result) {
                is QuizLoadResult.Ready -> MaterialUiState.Ready(result)
                is QuizLoadResult.Refused -> preview.copy(error = result.reason)
                is QuizLoadResult.Unavailable -> preview.copy(error = result.message)
                QuizLoadResult.InvalidTopic -> preview.copy(error = "Choose a file with readable study text.")
            }
        }
    }

    internal fun closeMaterial() {
        materialWork?.cancel()
        materialState = MaterialUiState.Closed
    }

    private companion object {
        const val MAX_RECENT = 5
    }
}
