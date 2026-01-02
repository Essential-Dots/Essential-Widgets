package com.essentialwidgets.org.widgets

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.essentialwidgets.org.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class ClockUpdateService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var timer: Timer? = null
    private val glanceAppWidget = NdotDigitalWidget()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithNotification()
        startClockUpdates()
        // If the service is killed, restart it
        return START_STICKY
    }

    private fun startClockUpdates() {

        timer?.cancel() // Cancel any existing timer
        timer = Timer()

        // gets the current time and date
        val calendar = java.util.Calendar.getInstance()

        // calculate how many milliseconds are left until the next starts
        val secondsUntilNextMinute = 60 - calendar.get(java.util.Calendar.SECOND)

        // convertion from seconds to milliseconds
        val delay = secondsUntilNextMinute * 1000L

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                coroutineScope.launch {
                    // Find all placed widgets
                    val manager = GlanceAppWidgetManager(applicationContext)
                    val glanceIds = manager.getGlanceIds(NdotDigitalWidget::class.java)

                    if (glanceIds.isNotEmpty()) {
                        // Update the state for each widget
                        glanceIds.forEach { glanceId ->
                            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                                prefs[NdotDigitalWidget.timeKey] = System.currentTimeMillis()
                            }
                            // Tell the widget to redraw itself
                            glanceAppWidget.update(applicationContext, glanceId)
                        }
                    } else {
                        // No widgets left, so stop the service
                        stopSelf()
                    }
                }
            }
        }, delay, 60000L) // Update every 60,000 ms (1 minute)
    }

    private fun startForegroundWithNotification() {
        val channelId = "clock_widget_update_channel"
        val channelName = "Clock Widget Service"
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Create a notification channel for Android 8.0+
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.description = "Keeps the clock widget updated."
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Essential Widget")
            .setContentText("Clock service is running.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this drawable exists
            .setOngoing(true)
            .build()

        // The ID must be a non-zero integer
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        coroutineScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't use binding, so return null
        return null
    }
}
