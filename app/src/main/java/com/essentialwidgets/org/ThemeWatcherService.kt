package com.essentialwidgets.org

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class ThemeWatcherService : Service() {

    private val themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                val appWidgetManager = AppWidgetManager.getInstance(context)

                // Update clock widget
                appWidgetManager.getAppWidgetIds(
                    ComponentName(context, ClockWidgetProvider::class.java)
                ).forEach { updateClockWidget(context, appWidgetManager, it) }

                // Update alarm widget
                appWidgetManager.getAppWidgetIds(
                    ComponentName(context, AlarmWidgetProvider::class.java)
                ).forEach { updateAlarmWidget(context, appWidgetManager, it) }

                // Update calendar widget
                appWidgetManager.getAppWidgetIds(
                    ComponentName(context, CalendarWidgetProvider::class.java)
                ).forEach { updateCalendarWidget(context, appWidgetManager, it) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        registerReceiver(themeReceiver, IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // riavvia automaticamente se il sistema lo termina
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(themeReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "theme_watcher"
        val channel = NotificationChannel(
            channelId,
            "Widget Service",
            NotificationManager.IMPORTANCE_MIN // importanza minima, notifica silenziosa
        ).apply {
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Essential Widgets")
            .setContentText("Keeping widgets up to date")
            .setSmallIcon(R.drawable.ic_alarm_on)
            .setOngoing(true) // non rimovibile dall'utente
            .build()
    }
}