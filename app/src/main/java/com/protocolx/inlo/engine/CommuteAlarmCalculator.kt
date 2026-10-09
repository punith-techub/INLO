package com.protocolx.inlo.engine

import com.protocolx.inlo.data.model.UserSettings

data class AlarmCalculationResult(
    val alarmHour: Int,
    val alarmMinute: Int,
    val targetHour: Int,
    val targetMinute: Int,
    val isEarlyShift: Boolean,
    val earlyShiftMinutesDelta: Int,
    val totalBufferMinutes: Int,
    val commuteMinutes: Int,
    val prepMinutes: Int
) {
    val formattedAlarmTime: String
        get() {
            val h = if (alarmHour % 12 == 0) 12 else alarmHour % 12
            val amPm = if (alarmHour in 12..23) "PM" else "AM"
            return String.format("%02d:%02d %s", h, alarmMinute, amPm)
        }

    val formattedTargetTime: String
        get() {
            val h = if (targetHour % 12 == 0) 12 else targetHour % 12
            val amPm = if (targetHour in 12..23) "PM" else "AM"
            return String.format("%02d:%02d %s", h, targetMinute, amPm)
        }
}

object CommuteAlarmCalculator {
    fun calculate(
        targetHour: Int,
        targetMinute: Int,
        settings: UserSettings
    ): AlarmCalculationResult {
        val totalBufferMinutes = settings.commuteMinutes + settings.prepMinutes

        val targetTotalMinutes = targetHour * 60 + targetMinute
        var alarmTotalMinutes = targetTotalMinutes - totalBufferMinutes

        // If alarm wraps before midnight (e.g. 11 PM previous night)
        if (alarmTotalMinutes < 0) {
            alarmTotalMinutes += 24 * 60
        }

        val alarmHour = (alarmTotalMinutes / 60) % 24
        val alarmMinute = alarmTotalMinutes % 60

        val typicalTotalMinutes = settings.typicalArrivalHour * 60 + settings.typicalArrivalMinute
        val delta = typicalTotalMinutes - targetTotalMinutes
        val isEarlyShift = delta > 0

        return AlarmCalculationResult(
            alarmHour = alarmHour,
            alarmMinute = alarmMinute,
            targetHour = targetHour,
            targetMinute = targetMinute,
            isEarlyShift = isEarlyShift,
            earlyShiftMinutesDelta = if (isEarlyShift) delta else 0,
            totalBufferMinutes = totalBufferMinutes,
            commuteMinutes = settings.commuteMinutes,
            prepMinutes = settings.prepMinutes
        )
    }
}
