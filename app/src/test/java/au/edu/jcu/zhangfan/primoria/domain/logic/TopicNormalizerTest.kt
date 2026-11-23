package au.edu.jcu.zhangfan.primoria.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TopicNormalizerTest {

    @Test
    fun `cleans whitespace and control characters while preserving the topic`() {
        val raw = "  Year\t9\nWorld\u0000History   1945  "

        val cleaned = TopicNormalizer.clean(raw)

        assertEquals("Year 9 World History 1945", cleaned)
    }

    @Test
    fun `removes contact details and keeps the surrounding subject`() {
        val raw = "  Year 9 student@example.org History +65 9123 4567 1945 https://example.org/notes  "

        val cleaned = TopicNormalizer.clean(raw)

        assertEquals("Year 9 History 1945", cleaned)
    }

    @Test
    fun `returns null when no letters or digits remain`() {
        val rawTopics = listOf("", " \t\n ", " -- ... ! ", "student@example.org")

        for (raw in rawTopics) {
            val cleaned = TopicNormalizer.clean(raw)

            assertNull(cleaned)
        }
    }

    @Test
    fun `keeps a topic exactly sixty characters long`() {
        val raw = "a".repeat(60)

        val cleaned = TopicNormalizer.clean(raw)

        assertEquals(raw, cleaned)
    }

    @Test
    fun `limits the cleaned topic to its first sixty characters`() {
        val firstSixtyCharacters = "a".repeat(60)
        val raw = "  ${firstSixtyCharacters}extra  "

        val cleaned = TopicNormalizer.clean(raw)

        assertEquals(firstSixtyCharacters, cleaned)
    }

    @Test
    fun `trims whitespace left at the truncation boundary`() {
        val firstWord = "a".repeat(59)
        val raw = "$firstWord extra"

        val cleaned = TopicNormalizer.clean(raw)

        assertEquals(firstWord, cleaned)
    }

    @Test
    fun `topic keys ignore case and spacing`() {
        val firstTitle = "  World\tHISTORY "
        val secondTitle = "world   history"

        val firstKey = TopicNormalizer.key(firstTitle)
        val secondKey = TopicNormalizer.key(secondTitle)

        assertEquals("world history", firstKey)
        assertEquals(firstKey, secondKey)
    }
}
