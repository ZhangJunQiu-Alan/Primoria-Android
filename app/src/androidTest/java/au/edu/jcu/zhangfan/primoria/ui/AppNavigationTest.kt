package au.edu.jcu.zhangfan.primoria.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import au.edu.jcu.zhangfan.primoria.MainActivity
import au.edu.jcu.zhangfan.primoria.testing.FakeDeepSeekApi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * End-to-end navigation through the real app graph, with Hilt swapping in an in-memory
 * database and a fake DeepSeek (see TestModules).
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class AppNavigationTest {

    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeApi: FakeDeepSeekApi

    @Before
    fun setUp() {
        hilt.inject()
    }

    private fun completeOnboardingAsAdult(withAi: Boolean) {
        compose.waitUntilAtLeastOneExists(hasText("Welcome to Primoria"), TIMEOUT)
        compose.onNodeWithTag("onboarding_continue").performClick()
        compose.onNodeWithText("18 or older").performClick()
        compose.onNodeWithTag("onboarding_continue").performClick()
        if (withAi) compose.onNodeWithTag("ai_consent_toggle").performClick()
        compose.onNodeWithText("Start learning").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("topic_input"), TIMEOUT)
    }

    @Test
    fun aiQuiz_fromHomeToSummaryAndProgress() {
        fakeApi.reply = FakeDeepSeekApi.quizJson(5)
        completeOnboardingAsAdult(withAi = true)

        compose.onNodeWithTag("topic_input").performTextInput("Volcanoes")
        compose.onNodeWithTag("start_quiz").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Question 1 of 5"), TIMEOUT)
        compose.onNodeWithText("AI-generated").assertIsDisplayed()

        repeat(5) { index ->
            compose.onNodeWithTag("option_A").performClick()
            compose.onNodeWithTag("quiz_primary_action").performClick()
            compose.waitUntilAtLeastOneExists(hasText(if (index == 4) "See summary" else "Next question"), TIMEOUT)
            compose.onNodeWithTag("quiz_primary_action").performClick()
        }
        compose.waitUntilAtLeastOneExists(hasText("Quiz summary"), TIMEOUT)
        compose.onNodeWithText("Done").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Continue quiz"), TIMEOUT)

        // Top-level destination switch through the bottom bar.
        compose.onNodeWithText("Progress").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Your progress"), TIMEOUT)
        assertEquals("Only one quiz request should reach DeepSeek", 1, fakeApi.requests.size)
    }

    @Test
    fun withoutAiConsent_libraryQuizWorksAndDeepSeekIsNeverCalled() {
        completeOnboardingAsAdult(withAi = false)

        compose.onNodeWithTag("topic_input").performTextInput("black holes")
        compose.onNodeWithText("No offline topics match your search.").assertIsDisplayed()
        compose.onNodeWithTag("topic_input").performTextReplacement("pHoto")
        compose.onNodeWithText("Linear equations").assertDoesNotExist()
        compose.onNodeWithText("Photosynthesis").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Question 1 of 5"), TIMEOUT)
        compose.onNodeWithText("Offline library").assertIsDisplayed()

        assertTrue(fakeApi.requests.isEmpty())
    }

    @Test
    fun weeklyGoalStepperUpdatesHomeSummary() {
        completeOnboardingAsAdult(withAi = false)

        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("3 days").assertIsDisplayed()
        compose.onNodeWithContentDescription("Increase weekly goal").performClick()
        compose.waitUntilAtLeastOneExists(hasText("4 days"), TIMEOUT)

        compose.onNodeWithText("Home").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("0 of 4 days"))
        compose.onNodeWithText("0 of 4 days").assertIsDisplayed()
    }

    @Test
    fun deleteEverything_returnsToOnboarding() {
        completeOnboardingAsAdult(withAi = false)

        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithTag("open_my_data").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Your learning history stays on this device"), TIMEOUT)
        // The button is the last item of a lazy list, so scroll the list until it is composed.
        compose.onNode(hasScrollAction()).performScrollToNode(hasTestTag("delete_everything"))
        compose.onNodeWithTag("delete_everything").performClick()
        compose.onNodeWithTag("confirm_delete_everything").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Welcome to Primoria"), TIMEOUT)
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
