package au.edu.jcu.zhangfan.primoria.domain.logic

import java.time.LocalTime

/** Quiet hours may wrap past midnight, e.g. 21:00 → 07:00. */
object QuietHours {

    fun contains(time: LocalTime, startHour: Int, endHour: Int): Boolean {
        val hour = time.hour
        return when {
            startHour == endHour -> false
            startHour < endHour -> hour in startHour until endHour
            else -> hour >= startHour || hour < endHour
        }
    }
}
