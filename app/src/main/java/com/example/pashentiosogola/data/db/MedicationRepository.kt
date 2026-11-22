package com.example.pashentiosogola.data.db

import android.content.Context
import com.example.pashentiosogola.data.Medication
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MedicationRepository(context: Context) {
    private val file = File(context.filesDir, "wodwala_medications.json")
    private val settingsFile = File(context.filesDir, "wodwala_settings.json")

    fun loadMedications(): List<Medication> {
        if (!file.exists()) {
            val defaults = listOf(
                Medication(name = "Aspirin", dosage = "100mg", time = "08:00 AM", instructions = "Take with food", category = "Morning", pillCount = 12),
                Medication(name = "Metformin", dosage = "500mg", time = "01:00 PM", instructions = "Take after lunch", category = "Afternoon", pillCount = 5),
                Medication(name = "Vitamin D", dosage = "1000IU", time = "08:00 PM", instructions = "Take before bed", category = "Bedtime", pillCount = 20)
            )
            saveMedications(defaults)
            return defaults
        }

        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Medication>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Medication(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        dosage = obj.getString("dosage"),
                        time = obj.getString("time"),
                        instructions = obj.getString("instructions"),
                        category = obj.optString("category", "Morning"),
                        pillCount = obj.optInt("pillCount", 30),
                        isTaken = obj.getBoolean("isTaken"),
                        takenTimestamp = if (obj.has("takenTimestamp") && !obj.isNull("takenTimestamp")) obj.getString("takenTimestamp") else null
                    )
                )
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun saveMedications(medications: List<Medication>) {
        try {
            val jsonArray = JSONArray()
            for (med in medications) {
                val obj = JSONObject().apply {
                    put("id", med.id)
                    put("name", med.name)
                    put("dosage", med.dosage)
                    put("time", med.time)
                    put("instructions", med.instructions)
                    put("category", med.category)
                    put("pillCount", med.pillCount)
                    put("isTaken", med.isTaken)
                    put("takenTimestamp", med.takenTimestamp ?: JSONObject.NULL)
                }
                jsonArray.put(obj)
            }
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSettings(): Map<String, Any> {
        if (!settingsFile.exists()) {
            return mapOf(
                "alarmTone" to "Default Alarm",
                "soundEnabled" to true,
                "vibrationEnabled" to true,
                "snoozeTime" to "10 Minutes",
                "themeMode" to "System",
                "emergencyContact" to "Dr. Smith (555-0199)"
            )
        }
        try {
            val obj = JSONObject(settingsFile.readText())
            return mapOf(
                "alarmTone" to obj.optString("alarmTone", "Default Alarm"),
                "soundEnabled" to obj.optBoolean("soundEnabled", true),
                "vibrationEnabled" to obj.optBoolean("vibrationEnabled", true),
                "snoozeTime" to obj.optString("snoozeTime", "10 Minutes"),
                "themeMode" to obj.optString("themeMode", "System"),
                "emergencyContact" to obj.optString("emergencyContact", "Dr. Smith (555-0199)")
            )
        } catch (e: Exception) {
            return mapOf(
                "alarmTone" to "Default Alarm",
                "soundEnabled" to true,
                "vibrationEnabled" to true,
                "snoozeTime" to "10 Minutes",
                "themeMode" to "System",
                "emergencyContact" to "Dr. Smith (555-0199)"
            )
        }
    }

    fun saveSettings(alarmTone: String, soundEnabled: Boolean, vibrationEnabled: Boolean, snoozeTime: String, themeMode: String, emergencyContact: String) {
        try {
            val obj = JSONObject().apply {
                put("alarmTone", alarmTone)
                put("soundEnabled", soundEnabled)
                put("vibrationEnabled", vibrationEnabled)
                put("snoozeTime", snoozeTime)
                put("themeMode", themeMode)
                put("emergencyContact", emergencyContact)
            }
            settingsFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
