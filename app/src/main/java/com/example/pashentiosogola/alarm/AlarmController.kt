package com.example.pashentiosogola.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.pashentiosogola.R
import com.example.pashentiosogola.data.db.MedicationRepository

object AlarmController {
    const val CHANNEL_ID = "medication_alarm_heads_up_v2"
    const val NOTIFICATION_ID = 1001

    const val ACTION_TAKE_NOW = "com.example.pashentiosogola.ACTION_TAKE_NOW"
    const val ACTION_SNOOZE = "com.example.pashentiosogola.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.example.pashentiosogola.ACTION_DISMISS"

    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    var isAlarmPlaying: Boolean = false
        private set

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority heads-up alerts and sound for scheduled medications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun buildHeadsUpNotification(
        context: Context,
        medId: String,
        medName: String,
        medDosage: String,
        medTime: String,
        medInstructions: String
    ): Notification {
        createNotificationChannel(context)

        // Intent when clicking the notification body -> Opens full-screen AlarmAlertActivity
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            putExtra("MED_DOSAGE", medDosage)
            putExtra("MED_TIME", medTime)
            putExtra("MED_INSTRUCTIONS", medInstructions)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for TAKE NOW action button right in the notification banner
        val takeNowIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TAKE_NOW
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
        }
        val takeNowPendingIntent = PendingIntent.getBroadcast(
            context,
            medId.hashCode() + 101,
            takeNowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for SNOOZE action button right in the notification banner
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            putExtra("MED_DOSAGE", medDosage)
            putExtra("MED_TIME", medTime)
            putExtra("MED_INSTRUCTIONS", medInstructions)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            medId.hashCode() + 202,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("Scheduled: $medTime")
            if (medDosage.isNotBlank()) append(" • Dosage: $medDosage")
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wodwala_logo)
            .setContentTitle("🚨 Take Medication: $medName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$contentText\n${if (medInstructions.isNotBlank()) "Instructions: $medInstructions\n" else ""}Tap to open or choose an action below:"
            ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri, AudioManager.STREAM_ALARM)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setFullScreenIntent(contentPendingIntent, true) // Heads-up pop-up at top of screen
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_wodwala_logo, "✓ TAKE NOW", takeNowPendingIntent)
            .addAction(R.drawable.ic_wodwala_logo, "⏰ SNOOZE", snoozePendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()
    }

    fun sendHeadsUpNotification(
        context: Context,
        medId: String,
        medName: String,
        medDosage: String,
        medTime: String,
        medInstructions: String
    ) {
        try {
            val notification = buildHeadsUpNotification(
                context, medId, medName, medDosage, medTime, medInstructions
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAlarm(context: Context, tone: String? = null) {
        try {
            stopAlarm(context)

            val repository = MedicationRepository(context)
            val settings = repository.loadSettings()
            val soundEnabled = settings["soundEnabled"] as? Boolean ?: true
            val vibrationEnabled = settings["vibrationEnabled"] as? Boolean ?: true
            val effectiveTone = tone ?: (settings["alarmTone"] as? String ?: "Default Alarm")

            // Wake the screen up
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                @Suppress("DEPRECATION")
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.ON_AFTER_RELEASE,
                    "WodwalaOtsogola:AlarmLock"
                )
                wakeLock?.acquire(3 * 60 * 1000L) // 3 mins
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Start Vibration
            if (vibrationEnabled) {
                try {
                    vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    val pattern = longArrayOf(0, 1000, 800, 1000, 800)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, 0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Play Audio
            if (soundEnabled) {
                val uri: Uri = when (effectiveTone) {
                    "Default Notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    "Gentle Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    "Classic Beep" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    "Marimba Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    "Digital Buzzer" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }

                try {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setDataSource(context.applicationContext, uri)
                        isLooping = true
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    // Fallback to RingtoneManager
                    try {
                        ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                        ringtone?.play()
                    } catch (re: Exception) {
                        re.printStackTrace()
                    }
                }
            }

            isAlarmPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarm(context: Context) {
        try {
            // Stop media player
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) {
                // Ignore
            }
            try {
                mediaPlayer?.reset()
                mediaPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer = null

            // Stop ringtone fallback
            try {
                ringtone?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ringtone = null

            // Stop vibrator
            try {
                vibrator?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            vibrator = null

            // Release wake lock
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            wakeLock = null

            // Cancel notification
            cancelNotification(context)

            isAlarmPlaying = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
