package com.protocolx.inlo.engine

import java.util.Calendar

data class ExtractedSchedule(
    val targetHour: Int,           // 0..23
    val targetMinute: Int,         // 0..59
    val isTomorrow: Boolean,
    val targetLocationOrEvent: String,
    val rawMatchedPhrase: String
)

object TemporalSignalExtractor {
    // Regex for matching times like "at 8", "at 8am", "at 8:30", "come at 8", "by 8:00 pm", "tomorrow at 8"
    private val TIME_REGEX = Regex(
        "(?i)\\b(?:come|reach|arrive|be\\s+at|by|at)\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b"
    )

    // Fallback general time pattern: "8:00 am", "8am", "8 pm"
    private val DIRECT_TIME_REGEX = Regex(
        "(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b"
    )

    fun extractSchedule(text: String): ExtractedSchedule? {
        return try {
            val lowerText = text.lowercase()

            val isTomorrow = lowerText.contains("tomorrow") || lowerText.contains("tmrw") || lowerText.contains("next day")

            // Try primary action time regex
            val match = TIME_REGEX.find(text) ?: DIRECT_TIME_REGEX.find(text) ?: return null

            var hour = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
            val minute = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0

            val amPm = match.groupValues.getOrNull(3)?.lowercase() ?: ""

            // Adjust hour for AM/PM
            if (amPm == "pm" && hour < 12) {
                hour += 12
            } else if (amPm == "am" && hour == 12) {
                hour = 0
            } else if (amPm.isEmpty()) {
                // Contextual inference: work arrival times typically in morning (7..11) or afternoon (12..18)
                if (hour in 7..11) {
                    // assume AM for morning arrival
                } else if (hour in 1..6) {
                    if (lowerText.contains("morning") || lowerText.contains("early")) {
                        // AM
                    } else {
                        hour += 12 // assume PM for 1..6 if not specified
                    }
                }
            }

            // Location / context detection
            val location = when {
                lowerText.contains("office") -> "Office"
                lowerText.contains("client") -> "Client Meeting"
                lowerText.contains("site") -> "Site"
                lowerText.contains("branch") -> "Branch"
                lowerText.contains("demo") -> "Sprint Demo"
                lowerText.contains("review") -> "Project Review"
                lowerText.contains("call") || lowerText.contains("meeting") -> "Meeting"
                else -> "Work Arrival"
            }

            ExtractedSchedule(
                targetHour = hour,
                targetMinute = minute,
                isTomorrow = isTomorrow,
                targetLocationOrEvent = location,
                rawMatchedPhrase = match.value
            )
        } catch (e: Exception) {
            null
        }
    }
}
