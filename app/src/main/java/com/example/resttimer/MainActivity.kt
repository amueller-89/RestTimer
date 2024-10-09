package com.example.resttimer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.resttimer.ui.theme.RestTimerTheme
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startTimerService()
            } else {
                println("permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        enableEdgeToEdge()
        setContent {
            val timerState: TimerState = viewModel()
            RestTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerView(timerState, Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val foregroundServicePermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED

            when {
                notificationPermissionGranted && foregroundServicePermissionGranted -> {
                    startTimerService()
                }
                !foregroundServicePermissionGranted -> {
                    requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startTimerService()
        }
    }


    private fun startTimerService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        startForegroundService(serviceIntent)
    }
}

@Composable
fun CenteredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 60.sp
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = fontSize
        ),
        enabled = enabled,
        modifier = modifier,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RestTimerTheme {
        TimerView(TimerState())
    }
}
