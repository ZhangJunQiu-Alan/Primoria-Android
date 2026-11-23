package au.edu.jcu.zhangfan.primoria.data.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

/** Short answer tones; behind an interface so ViewModels stay testable on the JVM. */
interface SoundEffects {
    fun playAnswer(correct: Boolean)
}

@Singleton
class ToneSoundEffects @Inject constructor() : SoundEffects {

    private val tones: ToneGenerator? by lazy {
        runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME) }.getOrNull()
    }

    override fun playAnswer(correct: Boolean) {
        val tone = if (correct) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK
        tones?.startTone(tone, DURATION_MS)
    }

    private companion object {
        const val VOLUME = 60
        const val DURATION_MS = 150
    }
}
