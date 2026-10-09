package com.protocolx.inlo.engine

import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier
import java.util.Locale

data class AiMessageSummary(
    val headline: String,
    val actionPill: String?,
    val targetTimeString: String? = null,
    val alarmTimeString: String? = null,
    val detectedIntent: String = "GENERAL"
)

data class AiSectionSummary(
    val tier: PriorityTier,
    val title: String,
    val executiveBrief: String,
    val keyTakeaways: List<String>,
    val actionItems: List<String>,
    val activeSenders: List<String>,
    val messageCount: Int,
    val generatedAt: Long = System.currentTimeMillis()
)

data class AiGlobalSummary(
    val title: String,
    val totalCount: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val p3Count: Int,
    val executiveBrief: String,
    val criticalAlerts: List<String>,
    val highPriorityTasks: List<String>,
    val mentionsAndInquiries: List<String>,
    val ambientDigest: String?,
    val recommendedActions: List<String>,
    val generatedAt: Long = System.currentTimeMillis()
)

object LocalAiSummarizer {

    // Regex for stripping common greeting preambles
    private val GREETING_REGEX = Regex(
        "^(hey|hi|hello|good\\s+(morning|afternoon|evening)|dear|yo|sup|hiya|greetings)(\\s+([\\w\\d]+|all|team|everyone|folks|guys|bro|dude|there))?[,!:\\-\\.]*\\s*",
        RegexOption.IGNORE_CASE
    )

