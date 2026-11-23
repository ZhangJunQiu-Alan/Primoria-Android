package au.edu.jcu.zhangfan.primoria.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.PrivacyRepository
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppState {
    data object Loading : AppState
    data class Ready(val settings: UserSettings) : AppState
}

/** App-wide state: decides between onboarding and the main app, and applies theme settings. */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    learningRepository: LearningRepository,
    privacyRepository: PrivacyRepository,
) : ViewModel() {

    val appState: StateFlow<AppState> = settingsRepository.settings
        .map<UserSettings, AppState> { AppState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState.Loading)

    init {
        viewModelScope.launch {
            learningRepository.ensureLibrarySeeded()
            // Expired history is removed on every launch (commitment 01).
            privacyRepository.applyRetention(settingsRepository.settings.first().retentionDays)
        }
    }
}
