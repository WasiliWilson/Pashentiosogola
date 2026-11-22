package com.example.pashentiosogola.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pashentiosogola.R
import com.example.pashentiosogola.data.Medication

@Composable
fun MedicationListScreen(
    viewModel: MedicationViewModel
) {
    val context = LocalContext.current
    val medications by viewModel.medications.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }

    val takenCount = medications.count { it.isTaken }
    val totalCount = medications.size
    val adherenceRate = if (totalCount > 0) (takenCount.toFloat() / totalCount) else 0f

    val filteredMedications = when (filter) {
        "Pending" -> medications.filter { !it.isTaken }
        "Taken" -> medications.filter { it.isTaken }
        else -> medications
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Clean Title Header identical to History and Settings pages
            item {
                Text(
                    text = "Medication Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Adherence Header Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Adherence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$takenCount of $totalCount medications taken",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CircularProgressIndicator(
                                progress = { adherenceRate },
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { adherenceRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pending", "Taken").forEach { tab ->
                        FilterChip(
                            selected = filter == tab,
                            onClick = { viewModel.setFilter(tab) },
                            label = { Text(tab) },
                            leadingIcon = if (filter == tab) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Medication List
            if (filteredMedications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_wodwala_logo),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No medications found in '$filter'",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredMedications, key = { it.id }) { medication ->
                    MedicationCard(
                        medication = medication,
                        onToggleTaken = { viewModel.toggleTaken(medication.id) },
                        onDelete = { viewModel.deleteMedication(medication.id) },
                        onEdit = { editingMedication = medication },
                        onTestAlarm = { viewModel.triggerAlarm(context, medication) }
                    )
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Medication") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .semantics { contentDescription = "Add new medication" }
        )

        // Add Dialog
        if (showAddDialog) {
            AddMedicationDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, dosage, time, instructions, category ->
                    viewModel.addMedication(name, dosage, time, instructions, category)
                    showAddDialog = false
                }
            )
        }

        // Edit Dialog
        editingMedication?.let { med ->
            AddMedicationDialog(
                medicationToEdit = med,
                onDismiss = { editingMedication = null },
                onSave = { name, dosage, time, instructions, category ->
                    viewModel.updateMedication(med.id, name, dosage, time, instructions, category)
                    editingMedication = null
                }
            )
        }
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onToggleTaken: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTestAlarm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${medication.name}, dosage ${medication.dosage}, at ${medication.time}, status ${if (medication.isTaken) "Taken" else "Pending"}"
            },
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isTaken)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (medication.isTaken) 1.dp else 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = medication.isTaken,
                onCheckedChange = { onToggleTaken() },
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Mark ${medication.name} as ${if (medication.isTaken) "not taken" else "taken"}" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (medication.isTaken) TextDecoration.LineThrough else null,
                    color = if (medication.isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${medication.time} • ${medication.dosage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (medication.instructions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = medication.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Edit Button
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "Edit ${medication.name}" }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Test Alarm Bell Button
            IconButton(
                onClick = onTestAlarm,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "Test ring alarm for ${medication.name}" }
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Test Alarm",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "Delete ${medication.name}" }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
