package com.example.pashentiosogola

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pashentiosogola.alarm.AlarmController
import com.example.pashentiosogola.ui.MainScaffold
import com.example.pashentiosogola.ui.MedicationViewModel
import com.example.pashentiosogola.ui.theme.PashentiOsogolaTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for top-of-screen heads-up alerts
        AlarmController.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: MedicationViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                "Light Mode" -> false
                "Dark Mode" -> true
                else -> isSystemInDarkTheme()
            }
            PashentiOsogolaTheme(darkTheme = darkTheme) {
                MainScaffold(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScaffold() {
    val viewModel: MedicationViewModel = viewModel()
    val themeMode by viewModel.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        "Light Mode" -> false
        "Dark Mode" -> true
        else -> isSystemInDarkTheme()
    }
    PashentiOsogolaTheme(darkTheme = darkTheme) {
        MainScaffold(viewModel = viewModel)
    }
}
