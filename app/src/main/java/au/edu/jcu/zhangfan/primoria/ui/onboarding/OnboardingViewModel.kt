package au.edu.jcu.zhangfan.primoria.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 0,
    val ageGroup: AgeGroup? = null,
    val aiConsent: Boolean = false,
    /** Learners aged 13–17 confirm a parent or guardian knows they use the app. */
    val guardianAware: Boolean = false,
) {
    val canContinue: Boolean get() = when (step) {
        0 -> true
        1 -> ageGroup != null
        else -> ageGroup != AgeGroup.TEEN || guardianAware
    }
    val isLastStep: Boolean get() = step == LAST_STEP

    companion object {
        const val LAST_STEP = 2
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun next() = _uiState.update {
        if (it.canContinue) it.copy(step = (it.step + 1).coerceAtMost(OnboardingUiState.LAST_STEP)) else it
    }

    fun back() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

    fun chooseAge(ageGroup: AgeGroup) = _uiState.update {
        // AI is opt-in for everyone, so changing the age band never pre-ticks consent.
        it.copy(ageGroup = ageGroup, guardianAware = false, aiConsent = false)
    }

    fun setAiConsent(enabled: Boolean) = _uiState.update { it.copy(aiConsent = enabled) }

    fun setGuardianAware(aware: Boolean) = _uiState.update { it.copy(guardianAware = aware) }

    fun finish() {
        val state = _uiState.value
        val ageGroup = state.ageGroup ?: return
        if (!state.canContinue) return
        viewModelScope.launch {
            settingsRepository.update {
                it.copy(onboardingComplete = true, ageGroup = ageGroup, aiConsent = state.aiConsent)
            }
        }
    }
}
