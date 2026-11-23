package au.edu.jcu.zhangfan.primoria.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.local.StoredDataCounts
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.PrivacyRepository
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyDataUiState(
    val counts: StoredDataCounts? = null,
    val topics: List<TopicSummary> = emptyList(),
    val retentionDays: Int = 90,
    val message: String? = null,
)

@HiltViewModel
class MyDataViewModel @Inject constructor(
    private val privacyRepository: PrivacyRepository,
    learningRepository: LearningRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val counts = MutableStateFlow<StoredDataCounts?>(null)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MyDataUiState> = combine(
        counts,
        learningRepository.observeTopics(),
        settingsRepository.settings,
        message,
    ) { counts, topics, settings, message ->
        MyDataUiState(counts, topics, settings.retentionDays, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyDataUiState())

    init {
        refreshCounts()
    }

    fun deleteTopic(topic: TopicSummary) {
        viewModelScope.launch {
            privacyRepository.deleteTopic(topic.id)
            message.value = "Deleted \"${topic.title}\" and all of its answers."
            refreshCounts()
        }
    }

    fun deleteEverything() {
        viewModelScope.launch {
            privacyRepository.deleteEverything()
            refreshCounts()
        }
    }

    fun messageShown() {
        message.value = null
    }

    private fun refreshCounts() {
        viewModelScope.launch { counts.value = privacyRepository.storedCounts() }
    }
}
