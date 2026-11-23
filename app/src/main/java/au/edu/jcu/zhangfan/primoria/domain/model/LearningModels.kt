package au.edu.jcu.zhangfan.primoria.domain.model

/** How demanding generated questions should be. */
enum class Difficulty(val label: String, val baseXp: Int) {
    FOUNDATION("Foundation", 5),
    STANDARD("Standard", 10),
    CHALLENGE("Challenge", 15),
}

/** Where a question came from; shown to the learner so AI content is never disguised. */
enum class ContentSource { AI_GENERATED, OFFLINE_LIBRARY }

/** Age band chosen at onboarding. It changes defaults, never what data is collected. */
enum class AgeGroup(val label: String) {
    TEEN("13–17"),
    ADULT("18+"),
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class QuizQuestion(
    val id: Long,
    val topicId: Long,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: Difficulty,
    val source: ContentSource,
)

/** A question as produced by DeepSeek or the offline library, before it is stored. */
data class QuestionDraft(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

data class TopicSummary(
    val id: Long,
    val title: String,
    val mastery: Double,
    val attempts: Int,
    val correct: Int,
    val lastStudiedAt: Long?,
) {
    val masteryLevel: MasteryLevel get() = MasteryLevel.of(mastery, attempts)
    val accuracy: Double get() = if (attempts == 0) 0.0 else correct.toDouble() / attempts
}

enum class MasteryLevel(val label: String) {
    NEW("New"),
    DEVELOPING("Developing"),
    SECURE("Secure"),
    MASTERED("Mastered");

    companion object {
        /** Mastered needs both a high estimate and enough evidence, so one lucky quiz is not enough. */
        fun of(mastery: Double, attempts: Int): MasteryLevel = when {
            attempts == 0 -> NEW
            mastery >= 0.85 && attempts >= 8 -> MASTERED
            mastery >= 0.6 -> SECURE
            else -> DEVELOPING
        }
    }
}

data class UserSettings(
    val onboardingComplete: Boolean = false,
    val ageGroup: AgeGroup = AgeGroup.TEEN,
    val aiConsent: Boolean = false,
    val difficulty: Difficulty = Difficulty.STANDARD,
    val questionsPerQuiz: Int = 5,
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    val weeklyGoalDays: Int = 3,
    val quietHoursStart: Int = 21,
    val quietHoursEnd: Int = 7,
    val retentionDays: Int = 90,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val largeText: Boolean = false,
)
