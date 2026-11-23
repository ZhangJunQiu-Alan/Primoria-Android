package au.edu.jcu.zhangfan.primoria.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MasteryLevelTest {

    @Test
    fun `a topic with no attempts is new regardless of its estimate`() {
        val mastery = 1.0

        val level = MasteryLevel.of(mastery, attempts = 0)

        assertEquals(MasteryLevel.NEW, level)
    }

    @Test
    fun `the secure level starts at a mastery estimate of sixty percent`() {
        val attempts = 8

        val belowSecure = MasteryLevel.of(mastery = 0.599, attempts = attempts)
        val atSecure = MasteryLevel.of(mastery = 0.60, attempts = attempts)

        assertEquals(MasteryLevel.DEVELOPING, belowSecure)
        assertEquals(MasteryLevel.SECURE, atSecure)
    }

    @Test
    fun `a high mastery estimate still needs at least eight attempts`() {
        val mastery = 0.85

        val tooFewAttempts = MasteryLevel.of(mastery, attempts = 7)
        val enoughAttempts = MasteryLevel.of(mastery, attempts = 8)

        assertEquals(MasteryLevel.SECURE, tooFewAttempts)
        assertEquals(MasteryLevel.MASTERED, enoughAttempts)
    }

    @Test
    fun `eight attempts still need at least an eighty five percent estimate`() {
        val attempts = 8

        val belowMastered = MasteryLevel.of(mastery = 0.849, attempts = attempts)
        val atMastered = MasteryLevel.of(mastery = 0.85, attempts = attempts)

        assertEquals(MasteryLevel.SECURE, belowMastered)
        assertEquals(MasteryLevel.MASTERED, atMastered)
    }
}
