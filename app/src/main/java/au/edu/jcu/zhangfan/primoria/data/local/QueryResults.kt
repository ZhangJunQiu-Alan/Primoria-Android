package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.ColumnInfo

/** Topic joined with its mastery row (absent until the first answer). */
data class TopicWithMasteryRow(
    val id: Long,
    val title: String,
    @ColumnInfo(name = "last_studied_at") val lastStudiedAt: Long?,
    val mastery: Double?,
    val attempts: Int?,
    val correct: Int?,
)

data class DailyActivityRow(
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val answers: Int,
    @ColumnInfo(name = "correct_answers") val correctAnswers: Int,
    val xp: Int,
)

data class StoredDataCounts(
    val topics: Int,
    val questions: Int,
    val attempts: Int,
    val reports: Int,
)
