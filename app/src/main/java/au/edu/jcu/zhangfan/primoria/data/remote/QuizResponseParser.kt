package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

sealed interface ParsedQuiz {
    data class Questions(val drafts: List<QuestionDraft>) : ParsedQuiz
    data class Refused(val reason: String) : ParsedQuiz
    data object Invalid : ParsedQuiz
}

/**
 * Checks DeepSeek's reply before anything reaches the learner.
 *
 * Model output is untrusted: malformed questions (wrong option count, duplicate options,
 * an answer index out of range, empty text) are dropped rather than shown.
 */
object QuizResponseParser {

    private const val MAX_PROMPT = 400
    private const val MAX_OPTION = 160
    private const val MAX_EXPLANATION = 600

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class Payload(
        val refused: Boolean = false,
        val reason: String = "",
        val questions: List<RawQuestion> = emptyList(),
    )

    @Serializable
    private data class RawQuestion(
        val prompt: String = "",
        val options: List<String> = emptyList(),
        val correctIndex: Int = -1,
        val explanation: String = "",
    )

    fun parse(content: String?, maxQuestions: Int): ParsedQuiz {
        if (content.isNullOrBlank()) return ParsedQuiz.Invalid
        val payload = try {
            json.decodeFromString(Payload.serializer(), stripCodeFence(content))
        } catch (e: SerializationException) {
            return ParsedQuiz.Invalid
        } catch (e: IllegalArgumentException) {
            return ParsedQuiz.Invalid
        }

        if (payload.refused) {
            val reason = payload.reason.trim().ifEmpty { "This topic can't be turned into a quiz. Try a related school subject." }
            return ParsedQuiz.Refused(reason.take(MAX_EXPLANATION))
        }

        val drafts = payload.questions.mapNotNull(::validate).take(maxQuestions)
        return if (drafts.isEmpty()) ParsedQuiz.Invalid else ParsedQuiz.Questions(drafts)
    }

    private fun validate(raw: RawQuestion): QuestionDraft? {
        val prompt = raw.prompt.trim()
        val options = raw.options.map(String::trim)
        val explanation = raw.explanation.trim()
        val valid = prompt.isNotEmpty() && prompt.length <= MAX_PROMPT &&
            options.size == 4 &&
            options.all { it.isNotEmpty() && it.length <= MAX_OPTION } &&
            options.map(String::lowercase).toSet().size == 4 &&
            raw.correctIndex in options.indices &&
            explanation.isNotEmpty() && explanation.length <= MAX_EXPLANATION
        return if (valid) QuestionDraft(prompt, options, raw.correctIndex, explanation) else null
    }

    private fun stripCodeFence(text: String): String =
        text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
}
