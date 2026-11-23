package au.edu.jcu.zhangfan.primoria.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.remote.DeepSeekApi
import au.edu.jcu.zhangfan.primoria.testing.FakeDeepSeekApi
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.time.Clock
import java.util.UUID
import javax.inject.Named
import javax.inject.Singleton

/** Swaps the real database and settings file for throwaway ones, so UI tests never touch real data. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
object TestAppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Named("io")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PrimoriaDatabase =
        Room.inMemoryDatabaseBuilder(context, PrimoriaDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            File(context.cacheDir, "test_settings_${UUID.randomUUID()}.preferences_pb")
        }
}

/** Replaces Retrofit with a fake DeepSeek so tests are fast, offline and deterministic. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideFakeApi(): FakeDeepSeekApi = FakeDeepSeekApi()

    @Provides
    fun provideApi(fake: FakeDeepSeekApi): DeepSeekApi = fake

    @Provides
    @Named("deepseekModel")
    fun provideModel(): String = "test-model"
}
