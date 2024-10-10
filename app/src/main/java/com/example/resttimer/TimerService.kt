package com.example.resttimer

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import kotlinx.coroutines.*
import android.media.RingtoneManager
import android.media.Ringtone

class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.example.resttimer.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.resttimer.ACTION_STOP_TIMER"
        const val ACTION_KILL = "com.example.resttimer.ACTION_KILL"
        const val DEFAULT_RUN_TIME = 90

        const val NOTIF_CHANNEL_ID = "TimerChannel"
        const val NOTIF_ID = 1
        const val JINGLE_CHANNEL_ID = "JingleChannel"
        const val JINGLE_ID = 2
    }

    private var remoteViews: RemoteViews? = null
    private lateinit var notificationManager: NotificationManager
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var timerJob: Job? = null
    private var runtime = DEFAULT_RUN_TIME
    private var secondsRemaining = DEFAULT_RUN_TIME

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
        createJingleChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                runtime = intent.getIntExtra("runtime", runtime)
                secondsRemaining = runtime
                val notification = createNotification()
                startForeground(NOTIF_ID, notification)
                startTimer()
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
            ACTION_KILL -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val notification = createNotification()
                startForeground(NOTIF_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "Timer Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun createJingleChannel() {
        val channel = NotificationChannel(
            JINGLE_CHANNEL_ID,
            "Jingle",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }




    private fun sendJingleNotification() {
        val notification = NotificationCompat.Builder(this, JINGLE_CHANNEL_ID)
            .setSmallIcon(R.drawable.timer_icon)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(JINGLE_ID, notification)

        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(this, notificationUri)
        ringtone.play()

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(JINGLE_ID)
            ringtone.stop()
        }, 1000)
    }


    private fun createNotification(): Notification {
        remoteViews = RemoteViews(packageName, R.layout.notification_timer)
        remoteViews?.setTextViewText(R.id.tv_timer, secondsRemaining.toString())

        val progress = (secondsRemaining * 100) / runtime
        remoteViews?.setProgressBar(R.id.progress_bar, 100, progress, false)

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

        notificationBuilder = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.timer_icon)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        return notificationBuilder!!.build()
    }

    private fun startTimer() {
        if (timerJob == null) {
            timerJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive && secondsRemaining >= 0) {
                    updateNotification()
                    delay(1000)
                    secondsRemaining--
                }
                if (secondsRemaining < 0) {
                    stopTimer()
                    sendJingleNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val progress = (secondsRemaining * 100) / runtime
        remoteViews?.setProgressBar(R.id.progress_bar, 100, progress, false)

        remoteViews?.setTextViewText(R.id.tv_timer, secondsRemaining.toString())
        notificationManager.notify(NOTIF_ID, notificationBuilder!!.build())
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        secondsRemaining = runtime
        updateNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
