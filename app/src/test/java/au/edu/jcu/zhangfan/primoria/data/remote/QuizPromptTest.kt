package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizPromptTest {
    @Test
    fun `user message includes the topic difficulty label and count`() {
        val topic = "Volcanoes"
        val difficulty = Difficulty.CHALLENGE
        val count = 7

        val messages = QuizPrompt.messages(topic, difficulty, count, AgeGroup.ADULT)

        assertEquals(listOf("system", "user"), messages.map { it.role })
        val userMessage = messages.single { it.role == "user" }.content
        assertTrue(userMessage.contains("Topic: $topic"))
        assertTrue(userMessage.contains("Difficulty: ${difficulty.label}"))
        assertTrue(userMessage.contains("Number of questions: $count"))
    }

    @Test
    fun `teen system prompt names the secondary school audience and requests JSON and refusal`() {
        val ageGroup = AgeGroup.TEEN

        val systemMessage = QuizPrompt.messages("Volcanoes", Difficulty.STANDARD, 5, ageGroup)
            .single { it.role == "system" }.content

        assertTrue(systemMessage.contains("secondary-school learners aged 13 to 17"))
        assertJsonAndRefusalInstructions(systemMessage)
    }

    @Test
    fun `adult system prompt names the adult audience and requests JSON and refusal`() {
        val ageGroup = AgeGroup.ADULT

        val systemMessage = QuizPrompt.messages("Volcanoes", Difficulty.STANDARD, 5, ageGroup)
            .single { it.role == "system" }.content

        assertTrue(systemMessage.contains("learners aged 18 or over"))
        assertJsonAndRefusalInstructions(systemMessage)
    }

    @Test
    fun `messages do not mention learner data for either age group`() {
        val learnerDataWords = Regex(
            """\b(?:scores?|mastery|xp|history|emails?)\b""",
            RegexOption.IGNORE_CASE,
        )

        val messages = AgeGroup.entries.flatMap { ageGroup ->
            QuizPrompt.messages("Volcanoes", Difficulty.STANDARD, 5, ageGroup)
        }

        assertFalse(
            "Learner data terms appeared in the prompt: ${messages.mapNotNull { learnerDataWords.find(it.content)?.value }}",
            messages.any { learnerDataWords.containsMatchIn(it.content) },
        )
    }

    private fun assertJsonAndRefusalInstructions(systemMessage: String) {
        assertTrue(systemMessage.contains("JSON only"))
        assertTrue(Regex("""set "refused" to true\s+and give a .*?reason""", RegexOption.DOT_MATCHES_ALL)
            .containsMatchIn(systemMessage))
    }
}
