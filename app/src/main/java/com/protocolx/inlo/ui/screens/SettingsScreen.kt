package com.protocolx.inlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protocolx.inlo.data.model.UserSettings
import com.protocolx.inlo.ui.theme.BgDark
import com.protocolx.inlo.ui.theme.CardBorder
import com.protocolx.inlo.ui.theme.CardDark
import com.protocolx.inlo.ui.theme.TealAccent
import com.protocolx.inlo.ui.theme.TealPrimary
import com.protocolx.inlo.ui.theme.TextMuted
import com.protocolx.inlo.ui.theme.TextPrimary
import com.protocolx.inlo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: UserSettings,
    onBack: () -> Unit,
    onSave: (UserSettings) -> Unit
) {
    var commuteMin by remember { mutableFloatStateOf(currentSettings.commuteMinutes.toFloat()) }
    var prepMin by remember { mutableFloatStateOf(currentSettings.prepMinutes.toFloat()) }
    var typicalArrivalHour by remember { mutableIntStateOf(currentSettings.typicalArrivalHour) }
    var vipKeywordsText by remember { mutableStateOf(currentSettings.vipKeywords.joinToString(", ")) }
    var vipContactsText by remember { mutableStateOf(currentSettings.vipContacts.joinToString(", ")) }
    var autoAlarm by remember { mutableStateOf(currentSettings.isAutonomousAlarmEnabled) }
    var autoCalendar by remember { mutableStateOf(currentSettings.isAutonomousCalendarEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Routine Buffers", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Buffer Math Formula Explainer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TealPrimary))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Smart Wake-Up Buffer Formula", fontWeight = FontWeight.Bold, color = TealAccent, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Wake-Up Alarm = Target Arrival Time − (Commute + Prep Time)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val totalBuffer = commuteMin.toInt() + prepMin.toInt()
                    val totalBufferHours = totalBuffer / 60
                    val totalBufferRem = totalBuffer % 60
                    val bufferStr = if (totalBufferRem > 0) "${totalBufferHours}h ${totalBufferRem}m" else "${totalBufferHours}h"
                    Text(
                        "Current Total Buffer: $bufferStr (${commuteMin.toInt()/60}h travel + ${prepMin.toInt()/60}h prep)",
                        color = TealPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Example: If manager asks to arrive at 8:00 AM, alarm will ring at 5:00 AM.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Commute Travel Time Slider
            Text("COMMUTE / TRAVEL DURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text(
                "${commuteMin.toInt()} minutes (${String.format("%.1f", commuteMin / 60f)} hours)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Slider(
                value = commuteMin,
                onValueChange = { commuteMin = it },
                valueRange = 15f..240f,
                steps = 14,
                colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Morning Prep Time Slider
            Text("MORNING PREP DURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text(
                "${prepMin.toInt()} minutes (${String.format("%.1f", prepMin / 60f)} hours)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Slider(
                value = prepMin,
                onValueChange = { prepMin = it },
                valueRange = 15f..120f,
                steps = 6,
                colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Typical Arrival Time Picker Row
            Text("TYPICAL OFFICE ARRIVAL TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(8, 9, 10, 11).forEach { hour ->
                    val isSelected = typicalArrivalHour == hour
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) TealPrimary else CardDark)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$hour:00 AM",
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // VIP Contacts & Keywords
            Text("VIP CONTACTS (MANAGERS & LEADS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = vipContactsText,
                onValueChange = { vipContactsText = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("VIP KEYWORDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = vipKeywordsText,
                onValueChange = { vipKeywordsText = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Automation Toggles
            Text("AUTONOMOUS ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Auto-Set Alarms (AlarmClock API)", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text("Directly sets system alarm when early shift is detected", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = autoAlarm,
                    onCheckedChange = { autoAlarm = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary, checkedTrackColor = Color(0x3014B8A6))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Auto-Set Calendar (CalendarContract)", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text("Inserts schedule into Google/Device Calendar", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = autoCalendar,
                    onCheckedChange = { autoCalendar = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary, checkedTrackColor = Color(0x3014B8A6))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Guarantee Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Zero Network Data Transfer Guarantee", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "This app does not contain the android.permission.INTERNET permission. All summaries, alarms, and routines are processed 100% locally.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val updated = UserSettings(
                        typicalArrivalHour = typicalArrivalHour,
                        typicalArrivalMinute = 0,
                        commuteMinutes = commuteMin.toInt(),
                        prepMinutes = prepMin.toInt(),
                        vipKeywords = vipKeywordsText.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() },
                        vipContacts = vipContactsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        isAutonomousAlarmEnabled = autoAlarm,
                        isAutonomousCalendarEnabled = autoCalendar
                    )
                    onSave(updated)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
