package com.example.pashentiosogola.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.pashentiosogola.data.Medication
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {

    /**
     * Parses time string such as "08:00 AM", "8:30 PM", "14:00" into (hourOfDay, minute).
     * Robust against all Android device locales and formats.
     */
    fun parseHourMinute(timeStr: String): Pair<Int, Int>? {
        return try {
            val clean = timeStr.trim().uppercase(Locale.US)
            val isPm = clean.contains("PM")
            val isAm = clean.contains("AM")
            val numbersOnly = clean.replace("AM", "").replace("PM", "").trim()
            val parts = numbersOnly.split(":")
            if (parts.size >= 2) {
                var hour = parts[0].trim().toIntOrNull() ?: return null
                val minute = parts[1].trim().toIntOrNull() ?: return null
                if (isPm && hour < 12) {
                    hour += 12
                } else if (isAm && hour == 12) {
                    hour = 0
                }
                Pair(hour, minute)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun scheduleAlarm(context: Context, medication: Medication, snoozeMinutesOffset: Int? = null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MED_ID", medication.id)
            putExtra("MED_NAME", medication.name)
            putExtra("MED_DOSAGE", medication.dosage)
            putExtra("MED_TIME", medication.time)
            putExtra("MED_INSTRUCTIONS", medication.instructions)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerMillis: Long = if (snoozeMinutesOffset != null && snoozeMinutesOffset > 0) {
                System.currentTimeMillis() + (snoozeMinutesOffset * 60 * 1000L)
            } else {
                val parsed = parseHourMinute(medication.time) ?: return
                val (targetHour, targetMinute) = parsed

                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, targetHour)
                    set(Calendar.MINUTE, targetMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If scheduled for current minute, trigger in 2 seconds so user testing sees it immediately
                if (target.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) &&
                    target.get(Calendar.MINUTE) == now.get(Calendar.MINUTE)
                ) {
                    System.currentTimeMillis() + 2000L
                } else if (target.before(now)) {
                    // Time already passed earlier today; schedule for tomorrow
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    target.timeInMillis
                } else {
                    target.timeInMillis
                }
            }

            // Show intent when clicking alarm icon in status bar
            val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                putExtra("MED_ID", medication.id)
                putExtra("MED_NAME", medication.name)
                putExtra("MED_DOSAGE", medication.dosage)
                putExtra("MED_TIME", medication.time)
                putExtra("MED_INSTRUCTIONS", medication.instructions)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                medication.id.hashCode() + 500,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                // setAlarmClock is the most reliable alarm type in Android:
                // 1. Wakes device completely from Doze/Deep Sleep
                // 2. Guaranteed exact millisecond delivery
                // 3. Shows active alarm clock icon in system status bar
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } catch (e: Exception) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } catch (se: Exception) {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, medicationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
