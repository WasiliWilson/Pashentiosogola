package com.example.pashentiosogola.alarm

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pashentiosogola.data.Medication
import com.example.pashentiosogola.data.db.MedicationRepository
import com.example.pashentiosogola.ui.theme.PashentiOsogolaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up screen over keyguard/lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val medId = intent.getStringExtra("MED_ID") ?: ""
        val medName = intent.getStringExtra("MED_NAME") ?: "Medication"
        val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""
        val medTime = intent.getStringExtra("MED_TIME") ?: "Scheduled Time"
        val medInstructions = intent.getStringExtra("MED_INSTRUCTIONS") ?: ""

        // Post top-of-screen heads-up notification so user sees it in notification bar
        AlarmController.sendHeadsUpNotification(
            context = this,
            medId = medId,
            medName = medName,
            medDosage = medDosage,
            medTime = medTime,
            medInstructions = medInstructions
        )

        // Play alarm audio & vibration via centralized controller if not already playing
        if (!AlarmController.isAlarmPlaying) {
            AlarmController.playAlarm(this)
        }

        setContent {
            PashentiOsogolaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top Animated Alarm Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "MEDICATION REMINDER",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 2.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Main Time Content (Prominent Big Time)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp, horizontal = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = medTime,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Text(
                                            text = "Scheduled Dose Time",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Medication Name & Dosage Badge
                                Text(
                                    text = medName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Dosage: $medDosage",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                if (medInstructions.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Instructions: $medInstructions",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                // Take Now Action Button (Instantly stops audio, vibration, notification & marks taken)
                                Button(
                                    onClick = {
                                        AlarmController.stopAlarm(this@AlarmAlertActivity)
                                        markAsTaken(medId)
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("TAKE NOW", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Snooze / Dismiss Button (Instantly stops audio & reschedules)
                                OutlinedButton(
                                    onClick = {
                                        AlarmController.stopAlarm(this@AlarmAlertActivity)
                                        snoozeAlarm(this@AlarmAlertActivity, medId, medName, medDosage, medTime, medInstructions)
                                        finish()
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.Snooze, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SNOOZE / DISMISS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun snoozeAlarm(context: Context, medId: String, medName: String, medDosage: String, medTime: String, medInstructions: String) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun markAsTaken(medId: String) {
        if (medId.isBlank()) return
        val repo = MedicationRepository(this)
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

    override fun onDestroy() {
        super.onDestroy()
        AlarmController.stopAlarm(this)
    }
}
