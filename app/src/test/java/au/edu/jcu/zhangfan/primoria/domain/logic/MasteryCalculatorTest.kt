package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class MasteryCalculatorTest {

    @Test
    fun `correct answers raise mastery more at higher difficulties`() {
        val current = 0.4
        val expectedByDifficulty = mapOf(
            Difficulty.FOUNDATION to 0.49,
            Difficulty.STANDARD to 0.55,
            Difficulty.CHALLENGE to 0.61,
        )

        for ((difficulty, expected) in expectedByDifficulty) {
            val mastery = MasteryCalculator.update(current, correct = true, difficulty = difficulty)

            assertEquals(difficulty.name, expected, mastery, 1e-9)
        }
    }

    @Test
    fun `incorrect answers cost less mastery at higher difficulties`() {
        val current = 0.4
        val expectedByDifficulty = mapOf(
            Difficulty.FOUNDATION to 0.28,
            Difficulty.STANDARD to 0.30,
            Difficulty.CHALLENGE to 0.34,
        )

        for ((difficulty, expected) in expectedByDifficulty) {
            val mastery = MasteryCalculator.update(current, correct = false, difficulty = difficulty)

            assertEquals(difficulty.name, expected, mastery, 1e-9)
        }
    }

    @Test
    fun `a second answer updates the estimate from the previous answer`() {
        val difficulty = Difficulty.STANDARD
        val afterCorrectAnswer = MasteryCalculator.update(0.0, correct = true, difficulty = difficulty)

        val afterIncorrectAnswer = MasteryCalculator.update(
            afterCorrectAnswer, correct = false, difficulty = difficulty,
        )

        assertEquals(0.1875, afterIncorrectAnswer, 1e-9)
    }

    @Test
    fun `updated mastery is bounded by zero and one`() {
        val difficulty = Difficulty.STANDARD

        val lowerBound = MasteryCalculator.update(-1.0, correct = false, difficulty = difficulty)
        val upperBound = MasteryCalculator.update(2.0, correct = true, difficulty = difficulty)

        assertEquals(0.0, lowerBound, 1e-9)
        assertEquals(1.0, upperBound, 1e-9)
    }
}
