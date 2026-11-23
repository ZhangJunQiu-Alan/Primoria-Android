package au.edu.jcu.zhangfan.primoria.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrimoriaDaoTest {

    private lateinit var db: PrimoriaDatabase
    private lateinit var topicDao: TopicDao
    private lateinit var questionDao: QuestionDao
    private lateinit var attemptDao: AttemptDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PrimoriaDatabase::class.java).build()
        topicDao = db.topicDao()
        questionDao = db.questionDao()
        attemptDao = db.attemptDao()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun question(topicId: Long, prompt: String = "Q") = questionDao.insertAll(
        listOf(
            QuestionEntity(
                topicId = topicId,
                prompt = prompt,
                options = listOf("a", "b", "c", "d"),
                correctIndex = 0,
                explanation = "e",
                difficulty = Difficulty.STANDARD,
                source = ContentSource.AI_GENERATED,
                createdAt = 0,
            ),
        ),
    ).single()

    private fun attempt(questionId: Long, topicId: Long, correct: Boolean, xp: Int, day: Long, at: Long = day * 86_400_000) =
        AttemptEntity(
            questionId = questionId,
            topicId = topicId,
            sessionId = "s",
            chosenIndex = 0,
            isCorrect = correct,
            xpAwarded = xp,
            answeredAt = at,
            epochDay = day,
        )

    @Test
    fun getOrCreate_returnsTheSameTopicForTheSameKey() = runTest {
        val first = topicDao.getOrCreate("Photosynthesis", "photosynthesis", 1)
        val second = topicDao.getOrCreate("photosynthesis", "photosynthesis", 2)

        assertEquals(first.id, second.id)
        assertEquals(1, topicDao.count())
    }

    @Test
    fun options_roundTripThroughTypeConverter() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val id = question(topic.id)

        assertEquals(listOf("a", "b", "c", "d"), questionDao.findByIds(listOf(id)).single().options)
    }

    @Test
    fun deletingTopic_cascadesToQuestionsAttemptsAndMastery() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val q = question(topic.id)
        attemptDao.insert(attempt(q, topic.id, correct = true, xp = 10, day = 1))
        attemptDao.upsertMastery(TopicMasteryEntity(topic.id, 0.5, 1, 1, 0))

        topicDao.delete(topic.id)

        assertEquals(0, questionDao.count())
        assertEquals(0, attemptDao.count())
        assertNull(attemptDao.mastery(topic.id))
    }

    @Test
    fun practiceSet_excludesReportedQuestions() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val kept = question(topic.id, "kept")
        val reported = question(topic.id, "reported")
        db.reportDao().insert(ContentReportEntity(questionId = reported, reason = "wrong", createdAt = 0))

        val set = questionDao.practiceSet(topic.id, Difficulty.STANDARD, limit = 10)

        assertEquals(listOf(kept), set.map { it.id })
    }

    @Test
    fun practiceSet_putsQuestionsNotYetAnsweredCorrectlyFirst() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val mastered = question(topic.id, "mastered")
        val fresh = question(topic.id, "fresh")
        attemptDao.insert(attempt(mastered, topic.id, correct = true, xp = 10, day = 1))

        val set = questionDao.practiceSet(topic.id, Difficulty.STANDARD, limit = 1)

        assertEquals(fresh, set.single().id)
    }

    @Test
    fun dailyActivity_groupsAnswersAndXpByDay() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val q = question(topic.id)
        attemptDao.insert(attempt(q, topic.id, correct = true, xp = 10, day = 5))
        attemptDao.insert(attempt(q, topic.id, correct = false, xp = 0, day = 5))
        attemptDao.insert(attempt(q, topic.id, correct = true, xp = 15, day = 6))

        val rows = attemptDao.observeDailyActivity(fromEpochDay = 5).first()

        assertEquals(listOf(DailyActivityRow(5, 2, 1, 10), DailyActivityRow(6, 1, 1, 15)), rows)
        assertEquals(10, attemptDao.xpOnDay(5))
        assertEquals(25, attemptDao.observeTotalXp().first())
    }

    @Test
    fun deleteOlderThan_removesOnlyExpiredAnswers() = runTest {
        val topic = topicDao.getOrCreate("T", "t", 0)
        val q = question(topic.id)
        attemptDao.insert(attempt(q, topic.id, correct = true, xp = 10, day = 1, at = 1_000))
        attemptDao.insert(attempt(q, topic.id, correct = true, xp = 10, day = 2, at = 5_000))

        val removed = attemptDao.deleteOlderThan(cutoffMillis = 2_000)

        assertEquals(1, removed)
        assertEquals(1, attemptDao.count())
    }

    @Test
    fun topicsWithMastery_includeTopicsThatHaveNoAnswersYet() = runTest {
        val studied = topicDao.getOrCreate("Studied", "studied", 0)
        topicDao.getOrCreate("New", "new", 0)
        attemptDao.upsertMastery(TopicMasteryEntity(studied.id, 0.7, 4, 3, 0))

        val rows = topicDao.observeTopicsWithMastery().first().associateBy { it.title }

        assertEquals(0.7, rows.getValue("Studied").mastery!!, 1e-9)
        assertNull(rows.getValue("New").mastery)
        assertTrue(rows.size == 2)
    }
}
