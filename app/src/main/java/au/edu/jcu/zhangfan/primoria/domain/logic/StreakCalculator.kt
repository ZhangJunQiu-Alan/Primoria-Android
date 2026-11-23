package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class StreakSummary(
    /** Consecutive weeks in which the weekly goal was met. */
    val weeks: Int,
    val activeDaysThisWeek: Int,
    val weeklyGoal: Int,
    val freezesUsed: Int,
    val freezesLeft: Int,
) {
    val goalMetThisWeek: Boolean get() = activeDaysThisWeek >= weeklyGoal
}

/**
 * Commitment 04 — humane streaks.
 *
 * Streaks count weeks, not days: a week counts when the learner studied on at least
 * [weeklyGoal] days. A missed week can be covered by a streak freeze, and the current
 * week never breaks the streak while it is still in progress, so there is no
 * "study tonight or lose everything" pressure.
 */
object StreakCalculator {

    const val FREEZES_PER_STREAK = 2

    fun calculate(
        activeDays: Set<LocalDate>,
        today: LocalDate,
        weeklyGoal: Int,
        freezes: Int = FREEZES_PER_STREAK,
    ): StreakSummary {
        val goal = weeklyGoal.coerceIn(1, 7)
        val thisWeekStart = weekStart(today)
        val activeThisWeek = activeDays.count { !it.isBefore(thisWeekStart) && !it.isAfter(today) }

        var weeks = if (activeThisWeek >= goal) 1 else 0
        var freezesLeft = freezes
        var freezesUsed = 0
        val earliest = activeDays.minOrNull()?.let(::weekStart)

        var week = thisWeekStart.minusWeeks(1)
        while (earliest != null && !week.isBefore(earliest)) {
            val weekEnd = week.plusDays(6)
            val activeInWeek = activeDays.count { !it.isBefore(week) && !it.isAfter(weekEnd) }
            when {
                activeInWeek >= goal -> weeks++
                freezesLeft > 0 && hasMetGoalBefore(activeDays, week, goal) -> {
                    freezesLeft--
                    freezesUsed++
                }
                else -> break
            }
            week = week.minusWeeks(1)
        }

        return StreakSummary(
            weeks = weeks,
            activeDaysThisWeek = activeThisWeek,
            weeklyGoal = goal,
            freezesUsed = if (weeks == 0) 0 else freezesUsed,
            freezesLeft = if (weeks == 0) freezes else freezesLeft,
        )
    }

    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** A freeze only makes sense if there is an earlier met week for it to protect. */
    private fun hasMetGoalBefore(activeDays: Set<LocalDate>, week: LocalDate, goal: Int): Boolean =
        activeDays.filter { it.isBefore(week) }
            .groupBy(::weekStart)
            .any { (_, days) -> days.size >= goal }
}
