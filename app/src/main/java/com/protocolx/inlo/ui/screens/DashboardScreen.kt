package com.protocolx.inlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
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
    onOpenSection: (PriorityTier) -> Unit,
    onGlobalSummarize: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAlarms: () -> Unit,
    onInjectDemo: () -> Unit
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

    val p0Items = summaries.filter { it.tier == PriorityTier.P0_CRITICAL }
    val p1Items = summaries.filter { it.tier == PriorityTier.P1_HIGH }
    val p2Items = summaries.filter { it.tier == PriorityTier.P2_MEDIUM }
    val p3Items = summaries.filter { it.tier == PriorityTier.P3_LOW }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "INLO",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x2014B8A6))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "LOCAL AI",
                                color = TealAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // Demo Scenarios button for easy testing
                    IconButton(onClick = onInjectDemo) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Feed Demo Messages",
                            tint = TealAccent
                        )
                    }
                    IconButton(onClick = onOpenAlarms) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "Alarms Hub",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
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
            // Permission Alert Banner (if needed)
            if (!hasNotificationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E0A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(P1Amber)
                    )
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
                            onClick = { PermissionHelper.openNotificationAccessSettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = P1Amber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enable", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Global AI Summarize Hero Card (Outside App Sections)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TealPrimary)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.height(20.dp).width(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GLOBAL AI SUMMARIZER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealAccent,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "${summaries.size} Total Items",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Intelligently summarize whole conversation across all priority boxes into an executive brief.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onGlobalSummarize,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.height(16.dp).width(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ Global AI Summarize (${summaries.size} Messages)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sections Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIORITY SECTIONS (DECREASING)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap box to view & summarize",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Box 1: P0 · Critical & Emergency Action (Decreasing Priority 1/5)
            PrioritySectionBox(
                emoji = "🚨",
                tierTitle = "P0 · Critical Action & Emergency",
                description = "Urgent directives, schedule shifts & blockers",
                count = p0Items.size,
                countUnit = "Alerts",
                accentColor = P0Red,
                latestPreview = p0Items.firstOrNull()?.catchyHeadline ?: "All clear · No active emergencies",
                onClick = { onOpenSection(PriorityTier.P0_CRITICAL) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Box 2: P1 · High Priority & Decisions (Decreasing Priority 2/5)
            PrioritySectionBox(
                emoji = "⚡",
                tierTitle = "P1 · Decisions & Tasks",
                description = "Agreed consensus, deliverables & scheduled tasks",
                count = p1Items.size,
                countUnit = "Tasks",
                accentColor = P1Amber,
                latestPreview = p1Items.firstOrNull()?.catchyHeadline ?: "No pending high-priority deliverables",
                onClick = { onOpenSection(PriorityTier.P1_HIGH) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Box 3: P2 · Mentions & Inquiries (Decreasing Priority 3/5)
            PrioritySectionBox(
                emoji = "📌",
                tierTitle = "P2 · Mentions & Inquiries",
                description = "Direct @mentions, collaboration & pending questions",
                count = p2Items.size,
                countUnit = "Inquiries",
                accentColor = P2Teal,
                latestPreview = p2Items.firstOrNull()?.catchyHeadline ?: "No questions or mentions waiting",
                onClick = { onOpenSection(PriorityTier.P2_MEDIUM) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Box 4: P3 · Ambient & Banter (Decreasing Priority 4/5)
            PrioritySectionBox(
                emoji = "💬",
                tierTitle = "P3 · Ambient & Banter",
                description = "Casual chatter, newsletters & non-urgent rollups",
                count = p3Items.size,
                countUnit = "Updates",
                accentColor = P3Slate,
                latestPreview = p3Items.firstOrNull()?.catchyHeadline ?: "Inbox quiet · Zero ambient clutter",
                onClick = { onOpenSection(PriorityTier.P3_LOW) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Box 5: Smart Automations & Alarms (Decreasing Priority 5/5)
            PrioritySectionBox(
                emoji = "⏰",
                tierTitle = "Smart Automations & Alarms",
                description = "Auto-scheduled wake-up alarms & calendar syncs",
                count = automations.size,
                countUnit = "Active",
                accentColor = TealPrimary,
                latestPreview = if (automations.isNotEmpty()) {
                    "Auto-Set: ${automations.first().title} (${automations.first().scheduledTimeDisplay})"
                } else {
                    "No scheduled automations active"
                },
                onClick = onOpenAlarms
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PrioritySectionBox(
    emoji: String,
    tierTitle: String,
    description: String,
    count: Int,
    countUnit: String,
    accentColor: Color,
    latestPreview: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (count > 0) accentColor.copy(alpha = 0.8f) else CardBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Emoji + Title + Outside Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tierTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Outside Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (count > 0) accentColor.copy(alpha = 0.2f) else Color(0x15FFFFFF)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$count $countUnit",
                        color = if (count > 0) accentColor else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Preview Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = latestPreview,
                        fontSize = 12.sp,
                        color = if (count > 0) TextPrimary else TextMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (count > 0) FontWeight.Medium else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Section",
                        tint = if (count > 0) accentColor else TextMuted,
                        modifier = Modifier.height(14.dp).width(14.dp)
                    )
                }
            }
        }
    }
}
