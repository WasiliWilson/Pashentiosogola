package com.example.pashentiosogola.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pashentiosogola.data.db.MedicationRepository

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra("MED_ID") ?: ""
        val medName = intent.getStringExtra("MED_NAME") ?: "Medication"
        val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""
        val medTime = intent.getStringExtra("MED_TIME") ?: ""
        val medInstructions = intent.getStringExtra("MED_INSTRUCTIONS") ?: ""

        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            putExtra("MED_DOSAGE", medDosage)
            putExtra("MED_TIME", medTime)
            putExtra("MED_INSTRUCTIONS", medInstructions)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(alertIntent)
    }
}
