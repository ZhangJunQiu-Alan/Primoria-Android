package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty

@Dao
interface QuestionDao {

    @Insert
    suspend fun insertAll(questions: List<QuestionEntity>): List<Long>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<Long>): List<QuestionEntity>

    /**
     * Stored questions for a topic, preferring ones the learner has not yet answered
     * correctly, so offline practice targets gaps instead of repeating easy wins.
     */
    @Query(
        """
        SELECT q.* FROM questions q
        WHERE q.topic_id = :topicId AND q.difficulty = :difficulty
          AND q.id NOT IN (SELECT question_id FROM content_reports)
        ORDER BY (
            SELECT COUNT(*) FROM attempts a WHERE a.question_id = q.id AND a.is_correct = 1
        ) ASC, RANDOM()
        LIMIT :limit
        """
    )
    suspend fun practiceSet(topicId: Long, difficulty: Difficulty, limit: Int): List<QuestionEntity>

    @Query(
        """
        SELECT * FROM questions
        WHERE topic_id = :topicId AND id NOT IN (SELECT question_id FROM content_reports)
        ORDER BY RANDOM() LIMIT :limit
        """
    )
    suspend fun anyForTopic(topicId: Long, limit: Int): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}
