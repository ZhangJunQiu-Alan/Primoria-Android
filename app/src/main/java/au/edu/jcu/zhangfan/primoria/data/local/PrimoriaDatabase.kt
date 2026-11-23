package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TopicEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        TopicMasteryEntity::class,
        ContentReportEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2 adds content_reports for flagging AI-generated questions.
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
abstract class PrimoriaDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun reportDao(): ReportDao

    companion object {
        const val NAME = "primoria.db"
    }
}
