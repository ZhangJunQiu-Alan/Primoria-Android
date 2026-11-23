package au.edu.jcu.zhangfan.primoria.domain.logic

/** Cleans the free-text topic a learner types into something safe to store and send. */
object TopicNormalizer {

    const val MAX_LENGTH = 60

    /** Returns null when nothing meaningful is left. */
    fun clean(raw: String): String? {
        val cleaned = PersonalDataRedactor.redact(raw)
            .replace(PersonalDataRedactor.REDACTED, " ")
            .replace(Regex("""[\p{Cntrl}]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(MAX_LENGTH)
            .trim()
        return cleaned.takeIf { it.any(Char::isLetterOrDigit) }
    }

    /** Key used to recognise the same topic typed with different case or spacing. */
    fun key(title: String): String = title.lowercase().replace(Regex("""\s+"""), " ").trim()
}
