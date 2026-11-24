package au.edu.jcu.zhangfan.primoria.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.zhangfan.primoria.data.local.StoredDataCounts
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.ui.onboarding.OnboardingScreen
import au.edu.jcu.zhangfan.primoria.ui.onboarding.OnboardingUiState
import au.edu.jcu.zhangfan.primoria.ui.privacy.MyDataScreen
import au.edu.jcu.zhangfan.primoria.ui.privacy.MyDataUiState
import au.edu.jcu.zhangfan.primoria.ui.quiz.AnswerFeedback
import au.edu.jcu.zhangfan.primoria.ui.quiz.QuizScreen
import au.edu.jcu.zhangfan.primoria.ui.quiz.QuizUiState
import au.edu.jcu.zhangfan.primoria.ui.settings.SettingsScreen
import au.edu.jcu.zhangfan.primoria.ui.settings.SettingsUiState
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** GUI tests of stateless screens: each renders a given UI state and reports user intents. */
@RunWith(AndroidJUnit4::class)
class ScreenComposeTest {

    @get:Rule
    val compose = createComposeRule()

    private val question = QuizQuestion(
        id = 1, topicId = 1, prompt = "What is 2 + 2?", options = listOf("3", "4", "5", "22"),
        correctIndex = 1, explanation = "Two plus two is four.", difficulty = Difficulty.STANDARD,
        source = ContentSource.AI_GENERATED,
    )

    @Test
    fun teenCannotFinishOnboardingUntilGuardianIsAware() {
        var state = OnboardingUiState(step = 2, ageGroup = AgeGroup.TEEN)
        compose.setContent {
            PrimoriaTheme {
                OnboardingScreen(state, {}, {}, {}, {}, { state = state.copy(guardianAware = it) }, {})
            }
        }
        compose.onNodeWithTag("onboarding_continue").assertIsNotEnabled()
        compose.onNodeWithText("Off unless you turn it on.").assertIsDisplayed()
    }

    @Test
    fun adultCanFinishOnboardingWithAiLeftOff() {
        var finished = false
        compose.setContent {
            PrimoriaTheme {
                OnboardingScreen(OnboardingUiState(step = 2, ageGroup = AgeGroup.ADULT), {}, {}, {}, {}, {}, { finished = true })
            }
        }
        compose.onNodeWithTag("onboarding_continue").assertIsEnabled().performClick()
        assertTrue(finished)
    }

    @Test
    fun aiQuestionShowsLabelAndReportAction() {
        compose.setContent {
            PrimoriaTheme {
                QuizScreen(QuizUiState.Active("Maths", listOf(question)), {}, {}, {}, {}, { _, _ -> }, {}, {})
            }
        }
        compose.onNodeWithText("AI-generated").assertIsDisplayed()
        compose.onNodeWithContentDescription("Report this question").assertIsDisplayed()
        compose.onNodeWithTag("quiz_primary_action").assertIsNotEnabled()
    }

    @Test
    fun checkedAnswerIsAnnouncedInWordsNotOnlyColour() {
        val state = QuizUiState.Active(
            "Maths", listOf(question), selectedIndex = 0,
            feedback = AnswerFeedback(chosenIndex = 0, correct = false, xpAwarded = 0),
        )
        compose.setContent {
            PrimoriaTheme { QuizScreen(state, {}, {}, {}, {}, { _, _ -> }, {}, {}) }
        }
        compose.onNode(hasStateDescription("Correct answer")).assertExists()
        compose.onNode(hasStateDescription("Your answer, incorrect")).assertExists()
        compose.onNodeWithText("Not quite — here's why:").assertExists()
        compose.onNodeWithText("See summary").assertIsEnabled()
    }

    @Test
    fun reportingAQuestionSendsTheChosenReason() {
        var reported: Pair<QuizQuestion, String>? = null
        compose.setContent {
            PrimoriaTheme {
                QuizScreen(QuizUiState.Active("Maths", listOf(question)), {}, {}, {}, {}, { q, r -> reported = q to r }, {}, {})
            }
        }
        compose.onNodeWithContentDescription("Report this question").performClick()
        compose.onNodeWithText("The answer is wrong").performClick()
        compose.onNodeWithText("Submit report").performClick()

        assertEquals(question to "The answer is wrong", reported)
    }

    @Test
    fun summaryShowsEffortAndMasterySeparately() {
        compose.setContent {
            PrimoriaTheme {
                QuizScreen(
                    QuizUiState.Finished("Maths", 5, 4, 40, 0.62, au.edu.jcu.zhangfan.primoria.domain.model.MasteryLevel.SECURE, false),
                    {}, {}, {}, {}, { _, _ -> }, {}, {},
                )
            }
        }
        compose.onNodeWithText("4 of 5 correct").assertIsDisplayed()
        compose.onNodeWithText("+40 XP").assertIsDisplayed()
        compose.onNodeWithText("62%").assertIsDisplayed()
    }

    @Test
    fun teenTurningOnAiInSettingsMustConfirmFirst() {
        var settings = UserSettings(onboardingComplete = true, ageGroup = AgeGroup.TEEN, aiConsent = false)
        compose.setContent {
            PrimoriaTheme {
                SettingsScreen(SettingsUiState(settings, KeySource.BUILD), false, {}, {}, { settings = it(settings) }, {}, {}, {})
            }
        }
        compose.onNodeWithTag("settings_ai_toggle").performScrollTo().performClick()

        compose.onNodeWithText("Turn on AI quizzes?").assertIsDisplayed()
        assertEquals(false, settings.aiConsent)
        compose.onNodeWithText("They know — turn on").performClick()
        assertEquals(true, settings.aiConsent)
    }

    @Test
    fun deleteEverythingAsksBeforeWiping() {
        var wiped = false
        compose.setContent {
            PrimoriaTheme {
                MyDataScreen(
                    MyDataUiState(counts = StoredDataCounts(2, 12, 30, 1)),
                    {}, {}, {}, { wiped = true }, {},
                )
            }
        }
        compose.onNodeWithText("30 answers · 1 reports").assertExists()
        compose.onNodeWithTag("delete_everything").performClick()
        compose.onNodeWithText("Keep my data").assertIsDisplayed()
        assertEquals(false, wiped)

        compose.onNodeWithTag("confirm_delete_everything").performClick()
        assertTrue(wiped)
    }

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
}
