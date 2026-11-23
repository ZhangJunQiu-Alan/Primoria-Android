package au.edu.jcu.zhangfan.primoria.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.ThemeMode
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun update(transform: (UserSettings) -> UserSettings)
    suspend fun reset()
}

/** Persists [UserSettings] with Preferences DataStore. */
@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data.map(::toSettings)

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { prefs -> write(prefs, transform(toSettings(prefs))) }
    }

    override suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private fun toSettings(prefs: Preferences): UserSettings {
        val defaults = UserSettings()
        return UserSettings(
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: defaults.onboardingComplete,
            ageGroup = enumOrDefault(prefs[Keys.AGE_GROUP], defaults.ageGroup),
            aiConsent = prefs[Keys.AI_CONSENT] ?: defaults.aiConsent,
            difficulty = enumOrDefault(prefs[Keys.DIFFICULTY], defaults.difficulty),
            questionsPerQuiz = prefs[Keys.QUESTIONS_PER_QUIZ] ?: defaults.questionsPerQuiz,
            soundEffects = prefs[Keys.SOUND] ?: defaults.soundEffects,
            haptics = prefs[Keys.HAPTICS] ?: defaults.haptics,
            weeklyGoalDays = prefs[Keys.WEEKLY_GOAL] ?: defaults.weeklyGoalDays,
            quietHoursStart = prefs[Keys.QUIET_START] ?: defaults.quietHoursStart,
            quietHoursEnd = prefs[Keys.QUIET_END] ?: defaults.quietHoursEnd,
            retentionDays = prefs[Keys.RETENTION_DAYS] ?: defaults.retentionDays,
            themeMode = enumOrDefault(prefs[Keys.THEME], defaults.themeMode),
            largeText = prefs[Keys.LARGE_TEXT] ?: defaults.largeText,
        )
    }

    private fun write(prefs: MutablePreferences, s: UserSettings) {
        prefs[Keys.ONBOARDING_COMPLETE] = s.onboardingComplete
        prefs[Keys.AGE_GROUP] = s.ageGroup.name
        prefs[Keys.AI_CONSENT] = s.aiConsent
        prefs[Keys.DIFFICULTY] = s.difficulty.name
        prefs[Keys.QUESTIONS_PER_QUIZ] = s.questionsPerQuiz
        prefs[Keys.SOUND] = s.soundEffects
        prefs[Keys.HAPTICS] = s.haptics
        prefs[Keys.WEEKLY_GOAL] = s.weeklyGoalDays
        prefs[Keys.QUIET_START] = s.quietHoursStart
        prefs[Keys.QUIET_END] = s.quietHoursEnd
        prefs[Keys.RETENTION_DAYS] = s.retentionDays
        prefs[Keys.THEME] = s.themeMode.name
        prefs[Keys.LARGE_TEXT] = s.largeText
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val AGE_GROUP = stringPreferencesKey("age_group")
        val AI_CONSENT = booleanPreferencesKey("ai_consent")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val QUESTIONS_PER_QUIZ = intPreferencesKey("questions_per_quiz")
        val SOUND = booleanPreferencesKey("sound_effects")
        val HAPTICS = booleanPreferencesKey("haptics")
        val WEEKLY_GOAL = intPreferencesKey("weekly_goal_days")
        val QUIET_START = intPreferencesKey("quiet_hours_start")
        val QUIET_END = intPreferencesKey("quiet_hours_end")
        val RETENTION_DAYS = intPreferencesKey("retention_days")
        val THEME = stringPreferencesKey("theme_mode")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
    }
}
