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
import android.text.TextPaint
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ClockWidgetProvider::class.java)
            )
            for (appWidgetId in allIds) {
                updateClockWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val serviceIntent = Intent(context, ThemeWatcherService::class.java)
        context.startForegroundService(serviceIntent)
        val allIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
        for (appWidgetId in allIds) {
            updateClockWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        val intent = Intent(context, ClockWidgetProvider::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}

fun scheduleNextUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, ClockWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Schedule update at the next exact minute
    val now = System.currentTimeMillis()
    val nextMinute = now + (60000 - now % 60000)

    alarmManager.setWindow(AlarmManager.RTC, nextMinute, 60000, pendingIntent)
}

internal fun updateClockWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.digital_widget_layout)

    val typeface = context.resources.getFont(R.font.ntype82_regular)
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    val isDarkMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

    val textColor = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    val textBitmap = createClockBitmap(context, currentTime, typeface, 32f, textColor)
    views.setImageViewBitmap(R.id.widget_text_clock, textBitmap)

    // Open the clock app on widget tap
    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, appWidgetId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun createClockBitmap(
    context: Context,
    text: String,
    typeface: Typeface,
    textSizeSp: Float,
    textColor: Int = android.graphics.Color.WHITE
): Bitmap {
    val textSizePx = textSizeSp * context.resources.displayMetrics.scaledDensity
    val amPmSizePx = textSizePx * 0.3f

    // Determine AM/PM colors: red if current period, gray otherwise
    val isAm = Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM
    val amColor = if (isAm) android.graphics.Color.RED else android.graphics.Color.GRAY
    val pmColor = if (!isAm) android.graphics.Color.RED else android.graphics.Color.GRAY



    val timePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx

        this.color = textColor
    }

    val amPmTypeface = context.resources.getFont(R.font.inter)

    val amPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = amPmTypeface
        this.textSize = amPmSizePx
    }

    val pmPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = amPmTypeface
        this.textSize = amPmSizePx
    }

    // Measure time text dimensions
    val timeWidth = timePaint.measureText(text)
    val timeTop = timePaint.fontMetrics.top
    val timeBottom = timePaint.fontMetrics.bottom
    val timeHeight = timeBottom - timeTop

    // Measure AM/PM dimensions
    val amPmWidth = maxOf(amPaint.measureText("AM"), pmPaint.measureText("PM"))
    val amPmHeight = amPaint.fontMetrics.bottom - amPaint.fontMetrics.top

    // Calculate total bitmap size
    val spacing = textSizePx * 0.2f
    val totalWidth = timeWidth + spacing + amPmWidth
    val totalHeight = timeHeight

    val bitmap = Bitmap.createBitmap(
        maxOf(totalWidth.toInt(), 1),
        maxOf(totalHeight.toInt(), 1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)

    // Draw the time
    canvas.drawText(text, 0f, -timeTop, timePaint)

    // Draw AM on top, PM on bottom
    val amPmX = timeWidth + spacing
    amPaint.color = amColor
    canvas.drawText("AM", amPmX, amPmHeight + textSizePx * 0.01f, amPaint)
    pmPaint.color = pmColor
    canvas.drawText("PM", amPmX, totalHeight - amPmHeight / 2 - textSizePx * 0.01f, pmPaint)

    return bitmap
}