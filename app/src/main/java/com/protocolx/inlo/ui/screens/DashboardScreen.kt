package com.protocolx.inlo.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.data.model.ScheduledAutomation
import com.protocolx.inlo.ui.PermissionHelper
import com.protocolx.inlo.ui.theme.BgDark
import com.protocolx.inlo.ui.theme.CardBorder
import com.protocolx.inlo.ui.theme.CardDark
import com.protocolx.inlo.ui.theme.P0Red
import com.protocolx.inlo.ui.theme.P1Amber
import com.protocolx.inlo.ui.theme.P2Teal
import com.protocolx.inlo.ui.theme.P3Slate
import com.protocolx.inlo.ui.theme.TealAccent
import com.protocolx.inlo.ui.theme.TealPrimary
import com.protocolx.inlo.ui.theme.TextMuted
import com.protocolx.inlo.ui.theme.TextPrimary
import com.protocolx.inlo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    summaries: List<MicroSummaryCard>,
    automations: List<ScheduledAutomation>,
    onOpenSettings: () -> Unit,
    onOpenAlarms: () -> Unit,
    onGenerateCatchUp: () -> Unit,
    onDismissCard: (String) -> Unit
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember { mutableStateOf(PermissionHelper.isNotificationAccessGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = PermissionHelper.isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("INLO", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x2014B8A6))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("LOCAL FIRST", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAlarms) {
                        Icon(Icons.Default.Alarm, contentDescription = "Alarms Hub", tint = TextPrimary)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
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
        ) {
            // Permission Banner (if not granted)
            if (!hasNotificationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E0A)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(P1Amber))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = P1Amber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notification Access Required", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Allow INLO to read and summarize incoming notifications.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = {
                                PermissionHelper.openNotificationAccessSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = P1Amber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enable", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGenerateCatchUp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("⚡ Recap (30s TL;DR)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onOpenAlarms,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = TextPrimary, modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alarms (${automations.size})", color = TextPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Summaries Grouped by Tier
            val p0Items = summaries.filter { it.tier == PriorityTier.P0_CRITICAL }
            val p1Items = summaries.filter { it.tier == PriorityTier.P1_HIGH }
            val p2Items = summaries.filter { it.tier == PriorityTier.P2_MEDIUM }
            val p3Items = summaries.filter { it.tier == PriorityTier.P3_LOW }

            if (summaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x1514B8A6))
                                .padding(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.height(36.dp).width(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("INLO is Active & Listening", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Incoming notifications from WhatsApp, Slack, Gmail, etc. will be summarized and prioritized here in real time.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "🔒 0 bytes leave your device",
                            color = TealPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (p0Items.isNotEmpty()) {
                        item { TierHeader("🚨 P0 · CRITICAL ACTION", p0Items.size, P0Red) }
                        items(p0Items, key = { it.id }) { item ->
                            FlashcardItem(item = item, accentColor = P0Red, onDismiss = { onDismissCard(item.id) })
                        }
                    }

                    if (p1Items.isNotEmpty()) {
                        item { TierHeader("⚡ P1 · DECISIONS & TASKS", p1Items.size, P1Amber) }
                        items(p1Items, key = { it.id }) { item ->
                            FlashcardItem(item = item, accentColor = P1Amber, onDismiss = { onDismissCard(item.id) })
                        }
                    }

                    if (p2Items.isNotEmpty()) {
                        item { TierHeader("📌 P2 · MENTIONS & QUESTIONS", p2Items.size, P2Teal) }
                        items(p2Items, key = { it.id }) { item ->
                            FlashcardItem(item = item, accentColor = P2Teal, onDismiss = { onDismissCard(item.id) })
                        }
                    }

                    if (p3Items.isNotEmpty()) {
                        item { TierHeader("💬 P3 · AMBIENT & BANTER", p3Items.size, P3Slate) }
                        items(p3Items, key = { it.id }) { item ->
                            FlashcardItem(item = item, accentColor = P3Slate, onDismiss = { onDismissCard(item.id) })
                        }
                    }

                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TierHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("$count", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FlashcardItem(
    item: MicroSummaryCard,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Source & Sender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.sourceApp, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(24.dp).width(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Punchy Headline
            Text(
                text = item.catchyHeadline,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // Action Pill
            if (item.actionPill != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.actionPill,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
