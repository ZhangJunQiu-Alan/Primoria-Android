package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.TypeConverter
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {
    private val stringList = ListSerializer(String.serializer())

    @TypeConverter
    fun optionsToJson(options: List<String>): String = Json.encodeToString(stringList, options)

    @TypeConverter
    fun jsonToOptions(json: String): List<String> = Json.decodeFromString(stringList, json)

    @TypeConverter
    fun difficultyToName(difficulty: Difficulty): String = difficulty.name

    @TypeConverter
    fun nameToDifficulty(name: String): Difficulty = Difficulty.valueOf(name)

    @TypeConverter
    fun sourceToName(source: ContentSource): String = source.name

    @TypeConverter
    fun nameToSource(name: String): ContentSource = ContentSource.valueOf(name)
}
