package au.edu.jcu.zhangfan.primoria.data.repository

import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.local.StoredDataCounts
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.settings.SettingsRepository
import au.edu.jcu.zhangfan.primoria.domain.logic.DataRetention
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Commitment 06 — learner control: see, expire and delete everything Primoria stores. */
interface PrivacyRepository {
    suspend fun storedCounts(): StoredDataCounts
    suspend fun deleteTopic(topicId: Long)
    suspend fun applyRetention(retentionDays: Int): Int
    suspend fun deleteEverything()
}

@Singleton
class DefaultPrivacyRepository @Inject constructor(
    private val database: PrimoriaDatabase,
    private val settingsRepository: SettingsRepository,
    private val keyProvider: ApiKeyProvider,
    private val learningRepository: LearningRepository,
    private val clock: Clock,
    @Named("io") private val ioDispatcher: CoroutineDispatcher,
) : PrivacyRepository {

    override suspend fun storedCounts(): StoredDataCounts = StoredDataCounts(
        topics = database.topicDao().count(),
        questions = database.questionDao().count(),
        attempts = database.attemptDao().count(),
    )

    override suspend fun deleteTopic(topicId: Long) {
        database.topicDao().delete(topicId)
    }

    override suspend fun applyRetention(retentionDays: Int): Int =
        database.attemptDao().deleteOlderThan(DataRetention.cutoffMillis(clock.millis(), retentionDays))

    override suspend fun deleteEverything() {
        withContext(ioDispatcher) { database.clearAllTables() }
        keyProvider.clearUserKey()
        settingsRepository.reset()
        learningRepository.ensureLibrarySeeded()
    }
}
