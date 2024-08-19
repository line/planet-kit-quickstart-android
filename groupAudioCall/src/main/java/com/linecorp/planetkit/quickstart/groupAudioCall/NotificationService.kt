package com.linecorp.planetkit.quickstart.groupAudioCall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
class NotificationService : Service() {
    companion object {
        private const val ONGOING_CHANNEL_ID = "ONGOING_CHANNEL_ID"
        private const val ONGOING_CHANNEL_NAME = "Group Call in Progress"

        // This can be any dummy number
        private const val NOTIFICATION_ID = 300

        private const val ACTION_CONNECT_SESSION = "ACTION_CONNECT_SESSION"
        private const val ACTION_CLEAR = "ACTION_CLEAR"

        @Synchronized
        @JvmStatic
        fun showOngoingSession(context: Context) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_CONNECT_SESSION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @Synchronized
        @JvmStatic
        fun clear(context: Context) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_CLEAR
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        when (action) {
            ACTION_CONNECT_SESSION -> {
                onConnectSession(intent)
            }
            ACTION_CLEAR -> {
                onClearRequested()
            }
        }
        return START_NOT_STICKY
    }

    private fun onClearRequested() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }
        else @Suppress("DEPRECATION") {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun onConnectSession(incomingIntent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ONGOING_CHANNEL_ID,
                ONGOING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val newIntent = Intent(this, MainActivity::class.java)
        val pendingFlags: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, newIntent, pendingFlags)

        val builder = NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
            .setContentTitle(ONGOING_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.ic_dev_launcher)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                builder.build(),
                FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                builder.build(),
                FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }
        else {
            startForeground(
                NOTIFICATION_ID,
                builder.build()
            )
        }
    }
    override fun onBind(p0: Intent?): IBinder? = null
}