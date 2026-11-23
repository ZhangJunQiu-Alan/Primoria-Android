package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class XpPolicyTest {

    @Test
    fun `a correct first answer earns the base XP for its difficulty`() {
        val expectedByDifficulty = mapOf(
            Difficulty.FOUNDATION to 5,
            Difficulty.STANDARD to 10,
            Difficulty.CHALLENGE to 15,
        )

        for ((difficulty, expected) in expectedByDifficulty) {
            val awarded = XpPolicy.award(
                correct = true, firstAttempt = true, difficulty = difficulty, xpEarnedToday = 0,
            )

            assertEquals(difficulty.name, expected, awarded)
        }
    }

    @Test
    fun `an incorrect first answer earns no XP`() {
        val difficulty = Difficulty.CHALLENGE

        val awarded = XpPolicy.award(
            correct = false, firstAttempt = true, difficulty = difficulty, xpEarnedToday = 0,
        )

        assertEquals(0, awarded)
    }

    @Test
    fun `a correct retry earns no XP`() {
        val difficulty = Difficulty.CHALLENGE

        val awarded = XpPolicy.award(
            correct = true, firstAttempt = false, difficulty = difficulty, xpEarnedToday = 0,
        )

        assertEquals(0, awarded)
    }

    @Test
    fun `a full award can reach the daily cap exactly`() {
        val xpEarnedToday = 135

        val awarded = XpPolicy.award(
            correct = true, firstAttempt = true, difficulty = Difficulty.CHALLENGE,
            xpEarnedToday = xpEarnedToday,
        )

        assertEquals(15, awarded)
    }

    @Test
    fun `an award is reduced to the last available XP below the daily cap`() {
        val xpEarnedToday = 149

        val awarded = XpPolicy.award(
            correct = true, firstAttempt = true, difficulty = Difficulty.CHALLENGE,
            xpEarnedToday = xpEarnedToday,
        )

        assertEquals(1, awarded)
    }

    @Test
    fun `a learner at or above the daily cap earns no further XP`() {
        val cappedTotals = listOf(150, 151)

        for (xpEarnedToday in cappedTotals) {
            val awarded = XpPolicy.award(
                correct = true, firstAttempt = true, difficulty = Difficulty.CHALLENGE,
                xpEarnedToday = xpEarnedToday,
            )

            assertEquals("XP already earned: $xpEarnedToday", 0, awarded)
        }
    }

    @Test
    fun `a custom daily cap limits the award`() {
        val dailyCap = 12

        val awarded = XpPolicy.award(
            correct = true, firstAttempt = true, difficulty = Difficulty.STANDARD,
            xpEarnedToday = 9, dailyCap = dailyCap,
        )

        assertEquals(3, awarded)
    }
}
