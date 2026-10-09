package com.protocolx.inlo.data.model

data class UserSettings(
    val typicalArrivalHour: Int = 10,       // 10:00 AM
    val typicalArrivalMinute: Int = 0,
    val commuteMinutes: Int = 120,          // 2 hours
    val prepMinutes: Int = 60,              // 1 hour
    val vipKeywords: List<String> = listOf("manager", "boss", "lead", "director", "cto", "vp"),
    val vipContacts: List<String> = listOf("Manager", "Sarah Tech Lead", "Alex VP"),
    val isAutonomousAlarmEnabled: Boolean = true,
    val isAutonomousCalendarEnabled: Boolean = true
) {
    val totalBufferMinutes: Int
        get() = commuteMinutes + prepMinutes

    val typicalArrivalTimeFormatted: String
        get() = String.format("%02d:%02d %s", 
            if (typicalArrivalHour % 12 == 0) 12 else typicalArrivalHour % 12, 
            typicalArrivalMinute, 
            if (typicalArrivalHour >= 12) "PM" else "AM"
        )
}