    // Regex for stripping email and message sign-offs
    private val SIGN_OFF_REGEX = Regex(
        "(?:\\r?\\n)+(?:thanks|thank you|regards|best regards|cheers|sincerely|warmly|best|talk soon|sent from my (?:iphone|android|mobile)|yours truly)[,!:\\-\\.]*.*$",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    // Regex for stripping conversational filler preambles
    private val FILLER_PREAMBLE_REGEX = Regex(
        "^(just (wanted to|checking|following up|letting you know)|i was wondering if|could you please|can (someone|you) please|do you mind if|quick (reminder|question|update|heads up|note)|fyi|please note that|please be advised that|heads up:?|ping me when|is it possible to)\\s*:?\\s*",
        RegexOption.IGNORE_CASE
    )

    // Regex for URLs
    private val URL_REGEX = Regex("https?://[^\\s]+")

    /**
     * Cleans and strips noise, greetings, fillers, and sign-offs from raw message text.
     */
    fun cleanMessage(rawText: String): String {
        var text = rawText.replace("\r\n", "\n").trim()
        text = SIGN_OFF_REGEX.replace(text, "")
        text = GREETING_REGEX.replace(text, "")
        text = FILLER_PREAMBLE_REGEX.replace(text, "")
        text = URL_REGEX.replace(text, "[Link]")
        text = text.replace(Regex("\\s+"), " ").trim()
        return if (text.isNotEmpty()) text else rawText.trim()
    }

    /**
     * Single notification AI summarization:
     * Synthesizes communicative intent, extracts key entities, and produces a crisp modern AI headline
     * instead of echoing the raw text.
     */
    fun summarizeSingle(
        tier: PriorityTier,
        sender: String,
        text: String,
        schedule: ExtractedSchedule?,
        alarmResult: AlarmCalculationResult?
    ): MicroSummaryResult {
        val cleaned = cleanMessage(text)
        val lower = cleaned.lowercase(Locale.ROOT)

        // Case 1: Early schedule shift with automated alarm
        if (tier == PriorityTier.P0_CRITICAL && alarmResult != null && alarmResult.isEarlyShift) {
            val prepHours = alarmResult.prepMinutes / 60
            val prepMins = alarmResult.prepMinutes % 60
            val prepText = if (prepHours > 0) "${prepHours}h prep" else "${prepMins}m prep"

            val travelHours = alarmResult.commuteMinutes / 60
            val travelMins = alarmResult.commuteMinutes % 60
            val travelText = if (travelHours > 0) "${travelHours}h travel" else "${travelMins}m travel"

            val loc = schedule?.targetLocationOrEvent ?: "Office"
            val headline = "🚨 Early Shift: $sender wants you at $loc by ${alarmResult.formattedTargetTime} → ⏰ Wake-up alarm set for ${alarmResult.formattedAlarmTime} ($travelText + $prepText)"
            val pill = "⏰ ${alarmResult.formattedAlarmTime} Alarm | 📅 Added"
            return MicroSummaryResult(headline, pill, alarmResult.formattedTargetTime, alarmResult.formattedAlarmTime)
        }

        // Case 2: Schedule update without early alarm
        if (schedule != null) {
            val h = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
            val amPm = if (schedule.targetHour in 12..23) "PM" else "AM"
            val timeStr = String.format(Locale.ROOT, "%02d:%02d %s", h, schedule.targetMinute, amPm)
            val loc = schedule.targetLocationOrEvent
            val day = if (schedule.isTomorrow) "tomorrow" else "today"
            val headline = "📅 Schedule: $sender scheduled $loc for $timeStr $day"
            val pill = "📅 Added to Calendar"
            return MicroSummaryResult(headline, pill, timeStr, null)
        }

        // Case 3: Outage, crash, or production blocker
        if (isOutageOrBlocker(lower)) {
            val incidentEntity = extractIncidentSubject(cleaned)
            val headline = "🚨 Critical Incident: $sender reported $incidentEntity outage requiring immediate investigation."
            val pill = "🚨 Urgent Incident"
            return MicroSummaryResult(headline, pill, null, null)
        }

        // Case 4: Action item, review, deliverable, or deadline
        if (isActionItem(lower)) {
            val actionItem = extractActionItemSubject(cleaned)
            val deadline = extractDeadline(cleaned)
            val deadlineSuffix = if (deadline != null) " (Due $deadline)" else ""
            val headline = "⚡ Action Required: $sender requested $actionItem$deadlineSuffix."
            val pill = "📌 Action Item"
            return MicroSummaryResult(headline, pill, null, null)
        }

        // Case 5: Decision, consensus, or approval
        if (isDecisionOrApproval(lower)) {
            val decisionSubject = extractDecisionSubject(cleaned)
            val headline = "⚡ Decision: $sender confirmed approval for $decisionSubject."
            val pill = "✅ Approved"
            return MicroSummaryResult(headline, pill, null, null)
        }

        // Case 6: Direct question, inquiry, or mention
        if (isInquiryOrQuestion(lower)) {
            val queryTopic = extractInquiryTopic(cleaned)
            val headline = "📌 Inquiry: $sender asked about $queryTopic."
            val pill = "👤 Needs Your Input"
            return MicroSummaryResult(headline, pill, null, null)
        }

        // Case 7: Casual chatter, coffee/lunch, or banter
        if (isCasualOrBanter(lower)) {
            val topic = extractCasualTopic(cleaned)
            val headline = "💬 Social: $sender: $topic."
            val pill = "☕ Casual Chatter"
            return MicroSummaryResult(headline, pill, null, null)
        }

        // Fallback: Intelligent semantic compression
        val compressed = compressText(cleaned, maxLength = 65)
        val tierPrefix = when (tier) {
            PriorityTier.P0_CRITICAL -> "🚨 Urgent: $sender: "
            PriorityTier.P1_HIGH -> "⚡ Important: $sender: "
            PriorityTier.P2_MEDIUM -> "📌 Update from $sender: "
            PriorityTier.P3_LOW -> "💬 $sender: "
        }
        val pill = when (tier) {
            PriorityTier.P0_CRITICAL -> "⚠️ Immediate Attention"
            PriorityTier.P1_HIGH -> "📌 Priority Item"
            PriorityTier.P2_MEDIUM -> "👤 Direct Mention"
            PriorityTier.P3_LOW -> "☕ Ambient"
        }
        return MicroSummaryResult("$tierPrefix$compressed", pill, null, null)
    }

    /**
     * Synthesizes all messages in a specific Priority Tier box into a short, executive brief.
     */
    fun summarizeSection(tier: PriorityTier, messages: List<MicroSummaryCard>): AiSectionSummary {
        val tierTitle = when (tier) {
            PriorityTier.P0_CRITICAL -> "P0 · Critical Action"
            PriorityTier.P1_HIGH -> "P1 · High Priority & Decisions"
            PriorityTier.P2_MEDIUM -> "P2 · Mentions & Inquiries"
            PriorityTier.P3_LOW -> "P3 · Ambient & Banter"
        }

        if (messages.isEmpty()) {
            return AiSectionSummary(
                tier = tier,
                title = tierTitle,
                executiveBrief = "No active notifications in this priority tier. Everything is caught up.",
                keyTakeaways = emptyList(),
                actionItems = emptyList(),
                activeSenders = emptyList(),
                messageCount = 0
            )
        }

        val senders = messages.map { it.sender }.distinct()
        val sendersList = when {
            senders.size == 1 -> senders[0]
            senders.size == 2 -> "${senders[0]} and ${senders[1]}"
            senders.size > 2 -> "${senders[0]}, ${senders[1]}, and ${senders.size - 2} other(s)"
            else -> "Senders"
        }

        val takeaways = mutableListOf<String>()
        val actions = mutableListOf<String>()

        messages.forEach { card ->
            val clean = cleanMessage(card.rawSnippet)
            val distilled = compressText(clean, maxLength = 75)
            takeaways.add("${card.sender}: $distilled")

            if (card.actionPill != null && !card.actionPill.contains("Ambient", ignoreCase = true) && !card.actionPill.contains("Casual", ignoreCase = true)) {
                actions.add("${card.actionPill} (${card.sender})")
            }
        }

        // Construct coherent executive brief based on tier characteristics
        val executiveBrief = when (tier) {
            PriorityTier.P0_CRITICAL -> {
                "${messages.size} critical item(s) requiring immediate attention from $sendersList. " +
                        "Review urgent shift directives and production blockers immediately."
            }
            PriorityTier.P1_HIGH -> {
                "${messages.size} high-priority deliverable(s) and decision(s) recorded from $sendersList. " +
                        "Action items require scheduled follow-up or review."
            }
            PriorityTier.P2_MEDIUM -> {
                "${messages.size} inquiry and collaboration request(s) waiting for your response from $sendersList."
            }
            PriorityTier.P3_LOW -> {
                "${messages.size} ambient update(s) and casual social conversation(s) rolled up across apps."
            }
        }

        return AiSectionSummary(
            tier = tier,
            title = tierTitle,
            executiveBrief = executiveBrief,
            keyTakeaways = takeaways.take(6),
            actionItems = actions.distinct().take(4),
            activeSenders = senders,
            messageCount = messages.size
        )
    }

    /**
     * Synthesizes all messages across ALL boxes into a unified Executive Intelligence Brief.
     */
    fun summarizeAll(messages: List<MicroSummaryCard>): AiGlobalSummary {
        val p0Items = messages.filter { it.tier == PriorityTier.P0_CRITICAL }
        val p1Items = messages.filter { it.tier == PriorityTier.P1_HIGH }
        val p2Items = messages.filter { it.tier == PriorityTier.P2_MEDIUM }
        val p3Items = messages.filter { it.tier == PriorityTier.P3_LOW }

        if (messages.isEmpty()) {
            return AiGlobalSummary(
                title = "Global Intelligence Brief",
                totalCount = 0,
                p0Count = 0,
                p1Count = 0,
                p2Count = 0,
                p3Count = 0,
                executiveBrief = "✨ All streams clear. Zero unread priority notifications.",
                criticalAlerts = emptyList(),
                highPriorityTasks = emptyList(),
                mentionsAndInquiries = emptyList(),
                ambientDigest = null,
                recommendedActions = listOf("Inbox zero achieved. No pending tasks.")
            )
        }

        val criticalAlerts = p0Items.map { card ->
            "🚨 [${card.sender}]: ${cleanHeadline(card.catchyHeadline)}"
        }

        val highPriorityTasks = p1Items.map { card ->
            "⚡ [${card.sender}]: ${cleanHeadline(card.catchyHeadline)}"
        }

        val mentionsAndInquiries = p2Items.map { card ->
            "📌 [${card.sender}]: ${cleanHeadline(card.catchyHeadline)}"
        }

        val ambientDigest = if (p3Items.isNotEmpty()) {
            val sources = p3Items.map { it.sourceApp }.distinct().joinToString(", ")
            "💬 ${p3Items.size} low-priority update(s) rolled up from $sources."
        } else null

        // Synthesize executive brief
        val briefBuilder = StringBuilder()
        briefBuilder.append("Total ${messages.size} updates triaged across 4 priority streams on-device. ")
        if (p0Items.isNotEmpty()) {
            briefBuilder.append("🚨 Priority 0: ${p0Items.size} emergency alert(s) demand immediate action. ")
        }
        if (p1Items.isNotEmpty()) {
            briefBuilder.append("⚡ ${p1Items.size} important task(s) and decision(s) tracked. ")
        }
        if (p2Items.isNotEmpty()) {
            briefBuilder.append("📌 ${p2Items.size} mention(s) waiting for response. ")
        }
        if (p3Items.isNotEmpty()) {
            briefBuilder.append("💬 ${p3Items.size} ambient updates quieted.")
        }

        // Recommend top next actions
        val recommendedActions = mutableListOf<String>()
        if (p0Items.isNotEmpty()) {
            recommendedActions.add("Address ${p0Items.size} critical alert(s) immediately.")
        }
        if (p1Items.isNotEmpty()) {
            recommendedActions.add("Review tasks and deadlines from ${p1Items.take(2).map { it.sender }.distinct().joinToString(", ")}.")
        }
        if (p2Items.isNotEmpty()) {
            recommendedActions.add("Respond to questions from ${p2Items.take(2).map { it.sender }.distinct().joinToString(", ")}.")
        }
        if (recommendedActions.isEmpty()) {
            recommendedActions.add("No urgent actions required.")
        }

        return AiGlobalSummary(
            title = "Executive Intelligence Brief",
            totalCount = messages.size,
            p0Count = p0Items.size,
            p1Count = p1Items.size,
            p2Count = p2Items.size,
            p3Count = p3Items.size,
            executiveBrief = briefBuilder.toString().trim(),
            criticalAlerts = criticalAlerts,
            highPriorityTasks = highPriorityTasks,
            mentionsAndInquiries = mentionsAndInquiries,
            ambientDigest = ambientDigest,
            recommendedActions = recommendedActions
        )
    }

    private fun cleanHeadline(headline: String): String {
        return headline.replace(Regex("^[🚨⚡📌💬⏰]\\s*"), "").trim()
    }

    // Helper NLP pattern matchers
    private fun isOutageOrBlocker(text: String): Boolean {
        val keywords = listOf("down", "outage", "500", "502", "503", "504", "crash", "crashed", "offline", "timeout", "failing", "incident", "broken build", "critical bug", "sev-1", "sev1", "blocker", "prod down", "service is down")
        return keywords.any { text.contains(it) }
    }

    private fun isActionItem(text: String): Boolean {
        val keywords = listOf("review", "approve", "approval", "sign", "submit", "send", "share", "pr #", "pull request", "draft", "invoice", "due by", "deadline", "complete", "finish", "merge", "feedback", "presentation", "budget")
        return keywords.any { text.contains(it) }
    }

    private fun isDecisionOrApproval(text: String): Boolean {
        val keywords = listOf("approved", "agreed", "confirmed", "signed off", "merged", "consensus", "green light", "finalized", "accepted")
        return keywords.any { text.contains(it) }
    }

    private fun isInquiryOrQuestion(text: String): Boolean {
        if (text.contains("?")) return true
        val keywords = listOf("are you free", "when can", "what is", "where is", "how is", "who is", "any update", "status of", "status on", "timeline for")
        return keywords.any { text.contains(it) }
    }

    private fun isCasualOrBanter(text: String): Boolean {
        val keywords = listOf("lunch", "coffee", "gym", "beer", "drinks", "party", "weekend", "dinner", "meme", "lol", "haha", "plans")
        return keywords.any { text.contains(it) }
    }

    private fun extractIncidentSubject(cleaned: String): String {
        val patterns = listOf(
            Regex("([\\w\\-\\s]+)(outage|service|server|api|database|db|gateway|portal|cluster)", RegexOption.IGNORE_CASE),
            Regex("(50[0-4]|error|crash|timeout)\\s+in\\s+([\\w\\-\\s]+)", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(cleaned)
            if (match != null) {
                return match.value.trim().take(30)
            }
        }
        return "system component"
    }

    private fun extractActionItemSubject(cleaned: String): String {
        val patterns = listOf(
            Regex("(review|approve|submit|send|share|check)\\s+the\\s+([\\w\\-\\s]+)", RegexOption.IGNORE_CASE),
            Regex("(pr|pull request)\\s*(#?\\d+)?", RegexOption.IGNORE_CASE),
            Regex("([\\w\\-\\s]+)(proposal|document|doc|sheet|slides|deck|report|pr)", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(cleaned)
            if (match != null) {
                return match.value.trim().take(35)
            }
        }
        return "deliverable review"
    }

    private fun extractDeadline(cleaned: String): String? {
        val pattern = Regex("(by|before|due)\\s+([a-zA-Z]+\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?|\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)|today|tomorrow|friday|monday)", RegexOption.IGNORE_CASE)
        val match = pattern.find(cleaned)
        return match?.groupValues?.getOrNull(2)
    }

    private fun extractDecisionSubject(cleaned: String): String {
        val pattern = Regex("(approved|agreed|confirmed|merged)\\s+([\\w\\-\\s]+)", RegexOption.IGNORE_CASE)
        val match = pattern.find(cleaned)
        return match?.groupValues?.getOrNull(2)?.take(30) ?: "proposal"
    }

    private fun extractInquiryTopic(cleaned: String): String {
        val pattern = Regex("(about|regarding|on)\\s+([\\w\\-\\s]+)", RegexOption.IGNORE_CASE)
        val match = pattern.find(cleaned)
        return match?.groupValues?.getOrNull(2)?.replace("?", "")?.take(35) ?: "pending topic"
    }

    private fun extractCasualTopic(cleaned: String): String {
        val keywords = listOf("lunch", "coffee", "gym", "drinks", "dinner", "party", "weekend plans")
        for (kw in keywords) {
            if (cleaned.contains(kw, ignoreCase = true)) {
                return "Asked about $kw"
            }
        }
        return compressText(cleaned, maxLength = 35)
    }

    private fun compressText(text: String, maxLength: Int): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        val firstSentence = clean.split(Regex("[.!?]\\s+")).firstOrNull()?.trim() ?: clean
        return if (firstSentence.length > maxLength) {
            firstSentence.take(maxLength - 3) + "..."
        } else {
            firstSentence
        }
    }
}
