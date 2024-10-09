package com.example.resttimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimerView(timerState: TimerState, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.4f))
        CenteredTextField(
            value = timerState.timerString(),
            onValueChange = { newText -> timerState.valueChange(newText) },
            enabled = !timerState.isTimerRunning.value,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!timerState.isTimerRunning.value) timerState.startTimer() else
                    timerState.resetTimer()
            },
            modifier = Modifier.fillMaxWidth(0.6f)
                .height(100.dp)
        ) {
            Text(
                text = if (timerState.isTimerRunning.value) "Reset" else "Start",
                fontSize = 24.sp
            )
        }
    }
}

class TimerState: ViewModel() {
    var defaultTime = mutableIntStateOf(90)
    var isTimerRunning = mutableStateOf(false)
    var timerValue = mutableIntStateOf(defaultTime.intValue)
    private var job: Job? = null

    fun timerString():String{
        return timerValue.intValue.toString()
    }

    fun valueChange(newText:String){
        if (!this.isTimerRunning.value) this.setValue(newText)
    }

    fun setValue(value: String){
        value.toIntOrNull()?.let {
            defaultTime.intValue = it
            timerValue.intValue = it
        }
    }

    fun startTimer() {
        isTimerRunning.value = true
        timerValue.intValue = defaultTime.intValue
        job = viewModelScope.launch {
            while (timerValue.intValue > 0) {
                delay(1000)
                timerValue.intValue--
            }
            resetTimer()
        }
    }

    fun resetTimer() {
        job?.cancel()
        timerValue.intValue = defaultTime.intValue
        isTimerRunning.value = false
    }
}