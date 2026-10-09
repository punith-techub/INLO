package com.protocolx.inlo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.protocolx.inlo.data.db.AppDatabase
import com.protocolx.inlo.data.model.AutomationStatus
import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.ScheduledAutomation
import com.protocolx.inlo.data.model.UserSettings
import com.protocolx.inlo.data.preferences.UserPreferencesManager
import com.protocolx.inlo.engine.AutonomousMemoryEngine
import com.protocolx.inlo.recap.RecapEngine
import com.protocolx.inlo.recap.RecapReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val prefsManager = UserPreferencesManager(application)

    val settings: StateFlow<UserSettings> = prefsManager.settingsFlow

    val activeSummaries: StateFlow<List<MicroSummaryCard>> = db.summaryDao()
        .getAllActiveSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automations: StateFlow<List<ScheduledAutomation>> = db.automationDao()
        .getAllAutomations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRecap = MutableStateFlow<RecapReport?>(null)
    val currentRecap: StateFlow<RecapReport?> = _currentRecap.asStateFlow()

    fun dismissCard(id: String) {
        viewModelScope.launch {
            val card = activeSummaries.value.find { it.id == id }
            if (card != null) {
                // Autonomous background learning: learn from user dismissal
                AutonomousMemoryEngine.recordCorrection(getApplication(), card.sender, card.catchyHeadline)
            }
            db.summaryDao().dismissSummary(id)
        }
    }

    fun clearAllCards() {
        viewModelScope.launch {
            db.summaryDao().clearAll()
        }
    }

    fun cancelAutomation(id: String) {
        viewModelScope.launch {
            val item = automations.value.find { it.id == id }
            if (item != null) {
                // Autonomous background learning: learn that this auto-alarm was not desired
                AutonomousMemoryEngine.recordCorrection(getApplication(), item.title, item.detail)
            }
            db.automationDao().updateStatus(id, AutomationStatus.CANCELLED)
        }
    }

    fun generateCatchUpRecap(title: String = "30-Second Catch-Up") {
        val cards = activeSummaries.value
        val report = RecapEngine.generateCatchUpRecap(cards, title)
        _currentRecap.value = report
    }

    fun dismissRecapDialog() {
        _currentRecap.value = null
    }

    fun saveSettings(
        arrivalHour: Int,
        arrivalMinute: Int,
        commuteMin: Int,
        prepMin: Int,
        vipKeywords: List<String>,
        vipContacts: List<String>,
        autoAlarm: Boolean,
        autoCal: Boolean
    ) {
        prefsManager.updateSettings(
            arrivalHour = arrivalHour,
            arrivalMinute = arrivalMinute,
            commuteMin = commuteMin,
            prepMin = prepMin,
            vipKeywords = vipKeywords,
            vipContacts = vipContacts,
            autoAlarm = autoAlarm,
            autoCal = autoCal
        )
    }
}
