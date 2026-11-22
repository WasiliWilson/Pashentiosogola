package com.example.pashentiosogola.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pashentiosogola.ui.screens.DashboardScreen
import com.example.pashentiosogola.ui.screens.HistoryScreen
import com.example.pashentiosogola.ui.screens.RefillsScreen
import com.example.pashentiosogola.ui.screens.SettingsScreen

@Composable
fun MainScaffold(viewModel: MedicationViewModel) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    val activeAlarm by viewModel.activeAlarmMedication.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { selectedTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "medications",
                    onClick = { selectedTab = "medications" },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Medications") },
                    label = { Text("Meds") }
                )
                NavigationBarItem(
                    selected = selectedTab == "refills",
                    onClick = { selectedTab = "refills" },
                    icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Refills & Journal") },
                    label = { Text("Refills") }
                )
                NavigationBarItem(
                    selected = selectedTab == "history",
                    onClick = { selectedTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
     ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMedications = { selectedTab = "medications" },
                    onNavigateToRefills = { selectedTab = "refills" },
                    onNavigateToHistory = { selectedTab = "history" },
                    onNavigateToSettings = { selectedTab = "settings" }
                )
                "medications" -> MedicationListScreen(viewModel = viewModel)
                "refills" -> RefillsScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }

        // Active Alarm Ringing Overlay Popup (Full screen high priority alert)
        activeAlarm?.let { med ->
            AlertDialog(
                onDismissRequest = { viewModel.stopAlarm() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("🚨 Medication Due Now: ${med.name}") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Time to take your medication!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Dosage: ${med.dosage}", style = MaterialTheme.typography.bodyLarge)
                        Text("Scheduled Time: ${med.time}", style = MaterialTheme.typography.bodyMedium)
                        if (med.instructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Instructions: ${med.instructions}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleTaken(med.id)
                            viewModel.stopAlarm()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Now & Stop Alarm Audio")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.stopAlarm() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Snooze / Dismiss")
                    }
                }
            )
        }
    }
}
