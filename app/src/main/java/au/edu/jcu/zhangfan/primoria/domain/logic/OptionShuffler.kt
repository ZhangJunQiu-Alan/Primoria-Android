package au.edu.jcu.zhangfan.primoria.domain.logic

import au.edu.jcu.zhangfan.primoria.domain.model.QuestionDraft
import kotlin.random.Random

/**
 * Language models tend to put the right answer first (DeepSeek did so for every question
 * in testing), which lets learners score by position instead of understanding. Options are
 * shuffled before storage and the answer index is moved with them.
 */
object OptionShuffler {

    fun shuffle(draft: QuestionDraft, random: Random = Random.Default): QuestionDraft {
        val order = draft.options.indices.shuffled(random)
        return draft.copy(
            options = order.map { draft.options[it] },
            correctIndex = order.indexOf(draft.correctIndex),
        )
    }
}
