package com.protocolx.inlo.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.protocolx.inlo.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("INLO_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): UserSettings {
        return _settingsFlow.value
    }

    private fun loadSettings(): UserSettings {
        val arrivalHour = prefs.getInt(KEY_ARRIVAL_HOUR, 10)
        val arrivalMinute = prefs.getInt(KEY_ARRIVAL_MINUTE, 0)
        val commuteMin = prefs.getInt(KEY_COMMUTE_MIN, 120) // 2 hours
        val prepMin = prefs.getInt(KEY_PREP_MIN, 60)         // 1 hour
        val vipKeywordsString = prefs.getString(KEY_VIP_KEYWORDS, "manager,boss,lead,director,cto,vp") ?: "manager,boss,lead"
        val vipContactsString = prefs.getString(KEY_VIP_CONTACTS, "Manager,Sarah Tech Lead,Alex VP") ?: "Manager"
        val autoAlarm = prefs.getBoolean(KEY_AUTO_ALARM, true)
        val autoCal = prefs.getBoolean(KEY_AUTO_CALENDAR, true)

        val vipKeywords = vipKeywordsString.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val vipContacts = vipContactsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return UserSettings(
            typicalArrivalHour = arrivalHour,
            typicalArrivalMinute = arrivalMinute,
            commuteMinutes = commuteMin,
            prepMinutes = prepMin,
            vipKeywords = vipKeywords,
            vipContacts = vipContacts,
            isAutonomousAlarmEnabled = autoAlarm,
            isAutonomousCalendarEnabled = autoCal
        )
    }

    fun updateSettings(
        arrivalHour: Int,
        arrivalMinute: Int,
        commuteMin: Int,
        prepMin: Int,
        vipKeywords: List<String>,
        vipContacts: List<String>,
        autoAlarm: Boolean,
        autoCal: Boolean
    ) {
        prefs.edit()
            .putInt(KEY_ARRIVAL_HOUR, arrivalHour)
            .putInt(KEY_ARRIVAL_MINUTE, arrivalMinute)
            .putInt(KEY_COMMUTE_MIN, commuteMin)
            .putInt(KEY_PREP_MIN, prepMin)
            .putString(KEY_VIP_KEYWORDS, vipKeywords.joinToString(","))
            .putString(KEY_VIP_CONTACTS, vipContacts.joinToString(","))
            .putBoolean(KEY_AUTO_ALARM, autoAlarm)
            .putBoolean(KEY_AUTO_CALENDAR, autoCal)
            .apply()

        _settingsFlow.value = loadSettings()
    }

    companion object {
        private const val KEY_ARRIVAL_HOUR = "key_arrival_hour"
        private const val KEY_ARRIVAL_MINUTE = "key_arrival_minute"
        private const val KEY_COMMUTE_MIN = "key_commute_min"
        private const val KEY_PREP_MIN = "key_prep_min"
        private const val KEY_VIP_KEYWORDS = "key_vip_keywords"
        private const val KEY_VIP_CONTACTS = "key_vip_contacts"
        private const val KEY_AUTO_ALARM = "key_auto_alarm"
        private const val KEY_AUTO_CALENDAR = "key_auto_calendar"
    }
}
