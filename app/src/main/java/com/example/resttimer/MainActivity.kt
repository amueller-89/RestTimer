package com.example.resttimer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
                        start = this::startService,
                        stop = ::stopService)
                }
            }
        }
    }

    private fun startService(seconds:String){
        if(permissionsDenied){
            checkPermissions()
        }
        startTimerService(seconds)
    }

    private fun stopService(){
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_KILL
        }
        startForegroundService(serviceIntent)
    }

    private fun startTimerService(seconds: String) {
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            putExtra("runtime", seconds.toInt())
            action = TimerService.ACTION_START_TIMER
        }
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
fun TimerView(modifier: Modifier = Modifier, start: (String) -> Unit, stop: () -> Unit) {
    var textFieldValue by remember { mutableStateOf("5") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.3f))
        CenteredTextField2(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        ElevatedButton(
            onClick = {
                start(textFieldValue)
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(100.dp)
        ) {
            Text(
                text = "START",
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedButton(
            onClick = {
                stop()
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(100.dp)
        ) {
            Text(
                text = "STOP",
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

@Composable
fun CenteredTextField2(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 100.sp,
    textColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black,
    cursorColor: Color = Color.Blue,
    placeholder: String = ""
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = textColor,
            textAlign = TextAlign.Center,
            fontSize = fontSize
        ),
        modifier = modifier
            .padding(16.dp),
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            innerTextField()
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
    )
}



@Preview( showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES, // Enable dark mode
    name = "Dark Mode Preview")
@Composable
fun GreetingPreview() {
    RestTimerTheme {
        TimerView(modifier = Modifier, start = {}, stop = {})
    }
}

