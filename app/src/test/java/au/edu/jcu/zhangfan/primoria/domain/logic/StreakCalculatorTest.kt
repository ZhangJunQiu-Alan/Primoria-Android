package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun `week starts on Monday even across a year boundary`() {
        val monday = LocalDate.of(2025, 12, 29)
        val sunday = LocalDate.of(2026, 1, 4)
        val nextMonday = LocalDate.of(2026, 1, 5)

        val startOfSunday = StreakCalculator.weekStart(sunday)
        val startOfMonday = StreakCalculator.weekStart(nextMonday)

        assertEquals(monday, startOfSunday)
        assertEquals(nextMonday, startOfMonday)
    }

    @Test
    fun `met weeks count toward the streak including the current week`() {
        val today = LocalDate.of(2026, 1, 14)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 11),
            LocalDate.of(2026, 1, 12), today,
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(2, 2, 2, 0, 2), summary)
        assertTrue(summary.goalMetThisWeek)
    }

    @Test
    fun `an in progress current week neither breaks the streak nor consumes a freeze`() {
        val today = LocalDate.of(2026, 1, 20)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 14),
            today, LocalDate.of(2026, 1, 21),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(2, 1, 2, 0, 2), summary)
        assertFalse(summary.goalMetThisWeek)
    }

    @Test
    fun `a freeze bridges a missed week between two met weeks`() {
        val today = LocalDate.of(2026, 1, 26)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 21),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(2, 0, 2, 1, 1), summary)
    }

    @Test
    fun `both default freezes can bridge two missed weeks`() {
        val today = LocalDate.of(2026, 2, 2)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 26), LocalDate.of(2026, 1, 28),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(2, 0, 2, 2, 0), summary)
    }

    @Test
    fun `a missed completed week breaks the streak when no freezes remain`() {
        val today = LocalDate.of(2026, 1, 26)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 21),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2, freezes = 0)

        assertEquals(StreakSummary(1, 0, 2, 0, 0), summary)
    }

    @Test
    fun `freezes are not spent on weeks before the first met week`() {
        val today = LocalDate.of(2026, 1, 26)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 21),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(1, 0, 2, 0, 2), summary)
    }

    @Test
    fun `a new streak keeps its freezes when a gap is too long to bridge`() {
        val today = LocalDate.of(2026, 2, 9)
        val activeDays = setOf(
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4),
        )

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(1, 0, 2, 0, 2), summary)
    }

    @Test
    fun `empty history yields no streak and leaves all freezes available`() {
        val today = LocalDate.of(2026, 1, 26)
        val activeDays = emptySet<LocalDate>()

        val summary = StreakCalculator.calculate(activeDays, today, weeklyGoal = 2)

        assertEquals(StreakSummary(0, 0, 2, 0, 2), summary)
        assertFalse(summary.goalMetThisWeek)
    }

    @Test
    fun `weekly goal is bounded by one and seven days`() {
        val today = LocalDate.of(2026, 1, 26)
        val activeDays = setOf(today)

        val minimumGoal = StreakCalculator.calculate(activeDays, today, weeklyGoal = 0)
        val maximumGoal = StreakCalculator.calculate(activeDays, today, weeklyGoal = 8)

        assertEquals(StreakSummary(1, 1, 1, 0, 2), minimumGoal)
        assertTrue(minimumGoal.goalMetThisWeek)
        assertEquals(StreakSummary(0, 1, 7, 0, 2), maximumGoal)
        assertFalse(maximumGoal.goalMetThisWeek)
    }
}
