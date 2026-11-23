package au.edu.jcu.zhangfan.primoria.data.repository

import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakCalculator
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary
import au.edu.jcu.zhangfan.primoria.domain.logic.XpPolicy
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class DayActivity(val date: LocalDate, val answers: Int, val correct: Int, val xp: Int)

data class LearningStats(
    val totalXp: Int,
    val xpToday: Int,
    val dailyXpCap: Int,
    val streak: StreakSummary,
    /** The last seven days, oldest first, including days with no activity. */
    val week: List<DayActivity>,
    val topics: List<TopicSummary>,
) {
    val studiedTopics: List<TopicSummary> get() = topics.filter { it.attempts > 0 }
    val totalAnswers: Int get() = studiedTopics.sumOf { it.attempts }
    val accuracy: Double get() =
        if (totalAnswers == 0) 0.0 else studiedTopics.sumOf { it.correct }.toDouble() / totalAnswers
}

interface ProgressRepository {
    fun observeStats(): Flow<LearningStats>
}

@Singleton
class DefaultProgressRepository @Inject constructor(
    database: PrimoriaDatabase,
    private val learningRepository: LearningRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ProgressRepository {

    private val attemptDao = database.attemptDao()

    override fun observeStats(): Flow<LearningStats> {
        val today = LocalDate.now(clock)
        val weekStart = today.minusDays(6)
        return combine(
            attemptDao.observeTotalXp(),
            attemptDao.observeDailyActivity(weekStart.toEpochDay()),
            attemptDao.observeActiveDays(),
            learningRepository.observeTopics(),
            settingsRepository.settings,
        ) { totalXp, daily, activeDays, topics, settings ->
            val byDay = daily.associateBy { it.epochDay }
            val week = (0L..6L).map { offset ->
                val date = weekStart.plusDays(offset)
                val row = byDay[date.toEpochDay()]
                DayActivity(date, row?.answers ?: 0, row?.correctAnswers ?: 0, row?.xp ?: 0)
            }
            LearningStats(
                totalXp = totalXp,
                xpToday = week.last().xp,
                dailyXpCap = XpPolicy.DAILY_CAP,
                streak = StreakCalculator.calculate(
                    activeDays = activeDays.map(LocalDate::ofEpochDay).toSet(),
                    today = today,
                    weeklyGoal = settings.weeklyGoalDays,
                ),
                week = week,
                topics = topics,
            )
        }
    }
}
