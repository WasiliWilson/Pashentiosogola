package com.example.pashentiosogola.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

class AlarmService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM
        val medId = intent?.getStringExtra("MED_ID") ?: ""
        val medName = intent?.getStringExtra("MED_NAME") ?: "Medication"
        val medDosage = intent?.getStringExtra("MED_DOSAGE") ?: ""
        val medTime = intent?.getStringExtra("MED_TIME") ?: ""
        val medInstructions = intent?.getStringExtra("MED_INSTRUCTIONS") ?: ""

        when (action) {
            ACTION_STOP_ALARM -> {
                AlarmController.stopAlarm(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_START_ALARM -> {
                // Build Heads-Up notification for persistent foreground service
                val notification = AlarmController.buildHeadsUpNotification(
                    context = this,
                    medId = medId,
                    medName = medName,
                    medDosage = medDosage,
                    medTime = medTime,
                    medInstructions = medInstructions
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        AlarmController.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(AlarmController.NOTIFICATION_ID, notification)
                }

                // Play persistent alarm sound & vibration
                AlarmController.playAlarm(this)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmController.stopAlarm(this)
    }

    companion object {
        const val ACTION_START_ALARM = "com.example.pashentiosogola.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.pashentiosogola.ACTION_STOP_ALARM"

        fun start(context: Context, medId: String, medName: String, medDosage: String, medTime: String, medInstructions: String) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra("MED_ID", medId)
                putExtra("MED_NAME", medName)
                putExtra("MED_DOSAGE", medDosage)
                putExtra("MED_TIME", medTime)
                putExtra("MED_INSTRUCTIONS", medInstructions)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, AlarmService::class.java).apply {
                    action = ACTION_STOP_ALARM
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
