package au.edu.jcu.zhangfan.primoria.data.remote

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
    ): ParsedQuiz {
        val response = api.chat(
            ChatRequest(
                model = model,
                messages = QuizPrompt.messages(topic, difficulty, count, ageGroup),
            ),
        )
        return QuizResponseParser.parse(response.firstContent, maxQuestions = count)
    }
}
