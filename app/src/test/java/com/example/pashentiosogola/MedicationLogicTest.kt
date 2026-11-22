package com.example.pashentiosogola

import com.example.pashentiosogola.data.Medication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationLogicTest {

    @Test
    fun testMedicationCreationAndDefaults() {
        val med = Medication(
            name = "Paracetamol",
            dosage = "500mg",
            time = "09:00 AM",
            instructions = "Take after meals",
            category = "Morning",
            pillCount = 30
        )

        assertEquals("Paracetamol", med.name)
        assertEquals("500mg", med.dosage)
        assertEquals("09:00 AM", med.time)
        assertEquals("Take after meals", med.instructions)
        assertEquals("Morning", med.category)
        assertEquals(30, med.pillCount)
        assertFalse(med.isTaken)
    }

    @Test
    fun testMedicationTakenCopy() {
        val med = Medication(
            name = "Aspirin",
            dosage = "100mg",
            time = "08:00 AM",
            instructions = "",
            pillCount = 10
        )

        val takenMed = med.copy(isTaken = true, pillCount = med.pillCount - 1)
        assertTrue(takenMed.isTaken)
        assertEquals(9, takenMed.pillCount)
    }

    @Test
    fun testTimeParsing() {
        val t1 = com.example.pashentiosogola.alarm.AlarmScheduler.parseHourMinute("08:00 AM")
        assertEquals(Pair(8, 0), t1)

        val t2 = com.example.pashentiosogola.alarm.AlarmScheduler.parseHourMinute("12:00 PM")
        assertEquals(Pair(12, 0), t2)

        val t3 = com.example.pashentiosogola.alarm.AlarmScheduler.parseHourMinute("12:00 AM")
        assertEquals(Pair(0, 0), t3)

        val t4 = com.example.pashentiosogola.alarm.AlarmScheduler.parseHourMinute("01:30 PM")
        assertEquals(Pair(13, 30), t4)

        val t5 = com.example.pashentiosogola.alarm.AlarmScheduler.parseHourMinute("11:45 PM")
        assertEquals(Pair(23, 45), t5)
    }
}
