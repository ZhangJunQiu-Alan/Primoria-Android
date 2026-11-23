package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DataRetentionTest {

    @Test
    fun `offers thirty ninety and three hundred sixty five day retention periods`() {
        val expected = listOf(30, 90, 365)

        val options = DataRetention.OPTIONS_DAYS

        assertEquals(expected, options)
    }

    @Test
    fun `cutoff is the chosen number of days before the current timestamp`() {
        val now = LocalDate.of(2026, 1, 1).atTime(12, 30).toInstant(ZoneOffset.UTC)
        val expectedDateByRetentionDays = mapOf(
            30 to LocalDate.of(2025, 12, 2),
            90 to LocalDate.of(2025, 10, 3),
            365 to LocalDate.of(2025, 1, 1),
        )

        for ((retentionDays, expectedDate) in expectedDateByRetentionDays) {
            val expected = expectedDate.atTime(12, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
            val cutoff = DataRetention.cutoffMillis(now.toEpochMilli(), retentionDays)

            assertEquals("Retention days $retentionDays", expected, cutoff)
        }
    }

    @Test
    fun `retention periods below one day use a one day cutoff`() {
        val now = LocalDate.of(2026, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val expected = LocalDate.of(2025, 12, 31).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val zeroDayCutoff = DataRetention.cutoffMillis(now, retentionDays = 0)
        val negativeDayCutoff = DataRetention.cutoffMillis(now, retentionDays = -10)

        assertEquals(expected, zeroDayCutoff)
        assertEquals(expected, negativeDayCutoff)
    }
}
