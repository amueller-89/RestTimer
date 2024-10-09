package com.example.resttimer

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import kotlinx.coroutines.*

class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.example.resttimer.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.resttimer.ACTION_STOP_TIMER"
    }

    private var timerJob: Job? = null
    private var timerSeconds = 0

    override fun onCreate() {
        super.onCreate()
        timerSeconds = 90
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                startTimer()
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
            else -> {
                //val notification = createBasicNotification()
                val notification = createNotification("90")
                startForeground(1, notification)
            }
        }
        return START_STICKY
    }

    private fun createNotification(time: String): Notification {
        val channelId = "timer_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Timer Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)


        val remoteViews = RemoteViews(packageName, R.layout.notification_timer)
        remoteViews.setTextViewText(R.id.tv_timer, time)

        val startIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_START_TIMER
        }
        val startPendingIntent = PendingIntent.getService(
            this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.btn_start, startPendingIntent)

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.timer_icon)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun startTimer() {
        if (timerJob == null) {
            timerJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    delay(1000)
                    timerSeconds--
                    val timeString = timerSeconds.toString()
                    val notification = createNotification(timeString)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager.notify(1, notification)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        timerSeconds = 90
        val notification = createNotification("90")
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
