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
        const val DEFAULT_RUN_TIME = 90
    }

    private var remoteViews: RemoteViews? = null
    private lateinit var notificationManager: NotificationManager
    private val notificationChannelId = "TimerChannel"
    private val notificationId = 1
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var timerJob: Job? = null
    private var timerSeconds = DEFAULT_RUN_TIME

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
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
                val notification = createNotification()
                startForeground(notificationId, notification)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            notificationChannelId,
            "Timer Notifications",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        remoteViews = RemoteViews(packageName, R.layout.notification_timer)
        remoteViews?.setTextViewText(R.id.tv_timer, timerSeconds.toString())

        val startIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_START_TIMER
        }
        val startPendingIntent = PendingIntent.getService(
            this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews?.setOnClickPendingIntent(R.id.btn_start, startPendingIntent)

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews?.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent)

        notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.timer_icon)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)

        return notificationBuilder!!.build()
    }


    private fun startTimer() {
        if (timerJob == null) {
            timerJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive && timerSeconds > 0) {
                    updateNotification()
                    delay(1000)
                    timerSeconds--
                }
                if (timerSeconds <= 0) {
                    stopTimer()
                }
            }
        }
    }

    private fun updateNotification() {
        notificationBuilder?.let { builder ->
            builder.setContentTitle("Timer")
                .setContentText("Time remaining: $timerSeconds seconds")

            // Reuse the existing RemoteViews instance
            remoteViews?.setTextViewText(R.id.tv_timer, timerSeconds.toString())

            // Update the notification with the modified RemoteViews
            notificationManager.notify(notificationId, builder.build())
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        timerSeconds = DEFAULT_RUN_TIME
        updateNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
