package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Query(
        """
        SELECT t.id, t.title, t.last_studied_at, m.mastery, m.attempts, m.correct
        FROM topics t LEFT JOIN topic_mastery m ON m.topic_id = t.id
        ORDER BY COALESCE(t.last_studied_at, t.created_at) DESC
        """
    )
    fun observeTopicsWithMastery(): Flow<List<TopicWithMasteryRow>>

    @Query("SELECT * FROM topics WHERE title_key = :titleKey LIMIT 1")
    suspend fun findByKey(titleKey: String): TopicEntity?

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun findById(id: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(topic: TopicEntity): Long

    /** Returns the existing topic for [titleKey] or creates it, atomically. */
    @Transaction
    suspend fun getOrCreate(title: String, titleKey: String, now: Long): TopicEntity {
        findByKey(titleKey)?.let { return it }
        val id = insert(TopicEntity(title = title, titleKey = titleKey, createdAt = now))
        return TopicEntity(id = id, title = title, titleKey = titleKey, createdAt = now)
    }

    @Query("UPDATE topics SET last_studied_at = :time WHERE id = :topicId")
    suspend fun markStudied(topicId: Long, time: Long)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun delete(topicId: Long)

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun count(): Int
}
