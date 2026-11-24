package au.edu.jcu.zhangfan.primoria.di

import au.edu.jcu.zhangfan.primoria.data.feedback.SoundEffects
import au.edu.jcu.zhangfan.primoria.data.feedback.ToneSoundEffects
import au.edu.jcu.zhangfan.primoria.data.reminder.ReminderScheduler
import au.edu.jcu.zhangfan.primoria.data.reminder.WorkManagerReminderScheduler
import au.edu.jcu.zhangfan.primoria.data.repository.AssetOfflineLibrary
import au.edu.jcu.zhangfan.primoria.data.repository.DefaultLearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.DefaultPrivacyRepository
import au.edu.jcu.zhangfan.primoria.data.repository.DefaultProgressRepository
import au.edu.jcu.zhangfan.primoria.data.repository.LearningRepository
import au.edu.jcu.zhangfan.primoria.data.repository.OfflineLibrary
import au.edu.jcu.zhangfan.primoria.data.repository.PrivacyRepository
import au.edu.jcu.zhangfan.primoria.data.repository.ProgressRepository
import au.edu.jcu.zhangfan.primoria.data.settings.DataStoreSettingsRepository
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds interfaces to implementations so ViewModels depend on abstractions and tests can swap fakes in. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindLearning(impl: DefaultLearningRepository): LearningRepository
    @Binds abstract fun bindProgress(impl: DefaultProgressRepository): ProgressRepository
    @Binds abstract fun bindPrivacy(impl: DefaultPrivacyRepository): PrivacyRepository
    @Binds abstract fun bindSettings(impl: DataStoreSettingsRepository): SettingsRepository
    @Binds abstract fun bindOfflineLibrary(impl: AssetOfflineLibrary): OfflineLibrary
    @Binds abstract fun bindSoundEffects(impl: ToneSoundEffects): SoundEffects
    @Binds abstract fun bindReminderScheduler(impl: WorkManagerReminderScheduler): ReminderScheduler
}
