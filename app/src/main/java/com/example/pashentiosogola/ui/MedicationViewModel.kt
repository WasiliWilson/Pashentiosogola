package com.example.pashentiosogola.ui

import android.app.Application
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pashentiosogola.alarm.AlarmController
import com.example.pashentiosogola.alarm.AlarmScheduler
import com.example.pashentiosogola.data.Medication
import com.example.pashentiosogola.data.db.MedicationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicationRepository(application)
    private val initialSettings = repository.loadSettings()

    // Navigation tab state: "dashboard", "medications", "refills", "history", "settings"
    private val _currentTab = MutableStateFlow("dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Medications list loaded from persistent local repository
    private val _medications = MutableStateFlow<List<Medication>>(repository.loadMedications())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    // Health notes / symptom journal logs
    private val _healthNotes = MutableStateFlow<List<String>>(
        listOf("Day 1: Felt energetic, blood pressure stable.", "Day 3: Mild headache in morning.")
    )
    val healthNotes: StateFlow<List<String>> = _healthNotes.asStateFlow()

    // Filter state for medications screen
    private val _filter = MutableStateFlow("All") // "All", "Pending", "Taken"
    val filter: StateFlow<String> = _filter.asStateFlow()

    // Active alarm ringing state
    private val _activeAlarmMedication = MutableStateFlow<Medication?>(null)
    val activeAlarmMedication: StateFlow<Medication?> = _activeAlarmMedication.asStateFlow()

    // Settings state
    private val _alarmTone = MutableStateFlow(initialSettings["alarmTone"] as? String ?: "Default Alarm")
    val alarmTone: StateFlow<String> = _alarmTone.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(initialSettings["vibrationEnabled"] as? Boolean ?: true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(initialSettings["soundEnabled"] as? Boolean ?: true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _snoozeTime = MutableStateFlow(initialSettings["snoozeTime"] as? String ?: "10 Minutes")
    val snoozeTime: StateFlow<String> = _snoozeTime.asStateFlow()

    private val _themeMode = MutableStateFlow(initialSettings["themeMode"] as? String ?: "System")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _emergencyContact = MutableStateFlow(initialSettings["emergencyContact"] as? String ?: "Dr. Smith (555-0199)")
    val emergencyContact: StateFlow<String> = _emergencyContact.asStateFlow()

    private var previewRingtone: Ringtone? = null
    private var timeCheckJob: Job? = null

    init {
        AlarmController.createNotificationChannel(application)
        startTimeChecker()
        for (med in _medications.value) {
            if (!med.isTaken) {
                AlarmScheduler.scheduleAlarm(application, med)
            }
        }
    }

    private fun persistSettings() {
        repository.saveSettings(
            alarmTone = _alarmTone.value,
            soundEnabled = _soundEnabled.value,
            vibrationEnabled = _vibrationEnabled.value,
            snoozeTime = _snoozeTime.value,
            themeMode = _themeMode.value,
            emergencyContact = _emergencyContact.value
        )
    }

    private fun startTimeChecker() {
        timeCheckJob = viewModelScope.launch {
            while (true) {
                delay(15_000L)
                checkAlarms()
            }
        }
    }

    private fun checkAlarms() {
        if (_activeAlarmMedication.value != null) return

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date()).uppercase(Locale.getDefault())

        val pending = _medications.value.filter { !it.isTaken }
        for (med in pending) {
            val medTime = med.time.trim().uppercase(Locale.getDefault())
            if (medTime == currentTime) {
                triggerAlarm(getApplication(), med)
                break
            }
        }
    }

    fun previewSound(context: Context, tone: String) {
        try {
            stopPreviewSound()
            val uri: Uri = when (tone) {
                "Default Notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "Gentle Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Classic Beep" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "Marimba Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Digital Buzzer" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            previewRingtone = RingtoneManager.getRingtone(context.applicationContext, uri)
            previewRingtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPreviewSound() {
        try {
            previewRingtone?.stop()
            previewRingtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCurrentTab(tab: String) {
        _currentTab.value = tab
    }

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }

    fun setAlarmTone(tone: String) {
        _alarmTone.value = tone
        persistSettings()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        persistSettings()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        persistSettings()
    }

    fun setSnoozeTime(duration: String) {
        _snoozeTime.value = duration
        persistSettings()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        persistSettings()
    }

    fun setEmergencyContact(contact: String) {
        _emergencyContact.value = contact
        persistSettings()
    }

    fun addMedication(
        name: String,
        dosage: String,
        time: String,
        instructions: String = "",
        category: String = "Morning",
        pillCount: Int = 30
    ) {
        val newMed = Medication(
            id = System.currentTimeMillis().toString(),
            name = name.trim(),
            dosage = dosage.trim(),
            time = time.trim(),
            instructions = instructions.trim(),
            category = category,
            pillCount = pillCount
        )

        _medications.update { current ->
            val updated = current + newMed
            repository.saveMedications(updated)
            updated
        }

        AlarmScheduler.scheduleAlarm(getApplication(), newMed)
    }

    fun updateMedication(
        id: String,
        name: String,
        dosage: String,
        time: String,
        instructions: String = "",
        category: String = "Morning"
    ) {
        _medications.update { current ->
            val updated = current.map {
                if (it.id == id) {
                    it.copy(
                        name = name.trim(),
                        dosage = dosage.trim(),
                        time = time.trim(),
                        instructions = instructions.trim(),
                        category = category
                    )
                } else it
            }
            repository.saveMedications(updated)
            updated
        }

        val updatedMed = _medications.value.find { it.id == id }
        if (updatedMed != null && !updatedMed.isTaken) {
            AlarmScheduler.scheduleAlarm(getApplication(), updatedMed)
        }
    }

    fun toggleTaken(id: String) {
        // Stop audio/alarm if this med is ringing
        stopAlarm()

        val timeFormat = SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        _medications.update { current ->
            val updated = current.map {
                if (it.id == id) {
                    val newTaken = !it.isTaken
                    val newPills = if (newTaken && it.pillCount > 0) it.pillCount - 1 else it.pillCount
                    it.copy(
                        isTaken = newTaken,
                        takenTimestamp = if (newTaken) currentTime else null,
                        pillCount = newPills
                    )
                } else it
            }
            repository.saveMedications(updated)
            updated
        }
    }

    fun refillMedication(id: String, addedPills: Int = 30) {
        _medications.update { current ->
            val updated = current.map {
                if (it.id == id) it.copy(pillCount = it.pillCount + addedPills) else it
            }
            repository.saveMedications(updated)
            updated
        }
    }

    fun deleteMedication(id: String) {
        AlarmScheduler.cancelAlarm(getApplication(), id)
        _medications.update { current ->
            val updated = current.filter { it.id != id }
            repository.saveMedications(updated)
            updated
        }
    }

    fun addHealthNote(note: String) {
        if (note.isBlank()) return
        val timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        _healthNotes.update { current -> listOf("[$timestamp] $note") + current }
    }

    fun triggerAlarm(context: Context, medication: Medication) {
        _activeAlarmMedication.value = medication

        // Send heads-up notification at top of phone screen
        AlarmController.sendHeadsUpNotification(
            context = context,
            medId = medication.id,
            medName = medication.name,
            medDosage = medication.dosage,
            medTime = medication.time,
            medInstructions = medication.instructions
        )

        // Play alarm sound and vibration via AlarmController
        if (_soundEnabled.value || _vibrationEnabled.value) {
            AlarmController.playAlarm(context, _alarmTone.value)
        }
    }

    fun stopAlarm() {
        AlarmController.stopAlarm(getApplication())
        _activeAlarmMedication.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
        stopPreviewSound()
        timeCheckJob?.cancel()
    }
}
