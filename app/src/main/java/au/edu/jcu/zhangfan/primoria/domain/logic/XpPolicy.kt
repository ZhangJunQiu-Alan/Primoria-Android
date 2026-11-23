package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty

/**
 * Commitment 05 — reward learning, not screen time.
 *
 * XP is earned only for a correct first answer to a question, never for time spent,
 * app opens or retries, and it stops at a daily cap so grinding has no payoff.
 */
object XpPolicy {

    const val DAILY_CAP = 150

    fun award(
        correct: Boolean,
        firstAttempt: Boolean,
        difficulty: Difficulty,
        xpEarnedToday: Int,
        dailyCap: Int = DAILY_CAP,
    ): Int {
        if (!correct || !firstAttempt) return 0
        val remaining = (dailyCap - xpEarnedToday).coerceAtLeast(0)
        return minOf(difficulty.baseXp, remaining)
    }
}
