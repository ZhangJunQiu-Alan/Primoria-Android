package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OptionShufflerTest {

    private val draft = QuestionDraft(
        prompt = "Which gas do plants take in?",
        options = listOf("Carbon dioxide", "Oxygen", "Nitrogen", "Helium"),
        correctIndex = 0,
        explanation = "Plants absorb CO2.",
    )

    @Test
    fun `correct answer text is preserved after shuffling`() {
        repeat(50) { seed ->
            val shuffled = OptionShuffler.shuffle(draft, Random(seed))

            assertEquals("Carbon dioxide", shuffled.options[shuffled.correctIndex])
            assertEquals(draft.options.toSet(), shuffled.options.toSet())
        }
    }

    @Test
    fun `answers that always come first get spread across positions`() {
        // Mirrors the DeepSeek behaviour seen in testing: every correct answer at index 0.
        val positions = (0 until 200).map { seed -> OptionShuffler.shuffle(draft, Random(seed)).correctIndex }

        val counts = positions.groupingBy { it }.eachCount()
        assertEquals(4, counts.size)
        assertTrue("Each position should appear regularly: $counts", counts.values.all { it >= 30 })
    }
}
