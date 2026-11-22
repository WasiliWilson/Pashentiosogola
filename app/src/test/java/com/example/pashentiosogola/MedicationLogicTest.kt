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
}
