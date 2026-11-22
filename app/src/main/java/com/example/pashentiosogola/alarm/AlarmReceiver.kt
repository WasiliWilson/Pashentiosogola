package com.example.pashentiosogola.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pashentiosogola.data.Medication
import com.example.pashentiosogola.data.db.MedicationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medId = intent.getStringExtra("MED_ID") ?: ""
        val medName = intent.getStringExtra("MED_NAME") ?: "Medication"
        val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""
        val medTime = intent.getStringExtra("MED_TIME") ?: ""
        val medInstructions = intent.getStringExtra("MED_INSTRUCTIONS") ?: ""

        when (action) {
            AlarmController.ACTION_TAKE_NOW -> {
                // Instantly stop audio, vibration, and foreground service
                AlarmService.stop(context)
                AlarmController.stopAlarm(context)

                // Mark medication as taken
                if (medId.isNotBlank()) {
                    val repo = MedicationRepository(context)
                    val meds = repo.loadMedications()
                    val timeFormat = SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault())
                    val currentTime = timeFormat.format(Date())

                    val updated = meds.map {
                        if (it.id == medId) {
                            val newPills = if (it.pillCount > 0) it.pillCount - 1 else it.pillCount
                            it.copy(isTaken = true, takenTimestamp = currentTime, pillCount = newPills)
                        } else it
                    }
                    repo.saveMedications(updated)
                }
            }
            AlarmController.ACTION_SNOOZE -> {
                // Instantly stop audio, vibration, and foreground service
                AlarmService.stop(context)
                AlarmController.stopAlarm(context)

                // Reschedule for snooze duration
                val repo = MedicationRepository(context)
                val settings = repo.loadSettings()
                val snoozeStr = settings["snoozeTime"] as? String ?: "10 Minutes"
                val snoozeMins = snoozeStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10

                val dummyMed = Medication(
                    id = medId,
                    name = medName,
                    dosage = medDosage,
                    time = medTime,
                    instructions = medInstructions
                )
                AlarmScheduler.scheduleAlarm(context, dummyMed, snoozeMinutesOffset = snoozeMins)
            }
            AlarmController.ACTION_DISMISS -> {
                AlarmService.stop(context)
                AlarmController.stopAlarm(context)
            }
            else -> {
                // 1. Start foreground service: guarantees sound, vibration, and heads-up notification
                // will ring continuously even if screen is off or user is outside the app
                AlarmService.start(
                    context = context,
                    medId = medId,
                    medName = medName,
                    medDosage = medDosage,
                    medTime = medTime,
                    medInstructions = medInstructions
                )

                // 2. Launch full-screen alert activity
                try {
                    val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                        putExtra("MED_ID", medId)
                        putExtra("MED_NAME", medName)
                        putExtra("MED_DOSAGE", medDosage)
                        putExtra("MED_TIME", medTime)
                        putExtra("MED_INSTRUCTIONS", medInstructions)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(alertIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
