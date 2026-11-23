package au.edu.jcu.zhangfan.primoria.ui.onboarding

import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.testing.FakeSettingsRepository
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
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val viewModel = OnboardingViewModel(settings)

    @Test
    fun `age selection step cannot continue without an age group`() {
        viewModel.next()

        viewModel.next()

        assertEquals(1, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `choosing either age group leaves AI consent unchecked`() {
        viewModel.next()
        assertFalse(viewModel.uiState.value.aiConsent)

        viewModel.chooseAge(AgeGroup.TEEN)

        assertEquals(AgeGroup.TEEN, viewModel.uiState.value.ageGroup)
        assertFalse(viewModel.uiState.value.aiConsent)

        viewModel.setAiConsent(true)
        viewModel.chooseAge(AgeGroup.ADULT)

        assertEquals(AgeGroup.ADULT, viewModel.uiState.value.ageGroup)
        assertFalse(viewModel.uiState.value.aiConsent)
    }

    @Test
    fun `teen cannot finish until guardian awareness is confirmed`() = runTest {
        viewModel.next()
        viewModel.chooseAge(AgeGroup.TEEN)
        viewModel.next()
        viewModel.setAiConsent(true)

        viewModel.finish()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLastStep)
        assertFalse(viewModel.uiState.value.canContinue)
        assertEquals(UserSettings(), settings.settings.value)

        viewModel.setGuardianAware(true)
        viewModel.finish()
        runCurrent()

        assertTrue(settings.settings.value.onboardingComplete)
        assertEquals(AgeGroup.TEEN, settings.settings.value.ageGroup)
        assertTrue(settings.settings.value.aiConsent)
    }

    @Test
    fun `adult finishes without guardian awareness and persists age and AI consent`() = runTest {
        viewModel.next()
        viewModel.chooseAge(AgeGroup.ADULT)
        viewModel.next()
        val initialSettings = UserSettings(soundEffects = false)
        settings.settings.value = initialSettings
        assertFalse(viewModel.uiState.value.guardianAware)
        assertTrue(viewModel.uiState.value.canContinue)

        viewModel.finish()
        runCurrent()

        assertEquals(
            initialSettings.copy(
                onboardingComplete = true,
                ageGroup = AgeGroup.ADULT,
                aiConsent = false,
            ),
            settings.settings.value,
        )
    }
}
