package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty

/**
 * Estimates how well a learner knows a topic with an exponential moving average.
 *
 * Harder questions move the estimate further, and a wrong answer on a hard question
 * costs less than one on an easy question, so learners are not punished for stretching.
 */
object MasteryCalculator {

    fun update(current: Double, correct: Boolean, difficulty: Difficulty): Double {
        val rate = when (difficulty) {
            Difficulty.FOUNDATION -> if (correct) 0.15 else 0.30
            Difficulty.STANDARD -> if (correct) 0.25 else 0.25
            Difficulty.CHALLENGE -> if (correct) 0.35 else 0.15
        }
        val target = if (correct) 1.0 else 0.0
        val next = current + rate * (target - current)
        return next.coerceIn(0.0, 1.0)
    }
}
