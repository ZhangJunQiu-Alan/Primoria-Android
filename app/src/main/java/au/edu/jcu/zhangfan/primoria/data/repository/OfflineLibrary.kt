package au.edu.jcu.zhangfan.primoria.data.repository

import android.content.Context
import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class LibraryTopic(val title: String, val questions: List<QuestionDraft>)

/** Hand-checked questions bundled with the app so it is fully usable without a network or key. */
interface OfflineLibrary {
    fun topics(): List<LibraryTopic>
}

@Singleton
class AssetOfflineLibrary @Inject constructor(
    @ApplicationContext private val context: Context,
) : OfflineLibrary {

    @Serializable
    private data class LibraryFile(val topics: List<TopicJson>)

    @Serializable
    private data class TopicJson(val title: String, val questions: List<QuestionJson>)

    @Serializable
    private data class QuestionJson(
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String,
    )

    private val topics: List<LibraryTopic> by lazy {
        val text = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        Json.decodeFromString(LibraryFile.serializer(), text).topics.map { topic ->
            LibraryTopic(
                title = topic.title,
                questions = topic.questions.map { QuestionDraft(it.prompt, it.options, it.correctIndex, it.explanation) },
            )
        }
    }

    override fun topics(): List<LibraryTopic> = topics

    private companion object {
        const val ASSET = "offline_library.json"
    }
}
