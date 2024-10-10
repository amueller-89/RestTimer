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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.resttimer.ui.theme.RestTimerTheme


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                permissionsDenied = true
            }
        }

    private var permissionsDenied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        enableEdgeToEdge()
        setContent {
            RestTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerView(
                        modifier = Modifier.padding(innerPadding),
                        btnClick = ::buttonClick )
                }
            }
        }
    }

    private fun buttonClick(seconds:String){
        if(permissionsDenied){
            checkPermissions()
        }
        startTimerService(seconds)
    }


    private fun startTimerService(seconds: String) {
        val serviceIntent = Intent(this, TimerService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val foregroundServicePermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED

            when {
                !notificationPermissionGranted -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                !foregroundServicePermissionGranted -> {
                    requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
                }
                else -> {
                    permissionsDenied = false
                }
            }
        }
    }
}

@Composable
fun TimerView(modifier: Modifier = Modifier, btnClick: (String) -> Unit) {
    var textFieldValue by remember { mutableStateOf("90") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.4f))
        CenteredTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                btnClick(textFieldValue)
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(100.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 24.sp
            )
        }
    }
}



@Composable
fun CenteredTextField(
    value: String,
    onValueChange: (String) -> Unit,
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
        TimerView(modifier = Modifier){}
    }
}

