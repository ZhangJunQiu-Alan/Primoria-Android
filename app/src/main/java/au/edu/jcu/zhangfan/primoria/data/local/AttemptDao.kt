package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {

    @Insert
    suspend fun insert(attempt: AttemptEntity): Long

    @Query("SELECT COUNT(*) FROM attempts WHERE question_id = :questionId")
    suspend fun countForQuestion(questionId: Long): Int

    @Query("SELECT COALESCE(SUM(xp_awarded), 0) FROM attempts WHERE epoch_day = :epochDay")
    suspend fun xpOnDay(epochDay: Long): Int

    @Query("SELECT COALESCE(SUM(xp_awarded), 0) FROM attempts")
    fun observeTotalXp(): Flow<Int>

    @Query(
        """
        SELECT epoch_day,
               COUNT(*) AS answers,
               SUM(CASE WHEN is_correct THEN 1 ELSE 0 END) AS correct_answers,
               COALESCE(SUM(xp_awarded), 0) AS xp
        FROM attempts
        WHERE epoch_day >= :fromEpochDay
        GROUP BY epoch_day
        ORDER BY epoch_day
        """
    )
    fun observeDailyActivity(fromEpochDay: Long): Flow<List<DailyActivityRow>>

    @Query("SELECT DISTINCT epoch_day FROM attempts ORDER BY epoch_day")
    fun observeActiveDays(): Flow<List<Long>>

    @Query("SELECT * FROM topic_mastery WHERE topic_id = :topicId")
    suspend fun mastery(topicId: Long): TopicMasteryEntity?

    @Upsert
    suspend fun upsertMastery(mastery: TopicMasteryEntity)

    @Query("DELETE FROM attempts WHERE answered_at < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long): Int

    @Query("SELECT COUNT(*) FROM attempts")
    suspend fun count(): Int
}
