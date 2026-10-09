package com.protocolx.inlo

import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.engine.AlarmCalculationResult
import com.protocolx.inlo.engine.ExtractedSchedule
import com.protocolx.inlo.engine.LocalAiSummarizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAiSummarizerTest {

    @Test
    fun testNoiseAndGreetingStripping() {
        val raw = "Hey guys, just wanted to check if the report is ready.\n\nThanks,\nJohn"
        val cleaned = LocalAiSummarizer.cleanMessage(raw)
        assertFalse(cleaned.contains("Hey guys", ignoreCase = true))
        assertFalse(cleaned.contains("Thanks", ignoreCase = true))
        assertTrue(cleaned.contains("report is ready", ignoreCase = true))
    }

    @Test
    fun testEarlyShiftAlarmSummarization() {
        val schedule = ExtractedSchedule(
            targetHour = 8,
            targetMinute = 0,
            isTomorrow = true,
            targetLocationOrEvent = "Office",
            rawMatchedPhrase = "come at 8"
        )
        val settings = com.protocolx.inlo.data.model.UserSettings(
            typicalArrivalHour = 10,
            typicalArrivalMinute = 0,
            commuteMinutes = 120,
            prepMinutes = 60
        )
        val alarmResult = com.protocolx.inlo.engine.CommuteAlarmCalculator.calculate(
            targetHour = 8,
            targetMinute = 0,
            settings = settings
        )

        val result = LocalAiSummarizer.summarizeSingle(
            tier = PriorityTier.P0_CRITICAL,
            sender = "Manager",
            text = "Tomorrow please come at 8 to office for client meeting",
            schedule = schedule,
            alarmResult = alarmResult
        )

        assertTrue(result.catchyHeadline.contains("08:00 AM"))
        assertTrue(result.catchyHeadline.contains("05:00 AM"))
        assertEquals("08:00 AM", result.targetTimeString)
        assertEquals("05:00 AM", result.alarmTimeString)
        assertNotNull(result.actionPill)
    }

    @Test
    fun testCriticalOutageSummarization() {
        val text = "Hi team, production database server is down with 502 connection timeout. Need immediate fix!"
        val result = LocalAiSummarizer.summarizeSingle(
            tier = PriorityTier.P0_CRITICAL,
            sender = "DevOps",
            text = text,
            schedule = null,
            alarmResult = null
        )

        assertTrue(result.catchyHeadline.contains("Critical Incident", ignoreCase = true))
        assertTrue(result.catchyHeadline.contains("immediate", ignoreCase = true))
        assertEquals("🚨 Urgent Incident", result.actionPill)
    }

    @Test
    fun testActionItemSummarizationWithDeadline() {
        val text = "Please review and approve the Q3 budget proposal by Friday 5 PM before the board sync."
        val result = LocalAiSummarizer.summarizeSingle(
            tier = PriorityTier.P1_HIGH,
            sender = "Finance Lead",
            text = text,
            schedule = null,
            alarmResult = null
        )

        assertTrue(result.catchyHeadline.contains("Action Required", ignoreCase = true))
        assertTrue(result.catchyHeadline.contains("Friday", ignoreCase = true))
        assertEquals("📌 Action Item", result.actionPill)
    }

    @Test
    fun testSectionSummarizer() {
        val cards = listOf(
            MicroSummaryCard(
                notificationId = "1",
                tier = PriorityTier.P0_CRITICAL,
                catchyHeadline = "🚨 Early Shift: Manager wants you at Office by 08:00 AM",
                sourceApp = "WhatsApp",
                sender = "Manager",
                rawSnippet = "Tomorrow come at 8 to office",
                actionPill = "⏰ 05:00 AM Alarm"
            ),
            MicroSummaryCard(
                notificationId = "2",
                tier = PriorityTier.P0_CRITICAL,
                catchyHeadline = "🚨 Critical Incident: Server down",
                sourceApp = "Slack",
                sender = "DevOps",
                rawSnippet = "Production server is down with timeout",
                actionPill = "🚨 Urgent Incident"
            )
        )

        val sectionSummary = LocalAiSummarizer.summarizeSection(PriorityTier.P0_CRITICAL, cards)
        assertEquals(2, sectionSummary.messageCount)
        assertEquals("P0 · Critical Action", sectionSummary.title)
        assertTrue(sectionSummary.executiveBrief.contains("critical item", ignoreCase = true))
        assertEquals(2, sectionSummary.keyTakeaways.size)
        assertTrue(sectionSummary.actionItems.isNotEmpty())
    }

    @Test
    fun testGlobalSummarizerAllTiers() {
        val cards = listOf(
            MicroSummaryCard(
                notificationId = "1",
                tier = PriorityTier.P0_CRITICAL,
                catchyHeadline = "🚨 Critical Incident: Server down",
                sourceApp = "Slack",
                sender = "DevOps",
                rawSnippet = "Production down"
            ),
            MicroSummaryCard(
                notificationId = "2",
                tier = PriorityTier.P1_HIGH,
                catchyHeadline = "⚡ Action: Review budget proposal by Friday 5 PM",
                sourceApp = "Gmail",
                sender = "Finance",
                rawSnippet = "Review budget proposal"
            ),
            MicroSummaryCard(
                notificationId = "3",
                tier = PriorityTier.P2_MEDIUM,
                catchyHeadline = "📌 Inquiry: Asked about Figma designs",
                sourceApp = "Slack",
                sender = "Design",
                rawSnippet = "Any comments on Figma?"
            ),
            MicroSummaryCard(
                notificationId = "4",
                tier = PriorityTier.P3_LOW,
                catchyHeadline = "💬 Social: Asked about coffee",
                sourceApp = "WhatsApp",
                sender = "Alex",
                rawSnippet = "Coffee today?"
            )
        )

        val global = LocalAiSummarizer.summarizeAll(cards)
        assertEquals(4, global.totalCount)
        assertEquals(1, global.p0Count)
        assertEquals(1, global.p1Count)
        assertEquals(1, global.p2Count)
        assertEquals(1, global.p3Count)
        assertTrue(global.executiveBrief.contains("4 updates"))
        assertEquals(1, global.criticalAlerts.size)
        assertEquals(1, global.highPriorityTasks.size)
        assertEquals(1, global.mentionsAndInquiries.size)
        assertNotNull(global.ambientDigest)
        assertTrue(global.recommendedActions.isNotEmpty())
    }
}
