package au.edu.jcu.zhangfan.primoria.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import au.edu.jcu.zhangfan.primoria.MainActivity
import au.edu.jcu.zhangfan.primoria.R
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.logic.ReminderPolicy
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime

/** Daily check that posts a study reminder only when [ReminderPolicy] allows it. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun settingsRepository(): SettingsRepository
        fun progressRepository(): ProgressRepository
        fun clock(): Clock
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val settings = deps.settingsRepository().settings.first()
        val stats = deps.progressRepository().observeStats().first()
        val now = LocalDateTime.now(deps.clock())

        val send = ReminderPolicy.shouldNotify(
            now = now,
            enabled = settings.reminderEnabled,
            quietStartHour = settings.quietHoursStart,
            quietEndHour = settings.quietHoursEnd,
            lastReminderEpochDay = settings.lastReminderEpochDay,
            studiedToday = stats.week.last().answers > 0,
            weeklyGoalMet = stats.streak.goalMetThisWeek,
        )
        if (!send || !canNotify()) return Result.success()

        postNotification(ReminderPolicy.message(stats.streak.activeDaysThisWeek, stats.streak.weeklyGoal))
        val today = now.toLocalDate().toEpochDay()
        deps.settingsRepository().update { it.copy(lastReminderEpochDay = today) }
        return Result.success()
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission") // Checked in canNotify().
    private fun postNotification(text: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Study reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "At most one gentle reminder a day, only if you turned it on."
            },
        )
        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Primoria")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "study_reminders"
        const val NOTIFICATION_ID = 1
    }
}
