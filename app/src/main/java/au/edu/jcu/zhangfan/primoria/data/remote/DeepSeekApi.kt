package au.edu.jcu.zhangfan.primoria.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/** DeepSeek's OpenAI-compatible chat completions endpoint. */
interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens") val maxTokens: Int = 2_000,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat(),
)

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ResponseFormat(val type: String = "json_object")

@Serializable
data class ChatResponse(val choices: List<Choice> = emptyList()) {
    @Serializable
    data class Choice(val message: ChatMessage? = null)

    val firstContent: String? get() = choices.firstOrNull()?.message?.content
}
