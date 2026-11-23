package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizResponseParserTest {
    private val validDraft = QuestionDraft(
        prompt = "What is molten rock below Earth's surface called?",
        options = listOf("Lava", "Magma", "Ash", "Pumice"),
        correctIndex = 1,
        explanation = "Molten rock below the surface is called magma.",
    )

    @Test
    fun `valid payload returns the cleaned question`() {
        val rawQuestion = validDraft.copy(
            prompt = "  ${validDraft.prompt} ",
            options = validDraft.options.map { " $it " },
            explanation = " ${validDraft.explanation} ",
        )
        val payload = quizPayload(rawQuestion)

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `refusal returns the reason even when questions are present`() {
        val reason = "Try a topic from earth science."
        val payload = buildJsonObject {
            put("refused", true)
            put("reason", " $reason ")
            put("questions", JsonArray(listOf(questionJson(validDraft))))
        }.toString()

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Refused(reason), result)
    }

    @Test
    fun `refusal without a reason returns the default message`() {
        val payload = """{"refused": true}"""

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(
            ParsedQuiz.Refused("This topic can't be turned into a quiz. Try a related school subject."),
            result,
        )
    }

    @Test
    fun `invalid JSON returns Invalid`() {
        val payload = """{"questions":["""

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Invalid, result)
    }

    @Test
    fun `code fenced JSON returns the question`() {
        val payload = " ```json\n${quizPayload(validDraft)}\n``` "

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `question with three options is dropped`() {
        val invalidDraft = validDraft.copy(options = listOf("Lava", "Magma", "Ash"))
        val payload = quizPayload(invalidDraft, validDraft)

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `question with case insensitive duplicate options is dropped`() {
        val invalidDraft = validDraft.copy(options = listOf("Lava", "Magma", "Ash", "mAGMA"))
        val payload = quizPayload(invalidDraft, validDraft)

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `questions with out of range answer indices are dropped`() {
        val payload = quizPayload(
            validDraft.copy(correctIndex = -1),
            validDraft.copy(correctIndex = 4),
            validDraft,
        )

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `question with a blank explanation is dropped`() {
        val payload = quizPayload(validDraft.copy(explanation = " \t "), validDraft)

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft)), result)
    }

    @Test
    fun `only the first maxQuestions valid questions are kept`() {
        val second = validDraft.copy(prompt = "What type of rock forms as magma cools?")
        val third = validDraft.copy(prompt = "What can cause magma to rise?")
        val payload = quizPayload(validDraft.copy(explanation = ""), validDraft, second, third)

        val result = QuizResponseParser.parse(payload, maxQuestions = 2)

        assertEquals(ParsedQuiz.Questions(listOf(validDraft, second)), result)
    }

    @Test
    fun `payload with only invalid questions returns Invalid`() {
        val payload = quizPayload(
            validDraft.copy(options = emptyList()),
            validDraft.copy(explanation = ""),
        )

        val result = QuizResponseParser.parse(payload, maxQuestions = 5)

        assertEquals(ParsedQuiz.Invalid, result)
    }

    private fun quizPayload(vararg questions: QuestionDraft): String = buildJsonObject {
        put("refused", false)
        put("questions", JsonArray(questions.map(::questionJson)))
    }.toString()

    private fun questionJson(question: QuestionDraft): JsonObject = buildJsonObject {
        put("prompt", question.prompt)
        put("options", JsonArray(question.options.map { JsonPrimitive(it) }))
        put("correctIndex", question.correctIndex)
        put("explanation", question.explanation)
    }
}
