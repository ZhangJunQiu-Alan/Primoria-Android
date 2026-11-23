package au.edu.jcu.zhangfan.primoria.domain.logic

import java.util.concurrent.TimeUnit

/** Commitment 01 — learner history expires instead of being kept forever. */
object DataRetention {

    val OPTIONS_DAYS = listOf(30, 90, 365)

    fun cutoffMillis(nowMillis: Long, retentionDays: Int): Long =
        nowMillis - TimeUnit.DAYS.toMillis(retentionDays.coerceAtLeast(1).toLong())
}
