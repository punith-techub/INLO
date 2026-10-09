package com.protocolx.inlo

import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.data.model.UserSettings
import com.protocolx.inlo.engine.CommuteAlarmCalculator
import com.protocolx.inlo.engine.PriorityClassifier
import com.protocolx.inlo.engine.TemporalSignalExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommuteAlarmCalculatorTest {

    @Test
    fun testManagerEarlyShiftAlarmCalculation() {
        // User typical arrival is 10:00 AM
        // Commute = 2 hours (120 mins)
        // Prep = 1 hour (60 mins)
        // Manager requests 8:00 AM arrival
        val settings = UserSettings(
            typicalArrivalHour = 10,
            typicalArrivalMinute = 0,
            commuteMinutes = 120,
            prepMinutes = 60
        )

        val result = CommuteAlarmCalculator.calculate(
            targetHour = 8,
            targetMinute = 0,
            settings = settings
        )

        // Target arrival: 8:00 AM
        assertEquals(8, result.targetHour)
        assertEquals(0, result.targetMinute)

        // Wake-up alarm: 8:00 AM - (2h + 1h = 3h) = 5:00 AM
        assertEquals(5, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("05:00 AM", result.formattedAlarmTime)
        assertEquals("08:00 AM", result.formattedTargetTime)

        // Verify detected as early shift (10:00 AM -> 8:00 AM = 120m early)
        assertTrue(result.isEarlyShift)
        assertEquals(120, result.earlyShiftMinutesDelta)
    }

    @Test
    fun testTemporalExtractionFromText() {
        val sampleText = "Tomorrow please come at 8 to office for client review"
        val schedule = TemporalSignalExtractor.extractSchedule(sampleText)

        assertNotNull(schedule)
        assertEquals(8, schedule?.targetHour)
        assertEquals(0, schedule?.targetMinute)
        assertTrue(schedule?.isTomorrow == true)
        assertEquals("Office", schedule?.targetLocationOrEvent)
    }

    @Test
    fun testPriorityClassificationForManagerEarlyShift() {
        val settings = UserSettings(
            typicalArrivalHour = 10,
            commuteMinutes = 120,
            prepMinutes = 60,
            vipContacts = listOf("Manager")
        )

        val schedule = TemporalSignalExtractor.extractSchedule("Tomorrow come at 8 to office")
        assertNotNull(schedule)

        val alarm = CommuteAlarmCalculator.calculate(schedule!!.targetHour, schedule.targetMinute, settings)

        val tier = PriorityClassifier.classify(
            sender = "Manager",
            text = "Tomorrow come at 8 to office",
            schedule = schedule,
            alarmResult = alarm,
            settings = settings
        )

        assertEquals(PriorityTier.P0_CRITICAL, tier)
    }
}
