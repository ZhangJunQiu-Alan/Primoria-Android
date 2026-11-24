package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.domain.logic.OptionShuffler
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Wraps the DeepSeek call so the repository only deals with validated results. */
@Singleton
class QuizRemoteDataSource @Inject constructor(
    private val api: DeepSeekApi,
    @Named("deepseekModel") private val model: String,
) {
    suspend fun generate(
        topic: String,
        difficulty: Difficulty,
        count: Int,
        ageGroup: AgeGroup,
    ): ParsedQuiz = generate(QuizPrompt.messages(topic, difficulty, count, ageGroup), count)

    suspend fun generateFromMaterial(
        excerpt: String,
        difficulty: Difficulty,
        count: Int,
        ageGroup: AgeGroup,
    ): ParsedQuiz = generate(QuizPrompt.materialMessages(excerpt, difficulty, count, ageGroup), count)

    private suspend fun generate(messages: List<ChatMessage>, count: Int): ParsedQuiz {
        val response = api.chat(
            ChatRequest(
                model = model,
                messages = messages,
            ),
        )
        return when (val parsed = QuizResponseParser.parse(response.firstContent, maxQuestions = count)) {
            is ParsedQuiz.Questions -> ParsedQuiz.Questions(parsed.drafts.map { OptionShuffler.shuffle(it) })
            else -> parsed
        }
    }
}
