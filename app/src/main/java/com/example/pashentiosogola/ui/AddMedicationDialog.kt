package com.example.pashentiosogola.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pashentiosogola.data.Medication
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    medicationToEdit: Medication? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, time: String, instructions: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf(medicationToEdit?.name ?: "") }
    var dosage by remember { mutableStateOf(medicationToEdit?.dosage ?: "") }
    var time by remember { mutableStateOf(medicationToEdit?.time ?: "08:00 AM") }
    var instructions by remember { mutableStateOf(medicationToEdit?.instructions ?: "") }
    var category by remember { mutableStateOf(medicationToEdit?.category ?: "Morning") }
    var expanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categories = listOf("Morning", "Afternoon", "Evening", "Bedtime")
    val quickTimes = listOf("08:00 AM", "12:00 PM", "06:00 PM", "09:00 PM")
    val isEditing = medicationToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Medication" else "Add New Medication",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name *") },
                    placeholder = { Text("e.g. Paracetamol, Amoxicillin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    placeholder = { Text("e.g. 500mg, 1 tablet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Professional Time Selector Section
                Text(
                    text = "Scheduled Dose Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Clickable Time Display Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = time,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        FilledTonalButton(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Change Time", fontSize = 12.sp)
                        }
                    }
                }

                // Quick One-Tap Time Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTimes.forEach { preset ->
                        FilterChip(
                            selected = time == preset,
                            onClick = { time = preset },
                            label = { Text(preset.replace(" ", ""), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    placeholder = { Text("e.g. Take after meal with water") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category / Slot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, dosage, time, instructions, category)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Save Medication" else "Add Medication", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Professional Material 3 TimePicker Dialog (Interactive Clock Dial + Direct Keyboard Input)
    if (showTimePicker) {
        Material3TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                time = selectedTime
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var initialHour = 8
    var initialMinute = 0
    try {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = format.parse(initialTime)
        if (date != null) {
            val cal = Calendar.getInstance().apply { time = date }
            initialHour = cal.get(Calendar.HOUR_OF_DAY)
            initialMinute = cal.get(Calendar.MINUTE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var showKeyboard by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { showKeyboard = !showKeyboard }) {
                    Icon(
                        imageVector = if (showKeyboard) Icons.Default.Schedule else Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard / Dial mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showKeyboard) {
                    TimeInput(state = timePickerState)
                } else {
                    TimePicker(state = timePickerState)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val hour = timePickerState.hour
                val minute = timePickerState.minute
                val isAm = hour < 12
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val formatted = String.format(
                    Locale.getDefault(),
                    "%02d:%02d %s",
                    displayHour,
                    minute,
                    if (isAm) "AM" else "PM"
                )
                onTimeSelected(formatted)
                onDismiss()
            }) {
                Text("Set Time", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
