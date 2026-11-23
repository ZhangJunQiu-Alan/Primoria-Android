package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class QuietHoursTest {

    @Test
    fun `daytime quiet hours include the start and exclude the end`() {
        val expectedByTime = mapOf(
            LocalTime.of(8, 59) to false,
            LocalTime.of(9, 0) to true,
            LocalTime.of(12, 30) to true,
            LocalTime.of(16, 59) to true,
            LocalTime.of(17, 0) to false,
        )

        for ((time, expected) in expectedByTime) {
            val isQuiet = QuietHours.contains(time, startHour = 9, endHour = 17)

            assertEquals("Time $time", expected, isQuiet)
        }
    }

    @Test
    fun `overnight quiet hours include midnight and end at the morning boundary`() {
        val expectedByTime = mapOf(
            LocalTime.of(20, 59) to false,
            LocalTime.of(21, 0) to true,
            LocalTime.of(23, 59) to true,
            LocalTime.of(0, 0) to true,
            LocalTime.of(6, 59) to true,
            LocalTime.of(7, 0) to false,
            LocalTime.of(12, 0) to false,
        )

        for ((time, expected) in expectedByTime) {
            val isQuiet = QuietHours.contains(time, startHour = 21, endHour = 7)

            assertEquals("Time $time", expected, isQuiet)
        }
    }

    @Test
    fun `equal start and end hours disable quiet hours`() {
        val times = listOf(LocalTime.MIDNIGHT, LocalTime.of(21, 0), LocalTime.of(23, 59))

        for (time in times) {
            val isQuiet = QuietHours.contains(time, startHour = 21, endHour = 21)

            assertFalse("Time $time", isQuiet)
        }
    }
}
