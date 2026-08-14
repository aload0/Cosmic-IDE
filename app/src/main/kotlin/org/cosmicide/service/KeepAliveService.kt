/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import org.cosmicide.MainActivity
import org.cosmicide.R

/**
 * Foreground service that keeps the app process alive in the background, like Termux.
 */
class KeepAliveService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NotificationId, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NotificationId, buildNotification())
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundCompat() = onStartCommand(null, 0, 0)

    private fun buildNotification(): Notification {
        val channelId = "keepalive"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Foreground service", NotificationManager.IMPORTANCE_MIN
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.keep_alive_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        private const val NotificationId = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, KeepAliveService::class.java))
        }
    }
}