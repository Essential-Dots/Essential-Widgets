package com.essentialwidgets.org

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.provider.AlarmClock
import android.text.TextPaint
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DigitalTime2Provider : AppWidgetProvider() {

    /** Re-renders the widget when the system theme changes */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, DigitalTime2Provider::class.java))
                .forEach { updateDigitalClock2Widget(context, manager, it) }
        }
    }

    /** Called by the system when the widget needs to be updated */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        context.startForegroundService(Intent(context, ThemeWatcherService::class.java))
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, DigitalTime2Provider::class.java))
            .forEach { updateDigitalClock2Widget(context, appWidgetManager, it) }

        // Use shared helpers from WidgetExtensions — no dedicated schedule function needed
        scheduleNextMinuteUpdate(
            context,
            buildWidgetUpdateIntent(context, DigitalTime2Provider::class.java, 0)
        )
    }

    /** Cancels the scheduled update when the last widget instance is removed */
    override fun onDisabled(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
            buildWidgetUpdateIntent(context, DigitalTime2Provider::class.java, 0)
        )
    }
}

/**
 * Renders the clock time onto a Bitmap.
 * Draws the time string on the left, with stacked AM/PM labels on the right.
 * The active period (AM or PM) is highlighted in red, the other in gray.
 * Size is in dp to stay immune to system font scaling.
 */
private fun createClockBitmap(context: Context, text: String, typeface: Typeface, textSizeDp: Float): Bitmap {
    // Convert dp to px — using density (not scaledDensity) to ignore user font scale
    val textSizePx = textSizeDp * context.resources.displayMetrics.density
    val amPmSizePx = textSizePx * 0.3f
    val isAm       = Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM
    val textColor  = context.themeColor()
    val interTf    = context.font(R.font.inter)

    // Main time text paint
    val timePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.color    = textColor
    }

    // AM label — red when active, gray otherwise
    val amPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = interTf
        this.textSize = amPmSizePx
        this.color    = if (isAm) android.graphics.Color.RED else android.graphics.Color.GRAY
    }

    // PM label — red when active, gray otherwise
    val pmPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = interTf
        this.textSize = amPmSizePx
        this.color    = if (!isAm) android.graphics.Color.RED else android.graphics.Color.GRAY
    }

    // Measure all elements to compute the final bitmap size
    val timeTop    = timePaint.fontMetrics.top
    val timeBottom = timePaint.fontMetrics.bottom
    val timeHeight = timeBottom - timeTop
    val timeWidth  = timePaint.measureText(text)
    val amPmWidth  = maxOf(amPaint.measureText("AM"), pmPaint.measureText("PM"))
    val amPmHeight = amPaint.fontMetrics.bottom - amPaint.fontMetrics.top
    val spacing    = textSizePx * 0.2f

    val bitmap = Bitmap.createBitmap(
        maxOf((timeWidth + spacing + amPmWidth).toInt(), 1),
        maxOf(timeHeight.toInt(), 1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)

    // Draw the time on the left
    canvas.drawText(text, 0f, -timeTop, timePaint)

    // Draw AM on top-right and PM on bottom-right
    val amPmX = timeWidth + spacing
    canvas.drawText("AM", amPmX, amPmHeight + textSizePx * 0.01f, amPaint)
    canvas.drawText("PM", amPmX, timeHeight - amPmHeight / 2 - textSizePx * 0.01f, pmPaint)

    return bitmap
}

/** Builds and pushes the clock widget UI. Internal so ThemeWatcherService can call it directly. */
internal fun updateDigitalClock2Widget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views    = RemoteViews(context.packageName, R.layout.digital_clock2_widget_layout)
    val typeface = context.font(R.font.serif_regular)
    val time     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    views.setImageViewBitmap(R.id.widget_text_clock, createClockBitmap(context, time, typeface, 32f))

    // Apply the themed rounded background
    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_2x1_background)

    // Tap opens the clock app
    views.setOnClickPendingIntent(
        R.id.widget_root,
        context.openAppIntent(appWidgetId, Intent(AlarmClock.ACTION_SHOW_ALARMS))
    )

    appWidgetManager.updateAppWidget(appWidgetId, views)

}