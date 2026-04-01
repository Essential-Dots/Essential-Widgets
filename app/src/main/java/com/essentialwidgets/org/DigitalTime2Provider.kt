package com.essentialwidgets.org

import android.app.AlarmManager
import android.app.PendingIntent
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
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.text.TextPaint

// Ogni occorrenza di ClockWidgetProvider → DigitalTime2Provider
class DigitalTime2Provider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, DigitalTime2Provider::class.java))
                .forEach { updateDigitalTime2Widget(context, manager, it) }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        context.startForegroundService(Intent(context, ThemeWatcherService::class.java))
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, DigitalTime2Provider::class.java))
            .forEach { updateDigitalTime2Widget(context, appWidgetManager, it) }
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        val pi = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, DigitalTime2Provider::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
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
/** Schedules an exact-ish broadcast at the top of the next minute to refresh the clock */
fun scheduleNextUpdate(context: Context) {
    val intent = Intent(context, DigitalTime2Provider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_IDS,
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, DigitalTime2Provider::class.java))
        )
    }
    val pi = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val now = System.currentTimeMillis()
    (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        .setWindow(AlarmManager.RTC, now + (60000 - now % 60000), 60000, pi)
}

/** Builds and pushes the clock widget UI. Internal so ThemeWatcherService can call it directly. */
internal fun updateDigitalTime2Widget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views    = RemoteViews(context.packageName, R.layout.digital_time2_widget_layout)
    val typeface = context.font(R.font.ntype82_regular)
    val time     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    views.setImageViewBitmap(R.id.widget_text_clock, createClockBitmap(context, time, typeface, 32f))
    views.setOnClickPendingIntent(
        R.id.widget_root,
        context.openAppIntent(appWidgetId, Intent(AlarmClock.ACTION_SHOW_ALARMS))
    )
    appWidgetManager.updateAppWidget(appWidgetId, views)
}