package au.edu.jcu.zhangfan.primoria.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.reminder.ReminderScheduler
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val keySource: KeySource = KeySource.NONE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val keyProvider: ApiKeyProvider,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        keyProvider.keySource,
    ) { settings, keySource -> SettingsUiState(settings, keySource) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** One entry point for every preference keeps the screen simple and the logic testable. */
    fun update(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    /** Called only after the notification permission was granted (or is not needed). */
    fun setReminder(enabled: Boolean, hour: Int = uiState.value.settings.reminderHour) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(reminderEnabled = enabled, reminderHour = hour) }
            if (enabled) reminderScheduler.schedule(hour) else reminderScheduler.cancel()
        }
    }

    fun saveApiKey(key: String) = keyProvider.saveUserKey(key)

    fun clearApiKey() = keyProvider.clearUserKey()
}
