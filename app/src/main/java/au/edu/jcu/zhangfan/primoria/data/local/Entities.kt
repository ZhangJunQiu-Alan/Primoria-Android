package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty

@Entity(
    tableName = "topics",
    indices = [Index(value = ["title_key"], unique = true)],
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Lower-cased title so "Photosynthesis" and "photosynthesis " are the same topic. */
    @ColumnInfo(name = "title_key") val titleKey: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_studied_at") val lastStudiedAt: Long? = null,
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("topic_id")],
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "topic_id") val topicId: Long,
    val prompt: String,
    val options: List<String>,
    @ColumnInfo(name = "correct_index") val correctIndex: Int,
    val explanation: String,
    val difficulty: Difficulty,
    val source: ContentSource,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("question_id"), Index("topic_id"), Index("epoch_day")],
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "question_id") val questionId: Long,
    @ColumnInfo(name = "topic_id") val topicId: Long,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "chosen_index") val chosenIndex: Int,
    @ColumnInfo(name = "is_correct") val isCorrect: Boolean,
    @ColumnInfo(name = "xp_awarded") val xpAwarded: Int,
    @ColumnInfo(name = "answered_at") val answeredAt: Long,
    /** Local calendar day of the answer, stored so daily XP and streaks are cheap to query. */
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
)

@Entity(
    tableName = "topic_mastery",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TopicMasteryEntity(
    @PrimaryKey @ColumnInfo(name = "topic_id") val topicId: Long,
    val mastery: Double,
    val attempts: Int,
    val correct: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
