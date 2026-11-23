package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.LocalDateTime

/**
 * Commitment 04 — humane reminders. A reminder is only sent when the learner asked for one,
 * never during quiet hours, at most once a day, and not at all once they have already
 * studied today or met this week's goal. Silence is the default answer.
 */
object ReminderPolicy {

    fun shouldNotify(
        now: LocalDateTime,
        enabled: Boolean,
        quietStartHour: Int,
        quietEndHour: Int,
        lastReminderEpochDay: Long?,
        studiedToday: Boolean,
        weeklyGoalMet: Boolean,
    ): Boolean = enabled &&
        !QuietHours.contains(now.toLocalTime(), quietStartHour, quietEndHour) &&
        lastReminderEpochDay != now.toLocalDate().toEpochDay() &&
        !studiedToday &&
        !weeklyGoalMet

    /** Positive, specific copy; never "don't lose your streak". */
    fun message(activeDaysThisWeek: Int, weeklyGoal: Int): String =
        if (activeDaysThisWeek == 0) {
            "Ready for a five-question practice? Any topic counts towards this week's goal."
        } else {
            "You've studied on $activeDaysThisWeek of $weeklyGoal days this week. Fancy a short practice?"
        }
}
