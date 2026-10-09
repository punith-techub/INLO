package com.protocolx.inlo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.ui.screens.AlarmsHistoryScreen
import com.protocolx.inlo.ui.screens.DashboardScreen
import com.protocolx.inlo.ui.screens.GlobalSummaryDialog
import com.protocolx.inlo.ui.screens.RecapDialog
import com.protocolx.inlo.ui.screens.SectionDetailScreen
import com.protocolx.inlo.ui.screens.SettingsScreen
import com.protocolx.inlo.ui.theme.INLOTheme
import com.protocolx.inlo.ui.viewmodel.MainViewModel

enum class Screen {
    DASHBOARD,
    SETTINGS,
    ALARMS,
    SECTION_DETAIL
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("MainActivity", "Permissions callback received: $permissions")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            requestRuntimePermissions()
        } catch (t: Throwable) {
            Log.e("MainActivity", "Safe catch during permissions request: ${t.message}")
        }

        setContent {
            INLOTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }

    private fun requestRuntimePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALENDAR)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_CALENDAR)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedTier by remember { mutableStateOf<PriorityTier?>(null) }

    val summaries by viewModel.activeSummaries.collectAsState()
    val automations by viewModel.automations.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentRecap by viewModel.currentRecap.collectAsState()
    val currentGlobalSummary by viewModel.currentGlobalSummary.collectAsState()
    val sectionSummaries by viewModel.sectionSummaries.collectAsState()

    // Handle system back navigation gracefully
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            currentScreen = Screen.DASHBOARD
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.DASHBOARD -> {
                DashboardScreen(
                    summaries = summaries,
                    automations = automations,
                    onOpenSection = { tier ->
                        selectedTier = tier
                        currentScreen = Screen.SECTION_DETAIL
                    },
                    onGlobalSummarize = { viewModel.generateGlobalSummary() },
                    onOpenSettings = { currentScreen = Screen.SETTINGS },
                    onOpenAlarms = { currentScreen = Screen.ALARMS },
                    onInjectDemo = { viewModel.injectDemoScenarios() }
                )
            }

            Screen.SECTION_DETAIL -> {
                val tier = selectedTier ?: PriorityTier.P0_CRITICAL
                val tierMessages = summaries.filter { it.tier == tier }
                SectionDetailScreen(
                    tier = tier,
                    messages = tierMessages,
                    sectionSummary = sectionSummaries[tier],
                    onSummarizeBox = { viewModel.generateBoxSummary(tier) },
                    onDismissSummary = { viewModel.dismissBoxSummary(tier) },
                    onDismissMessage = { viewModel.dismissCard(it) },
                    onBack = { currentScreen = Screen.DASHBOARD }
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    currentSettings = settings,
                    onBack = { currentScreen = Screen.DASHBOARD },
                    onSave = { updated ->
                        viewModel.saveSettings(
                            arrivalHour = updated.typicalArrivalHour,
                            arrivalMinute = updated.typicalArrivalMinute,
                            commuteMin = updated.commuteMinutes,
                            prepMin = updated.prepMinutes,
                            vipKeywords = updated.vipKeywords,
                            vipContacts = updated.vipContacts,
                            autoAlarm = updated.isAutonomousAlarmEnabled,
                            autoCal = updated.isAutonomousCalendarEnabled
                        )
                    }
                )
            }

            Screen.ALARMS -> {
                AlarmsHistoryScreen(
                    automations = automations,
                    onBack = { currentScreen = Screen.DASHBOARD },
                    onCancelAutomation = { viewModel.cancelAutomation(it) }
                )
            }
        }

        // Show Global Summary Dialog when triggered
        currentGlobalSummary?.let { summary ->
            GlobalSummaryDialog(
                summary = summary,
                onDismiss = { viewModel.dismissGlobalSummary() }
            )
        }

        // Show Catch-Up Recap Dialog when active
        currentRecap?.let { report ->
            RecapDialog(
                report = report,
                onDismiss = { viewModel.dismissRecapDialog() }
            )
        }
    }
}

