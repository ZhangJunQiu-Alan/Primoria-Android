package au.edu.jcu.zhangfan.primoria.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import au.edu.jcu.zhangfan.primoria.BuildConfig
import au.edu.jcu.zhangfan.primoria.data.local.PrimoriaDatabase
import au.edu.jcu.zhangfan.primoria.data.remote.ApiKeyProvider
import au.edu.jcu.zhangfan.primoria.data.remote.AuthInterceptor
import au.edu.jcu.zhangfan.primoria.data.remote.DeepSeekApi
import au.edu.jcu.zhangfan.primoria.data.remote.KeystoreApiKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Named("io")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PrimoriaDatabase =
        Room.databaseBuilder(context, PrimoriaDatabase::class.java, PrimoriaDatabase.NAME).build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideApiKeyProvider(provider: KeystoreApiKeyProvider): ApiKeyProvider = provider

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        // Without this, defaults such as response_format = json_object are silently left out of requests.
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        // Generating a full quiz can take a while on DeepSeek's side.
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideDeepSeekApi(client: OkHttpClient, json: Json): DeepSeekApi = Retrofit.Builder()
        .baseUrl(BuildConfig.DEEPSEEK_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(DeepSeekApi::class.java)

    @Provides
    @Named("deepseekModel")
    fun provideModel(): String = BuildConfig.DEEPSEEK_MODEL
}
