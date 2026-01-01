package com.essentialwidgets.org.widgets

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NdotDigitalWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NdotDigitalWidget()

    // This is called when the first instance of the widget is placed.
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start the service to begin updates.
        context.startForegroundService(Intent(context, ClockUpdateService::class.java))
    }

    // This is called when the last instance of the widget is removed.
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Stop the service to save battery.
        context.stopService(Intent(context, ClockUpdateService::class.java))
    }
}
