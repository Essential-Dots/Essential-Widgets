package com.essentialwidgets.org

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.provider.AlarmClock
import android.widget.RemoteViews
import java.util.Calendar

class AnalogDigitalClockWidgetProvider : AppWidgetProvider() {

    /** Called by the system when the widget needs to be updated */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        context.startForegroundService(Intent(context, ThemeWatcherService::class.java))
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, AnalogDigitalClockWidgetProvider::class.java))
            .forEach { updateAnalogDigitalClockWidget(context, appWidgetManager, it) }

        // Use shared helpers from WidgetExtensions — no dedicated schedule function needed
        scheduleNextMinuteUpdate(
            context,
            buildWidgetUpdateIntent(context, AnalogDigitalClockWidgetProvider::class.java, 3)
        )
    }

    /** Re-renders the widget when the system theme changes */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, AnalogDigitalClockWidgetProvider::class.java))
                .forEach { updateAnalogDigitalClockWidget(context, manager, it) }
        }
    }

    /** Cancels the scheduled update when the last widget instance is removed */
    override fun onDisabled(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
            buildWidgetUpdateIntent(context, AnalogDigitalClockWidgetProvider::class.java, 3)
        )
    }
}

/**
 * Builds and pushes the 2x2 digital clock widget UI.
 * Shows hours and minutes stacked, with a decorative border that adapts to the theme.
 * Internal so ThemeWatcherService can call it directly.
 */
internal fun updateAnalogDigitalClockWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views    = RemoteViews(context.packageName, R.layout.analog_digital_clock_widget_layout)
    val isDark   = context.isDarkMode()
    val calendar = Calendar.getInstance()
    val hours    = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
    val minutes  = String.format("%02d", calendar.get(Calendar.MINUTE))
    val typeface = context.font(R.font.nstyle_04)


    val W         = 1200
    val H         = 1200
    val bitmap    = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
    val canvas    = Canvas(bitmap)
    val textColor = context.themeColor()

    // Transparent background — shaped by widget_background.xml
    canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface  = typeface
        this.color     = textColor
        this.textSize  = context.dp(120f)
        this.textAlign = Paint.Align.CENTER
    }

    val lineHeight = paint.descent() - paint.ascent()
    val totalH     = lineHeight * 2
    val startY     = (H - totalH) / 2f - paint.ascent()

    // Draw hours on the top half, minutes on the bottom half
    canvas.drawText(hours,   W / 2f, startY, paint)
    canvas.drawText(minutes, W / 2f, startY + lineHeight, paint)


    // Draw the stacked clock bitmap (shared function from WidgetExtensions)
    views.setImageViewBitmap(
        R.id.analog_digital_clock_widget_canvas,
        bitmap
    )

    // Tint the decorative border to match the current theme (shared function from WidgetExtensions)
    views.applyBorderTint(R.id.analog_digital_clock_widget_border, isDark)

    // Apply the themed rounded background
    views.setInt(R.id.analog_digital_clock_widget_root, "setBackgroundResource", R.drawable.widget_2x2_background)

    // Tap opens the clock app
    views.setOnClickPendingIntent(
        R.id.analog_digital_clock_widget_root,
        context.openAppIntent(3, Intent(AlarmClock.ACTION_SHOW_ALARMS))
    )

    appWidgetManager.updateAppWidget(appWidgetId, views)

}