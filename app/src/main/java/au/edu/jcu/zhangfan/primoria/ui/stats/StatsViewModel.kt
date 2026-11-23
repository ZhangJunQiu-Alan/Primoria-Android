package au.edu.jcu.zhangfan.primoria.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.repository.LearningStats
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data class Loaded(val stats: LearningStats) : StatsUiState
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    progressRepository: ProgressRepository,
) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = progressRepository.observeStats()
        .map<LearningStats, StatsUiState> { StatsUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState.Loading)
}
