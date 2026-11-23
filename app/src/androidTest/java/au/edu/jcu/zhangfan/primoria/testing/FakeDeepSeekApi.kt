package au.edu.jcu.zhangfan.primoria.testing

import au.edu.jcu.zhangfan.primoria.data.remote.ChatMessage
import au.edu.jcu.zhangfan.primoria.data.remote.ChatRequest
import au.edu.jcu.zhangfan.primoria.data.remote.ChatResponse
import au.edu.jcu.zhangfan.primoria.data.remote.DeepSeekApi
import java.io.IOException

/** Scriptable stand-in for DeepSeek: returns [reply], or throws when [failWith] is set. */
class FakeDeepSeekApi(
    var reply: String = quizJson(3),
    var failWith: IOException? = null,
) : DeepSeekApi {
    val requests = mutableListOf<ChatRequest>()

    override suspend fun chat(request: ChatRequest): ChatResponse {
        requests += request
        failWith?.let { throw it }
        return ChatResponse(listOf(ChatResponse.Choice(ChatMessage("assistant", reply))))
    }

    companion object {
        fun quizJson(count: Int): String {
            val questions = (1..count).joinToString(",") { i ->
                """{"prompt":"AI question $i","options":["Right $i","Wrong a$i","Wrong b$i","Wrong c$i"],"correctIndex":0,"explanation":"Because $i."}"""
            }
            return """{"refused":false,"reason":"","questions":[$questions]}"""
        }

        const val REFUSAL = """{"refused":true,"reason":"Let's study something safer, like chemistry.","questions":[]}"""
    }
}
