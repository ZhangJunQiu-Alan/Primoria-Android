package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
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
import kotlinx.coroutines.flow.stateIn
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    learningRepository: LearningRepository,
    progressRepository: ProgressRepository,
    settingsRepository: SettingsRepository,
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
            recentTopics = stats.studiedTopics.take(MAX_RECENT),
            libraryTopics = libraryTitles,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onTopicInputChange(value: String) {
        topicInput = value.take(TopicNormalizer.MAX_LENGTH)
    }

    /** Returns the cleaned topic to open, or null when the input has no usable text. */
    fun consumeTopic(): String? = TopicNormalizer.clean(topicInput)?.also { topicInput = "" }

    private companion object {
        const val MAX_RECENT = 5
    }
}
