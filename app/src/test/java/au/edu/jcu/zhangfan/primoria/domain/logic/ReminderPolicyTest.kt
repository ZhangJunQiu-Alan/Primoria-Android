package au.edu.jcu.zhangfan.primoria.domain.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ReminderPolicyTest {

    private val afternoon = LocalDateTime.of(2026, 11, 24, 17, 0)

    private fun decide(
        now: LocalDateTime = afternoon,
        enabled: Boolean = true,
        lastReminderEpochDay: Long? = null,
        studiedToday: Boolean = false,
        weeklyGoalMet: Boolean = false,
    ) = ReminderPolicy.shouldNotify(now, enabled, 21, 7, lastReminderEpochDay, studiedToday, weeklyGoalMet)

    @Test
    fun `sends one reminder when the learner opted in and has not studied`() {
        assertTrue(decide())
    }

    @Test
    fun `never sends when reminders are off`() {
        assertFalse(decide(enabled = false))
    }

    @Test
    fun `never sends late at night or early in the morning`() {
        assertFalse(decide(now = afternoon.withHour(22)))
        assertFalse(decide(now = afternoon.withHour(6)))
        assertTrue(decide(now = afternoon.withHour(7)))
    }

    @Test
    fun `at most one reminder per day`() {
        assertFalse(decide(lastReminderEpochDay = afternoon.toLocalDate().toEpochDay()))
        assertTrue(decide(lastReminderEpochDay = afternoon.toLocalDate().minusDays(1).toEpochDay()))
    }

    @Test
    fun `stays quiet once the learner studied today or met the weekly goal`() {
        assertFalse(decide(studiedToday = true))
        assertFalse(decide(weeklyGoalMet = true))
    }

    @Test
    fun `message is encouraging and never loss-framed`() {
        val messages = listOf(ReminderPolicy.message(0, 3), ReminderPolicy.message(2, 3))

        messages.forEach { text ->
            listOf("lose", "lost", "don't", "miss", "break").forEach { word ->
                assertFalse("\"$text\" contains \"$word\"", text.lowercase().contains(word))
            }
        }
    }
}
