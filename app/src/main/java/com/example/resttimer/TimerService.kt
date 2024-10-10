package com.example.resttimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.*
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import kotlinx.coroutines.*
import android.media.RingtoneManager
import android.media.Ringtone
import android.graphics.Color


class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.example.resttimer.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.resttimer.ACTION_STOP_TIMER"
        const val ACTION_KILL = "com.example.resttimer.ACTION_KILL"
        const val DEFAULT_RUN_TIME = 90

        const val NOTIF_CHANNEL_ID = "TimerChannel"
        const val NOTIF_ID = 1
    }

    private var remoteViews: RemoteViews? = null
    private lateinit var notificationManager: NotificationManager
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var timerJob: Job? = null
    private var runtime = DEFAULT_RUN_TIME
    private var secondsRemaining = DEFAULT_RUN_TIME
    private lateinit var ringtone: Ringtone

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
        initializeRingtone()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                runtime = intent.getIntExtra("runtime", runtime) + 1
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

    private fun createNotification(): Notification {
        remoteViews = RemoteViews(packageName, R.layout.notification_timer)
        remoteViews?.setChronometer(
            R.id.chronometer,
            SystemClock.elapsedRealtime() + (secondsRemaining * 1000),
            null,
            true
        )
        remoteViews?.setTextColor(R.id.chronometer, Color.WHITE)

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
                    delay(1000)
                    secondsRemaining--
                }
                if (secondsRemaining <= 0) {
                    stopTimer()
                    playJingle()
                }
            }
        }
    }


    private fun initializeRingtone() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    private fun playJingle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = false
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0)

        ringtone.play()

        Handler(Looper.getMainLooper()).postDelayed({
            ringtone.stop()
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalVolume, 0)
        }, 3000)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        secondsRemaining = runtime
        remoteViews?.setChronometer(
            R.id.chronometer,
            SystemClock.elapsedRealtime() + runtime * 1000,
            null,
            false
        )
        notificationBuilder = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.timer_icon)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        val notification = notificationBuilder!!.build()
        notificationManager.notify(NOTIF_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
