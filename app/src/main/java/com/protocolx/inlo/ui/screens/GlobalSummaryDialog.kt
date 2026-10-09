package com.protocolx.inlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.protocolx.inlo.engine.AiGlobalSummary
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

@Composable
fun GlobalSummaryDialog(
    summary: AiGlobalSummary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(TealPrimary)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.height(18.dp).width(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Global AI Brief",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${summary.totalCount} items across all boxes · 100% Local AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2014B8A6))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🛡️ Local", color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Stat Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlobalStatPill("🚨 P0", summary.p0Count, P0Red)
                    GlobalStatPill("⚡ P1", summary.p1Count, P1Amber)
                    GlobalStatPill("📌 P2", summary.p2Count, P2Teal)
                    GlobalStatPill("💬 P3", summary.p3Count, P3Slate)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-level Executive Brief Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132238))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "🤖 CROSS-STREAM EXECUTIVE SYNTHESIS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealAccent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.executiveBrief,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                // P0 Critical Alerts Section
                if (summary.criticalAlerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🚨 CRITICAL DIRECTIVES & EMERGENCY (P0)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = P0Red
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    summary.criticalAlerts.forEach { alert ->
                        Text(
                            text = "• $alert",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // P1 High Priority Tasks Section
                if (summary.highPriorityTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "⚡ DECISIONS & TASKS (P1)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = P1Amber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    summary.highPriorityTasks.forEach { task ->
                        Text(
                            text = "• $task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // P2 Mentions Section
                if (summary.mentionsAndInquiries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "📌 INQUIRIES & COLLABORATION (P2)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = P2Teal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    summary.mentionsAndInquiries.forEach { inquiry ->
                        Text(
                            text = "• $inquiry",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // P3 Ambient Rollup
                if (summary.ambientDigest != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "💬 AMBIENT & CHATTER (P3)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = P3Slate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary.ambientDigest,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Recommended Actions
                if (summary.recommendedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🎯 RECOMMENDED IMMEDIATE ACTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    summary.recommendedActions.forEach { action ->
                        Text(
                            text = "→ $action",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got It (Dismiss Brief)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GlobalStatPill(label: String, count: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text("$count", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
