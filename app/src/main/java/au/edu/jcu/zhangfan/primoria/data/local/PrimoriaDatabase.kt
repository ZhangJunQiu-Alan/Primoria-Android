package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TopicEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        TopicMasteryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PrimoriaDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao

    companion object {
        const val NAME = "primoria.db"
    }
}
