package au.edu.jcu.zhangfan.primoria.data.remote

import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty

/**
 * Builds the only message Primoria ever sends to DeepSeek.
 *
 * By design it receives the topic, difficulty, question count and age band — and
 * nothing else. Scores, history, mastery and settings stay on the device.
 */
object QuizPrompt {

    fun messages(
        topic: String,
        difficulty: Difficulty,
        count: Int,
        ageGroup: AgeGroup,
    ): List<ChatMessage> = listOf(
        ChatMessage(role = "system", content = systemPrompt(ageGroup)),
        ChatMessage(
            role = "user",
            content = "Topic: $topic\nDifficulty: ${difficulty.label} (${difficultyGuide(difficulty)})\nNumber of questions: $count",
        ),
    )

    private fun systemPrompt(ageGroup: AgeGroup): String {
        val audience = when (ageGroup) {
            AgeGroup.TEEN -> "secondary-school learners aged 13 to 17"
            AgeGroup.ADULT -> "learners aged 18 or over"
        }
        return """
            You write multiple-choice practice questions for $audience.
            Rules:
            - Every question tests understanding of the topic, not trivia about people's private lives.
            - Exactly 4 distinct options per question and exactly one correct answer.
            - The explanation says why the correct option is right in at most 2 sentences.
            - Use clear, neutral, age-appropriate language. Do not invent statistics or quotes.
            - If the topic is unsafe or not appropriate for this audience, or is not something
              that can be studied, do not write questions; set "refused" to true and give a short,
              kind reason that suggests a related topic that would be suitable.
            Reply with JSON only, in this shape:
            {"refused": false, "reason": "", "questions": [{"prompt": "...", "options": ["...", "...", "...", "..."], "correctIndex": 0, "explanation": "..."}]}
        """.trimIndent()
    }

    private fun difficultyGuide(difficulty: Difficulty): String = when (difficulty) {
        Difficulty.FOUNDATION -> "recall of key facts and definitions"
        Difficulty.STANDARD -> "applying ideas to familiar examples"
        Difficulty.CHALLENGE -> "multi-step reasoning and unfamiliar situations"
    }
}
