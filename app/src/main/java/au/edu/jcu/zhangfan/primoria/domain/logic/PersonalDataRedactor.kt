package au.edu.jcu.zhangfan.primoria.domain.logic

/**
 * Data minimisation before anything is sent to DeepSeek.
 *
 * Learners sometimes type more than a topic ("help me, I'm Ana at Rosewood High,
 * ana@mail.com"). Contact details are removed so only the subject matter leaves the device.
 */
object PersonalDataRedactor {

    private val email = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val phone = Regex("""(?<!\w)\+?\d[\d\s-]{6,}\d(?!\w)""")
    private val url = Regex("""https?://\S+""")

    const val REDACTED = "[removed]"

    fun redact(text: String): String = text
        .replace(url, REDACTED)
        .replace(email, REDACTED)
        .replace(phone, REDACTED)

    fun containsPersonalData(text: String): Boolean =
        email.containsMatchIn(text) || phone.containsMatchIn(text) || url.containsMatchIn(text)
}
