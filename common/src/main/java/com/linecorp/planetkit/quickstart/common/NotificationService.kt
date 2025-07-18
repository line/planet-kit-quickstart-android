// Copyright 2024 LINE Plus Corporation
//
// LINE Plus Corporation licenses this file to you under the Apache License,
// version 2.0 (the "License"); you may not use this file except in compliance
// with the License. You may obtain a copy of the License at:
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations
// under the License.

package com.linecorp.planetkit.quickstart.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

abstract class NotificationService : Service() {
    companion object {
        const val ACTION_CONNECT_SESSION = "ACTION_CONNECT_SESSION"
        const val ACTION_CLEAR = "ACTION_CLEAR"
    }

    abstract fun createNotification(): Notification
    abstract val notificationId: Int
    abstract val channelId: String
    abstract val channelName: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT_SESSION -> onConnectSession()
            ACTION_CLEAR -> onClearRequested()
        }
        return START_NOT_STICKY
    }

    private fun onConnectSession() {
        createNotificationChannelImpl()
        val notification = createNotification()
        startForegroundImpl(notification)
    }

    private fun onClearRequested() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotificationChannelImpl() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(true)
                enableVibration(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundImpl(notification: Notification) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                startForeground(notificationId, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                startForeground(notificationId, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            else ->
                startForeground(notificationId, notification)
        }
    }

    protected fun buildNotification(targetActivity: Class<*>, iconResId: Int): Notification {
        val intent = Intent(this, targetActivity)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(channelName)
            .setSmallIcon(iconResId)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
