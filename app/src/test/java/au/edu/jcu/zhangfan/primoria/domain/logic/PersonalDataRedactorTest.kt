package au.edu.jcu.zhangfan.primoria.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataRedactorTest {

    @Test
    fun `detects and redacts email addresses`() {
        val text = "Algebra contact student.name+quiz@example.org please"

        val containsPersonalData = PersonalDataRedactor.containsPersonalData(text)
        val redacted = PersonalDataRedactor.redact(text)

        assertTrue(containsPersonalData)
        assertEquals("Algebra contact [removed] please", redacted)
        assertFalse(PersonalDataRedactor.containsPersonalData(redacted))
    }

    @Test
    fun `detects and redacts a phone number with a country code`() {
        val text = "Algebra contact +65 9123-4567 please"

        val containsPersonalData = PersonalDataRedactor.containsPersonalData(text)
        val redacted = PersonalDataRedactor.redact(text)

        assertTrue(containsPersonalData)
        assertEquals("Algebra contact [removed] please", redacted)
    }

    @Test
    fun `detects and redacts HTTP and HTTPS URLs`() {
        val urls = listOf("http://example.org/algebra", "https://example.org/algebra?class=9")

        for (url in urls) {
            val text = "Algebra notes $url for revision"

            val containsPersonalData = PersonalDataRedactor.containsPersonalData(text)
            val redacted = PersonalDataRedactor.redact(text)

            assertTrue(url, containsPersonalData)
            assertEquals("Algebra notes [removed] for revision", redacted)
        }
    }

    @Test
    fun `redacts multiple kinds of contact details from the same text`() {
        val text = "Algebra student@example.org +65 9123 4567 https://example.org/notes"

        val redacted = PersonalDataRedactor.redact(text)

        assertEquals("Algebra [removed] [removed] [removed]", redacted)
        assertFalse(PersonalDataRedactor.containsPersonalData(redacted))
    }

    @Test
    fun `keeps grade levels and standalone years in a topic`() {
        val text = "Year 9 history of 1945"

        val containsPersonalData = PersonalDataRedactor.containsPersonalData(text)
        val redacted = PersonalDataRedactor.redact(text)

        assertFalse(containsPersonalData)
        assertEquals(text, redacted)
    }

    @Test
    fun `keeps hyphenated year ranges in a history topic`() {
        val text = "World War I (1914-1918)"

        val containsPersonalData = PersonalDataRedactor.containsPersonalData(text)
        val redacted = PersonalDataRedactor.redact(text)

        assertEquals(text, redacted)
        assertFalse(containsPersonalData)
    }

    @Test
    fun `redacts the whole phone number including a parenthesized area code`() {
        val text = "Algebra contact (415) 555-2671 please"

        val redacted = PersonalDataRedactor.redact(text)

        assertEquals("Algebra contact [removed] please", redacted)
    }
}
