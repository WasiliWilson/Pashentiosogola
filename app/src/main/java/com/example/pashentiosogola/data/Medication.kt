package com.example.pashentiosogola.data

import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val time: String,
    val instructions: String,
    val category: String = "Morning", // Morning, Afternoon, Evening, Bedtime
    val pillCount: Int = 30, // Remaining pills for refill tracking
    val isTaken: Boolean = false,
    val takenTimestamp: String? = null
)
