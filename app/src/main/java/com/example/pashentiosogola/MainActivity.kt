package com.example.pashentiosogola

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pashentiosogola.ui.MainScaffold
import com.example.pashentiosogola.ui.MedicationViewModel
import com.example.pashentiosogola.ui.theme.PashentiOsogolaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
