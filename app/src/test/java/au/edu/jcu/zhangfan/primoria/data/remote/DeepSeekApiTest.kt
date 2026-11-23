package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.di.NetworkModule
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.testing.FakeApiKeyProvider
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class DeepSeekApiTest {
    @get:Rule
    val server = MockWebServer()

    private val json = NetworkModule.provideJson()

    @Test
    fun `quiz generation posts with bearer auth and preserves the shuffled correct answer`() = runTest {
        val dataSource = dataSource(FakeApiKeyProvider("test-key"))
        enqueueChatResponse(QUESTION_PAYLOAD)

        val result = dataSource.generate("Volcanoes", Difficulty.STANDARD, 1, AgeGroup.ADULT)

        val request = recordedRequest()
        assertEquals("POST", request.method)
        assertEquals("/chat/completions", request.path)
        assertEquals("Bearer test-key", request.getHeader("Authorization"))
        val draft = (result as ParsedQuiz.Questions).drafts.single()
        assertEquals("What is molten rock below Earth's surface called?", draft.prompt)
        assertEquals(setOf("Lava", "Magma", "Ash", "Pumice"), draft.options.toSet())
        assertEquals(4, draft.options.size)
        assertEquals("Magma", draft.options.getOrNull(draft.correctIndex))
    }

    @Test
    fun `quiz request asks for json object response format`() = runTest {
        val dataSource = dataSource(FakeApiKeyProvider("test-key"))
        enqueueChatResponse(QUESTION_PAYLOAD)

        dataSource.generate("Volcanoes", Difficulty.STANDARD, 1, AgeGroup.ADULT)

        val body = json.parseToJsonElement(recordedRequest().body.readUtf8()).jsonObject
        assertEquals("json_object", body["response_format"]?.jsonObject?.get("type")?.jsonPrimitive?.content)
    }

    @Test
    fun `refusal payload returns Refused with its reason`() = runTest {
        val dataSource = dataSource(FakeApiKeyProvider("test-key"))
        enqueueChatResponse("""{"refused":true,"reason":"Try a related school subject."}""")

        val result = dataSource.generate("Volcanoes", Difficulty.STANDARD, 1, AgeGroup.TEEN)

        assertEquals(ParsedQuiz.Refused("Try a related school subject."), result)
    }

    @Test
    fun `request has no authorization header when the key is null`() = runTest {
        val dataSource = dataSource(FakeApiKeyProvider())
        enqueueChatResponse(QUESTION_PAYLOAD)

        dataSource.generate("Volcanoes", Difficulty.STANDARD, 1, AgeGroup.ADULT)

        assertNull(recordedRequest().getHeader("Authorization"))
    }

    private fun dataSource(keyProvider: FakeApiKeyProvider): QuizRemoteDataSource {
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(NetworkModule.provideOkHttp(AuthInterceptor(keyProvider)))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DeepSeekApi::class.java)
        return QuizRemoteDataSource(api, model = "test-model")
    }

    private fun enqueueChatResponse(content: String) {
        val body = buildJsonObject {
            put("id", "test-completion")
            putJsonArray("choices") {
                add(buildJsonObject {
                    putJsonObject("message") {
                        put("role", "assistant")
                        put("content", content)
                    }
                })
            }
        }
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body.toString()),
        )
    }

    private fun recordedRequest(): RecordedRequest =
        checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "Expected a request to DeepSeek" }

    private companion object {
        val QUESTION_PAYLOAD = """
            {
                "refused": false,
                "questions": [{
                    "prompt": "What is molten rock below Earth's surface called?",
                    "options": ["Lava", "Magma", "Ash", "Pumice"],
                    "correctIndex": 1,
                    "explanation": "Molten rock below the surface is called magma."
                }]
            }
        """.trimIndent()
    }
}
