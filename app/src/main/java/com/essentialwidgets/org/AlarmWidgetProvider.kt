package com.essentialwidgets.org

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.provider.AlarmClock
import android.text.TextPaint
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAlarmWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, AlarmWidgetProvider::class.java)
            )
            for (id in allIds) {
                updateAlarmWidget(context, appWidgetManager, id)
            }
        }
    }
}

internal fun updateAlarmWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.alarm_widget_layout)

    // Detect light/dark mode for icon and text color
    val isDarkMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    val iconColor = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    // Get next alarm from system
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val nextAlarm: AlarmManager.AlarmClockInfo? = alarmManager.nextAlarmClock

    val clockToDisplay: String
    val amTextToDisplay: String
    val isAlarmSet: Boolean

    if (nextAlarm != null) {
        val alarmTime = nextAlarm.triggerTime
        clockToDisplay = SimpleDateFormat("h:mm", Locale.getDefault()).format(alarmTime)
        amTextToDisplay = SimpleDateFormat("a", Locale.getDefault()).format(alarmTime)
        views.setImageViewResource(R.id.alarm_icon, R.drawable.ic_alarm_on)
        views.setInt(R.id.alarm_icon, "setColorFilter", iconColor)
        isAlarmSet = true
    } else {
        clockToDisplay = "No Alarm"
        amTextToDisplay = ""
        views.setImageViewResource(R.id.alarm_icon, R.drawable.ic_alarm_off)
        views.setInt(R.id.alarm_icon, "setColorFilter", iconColor)
        isAlarmSet = false
    }

    val typeface = context.resources.getFont(R.font.serif_headline)

    // Bitmap for the clock (es. "7:30")
    val clockBitmap = createTextBitmap(context, clockToDisplay, typeface, 24f)
    views.setImageViewBitmap(R.id.alarm_status_text_as_image, clockBitmap)

    // Bitmap for AM/PM — only if the alarm was set
    if (isAlarmSet && amTextToDisplay.isNotEmpty()) {
        val amBitmap = createTextBitmap(context, amTextToDisplay, context.resources.getFont(R.font.inter), 12f)
        views.setImageViewBitmap(R.id.alarm_am_text, amBitmap)
        views.setViewVisibility(R.id.alarm_am_text, View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.alarm_am_text, View.GONE)
    }

    // Show or hide red dot based on alarm state
    views.setViewVisibility(R.id.red_circle_icon, if (isAlarmSet) View.VISIBLE else View.GONE)

    // Open Clock app to set a new alarm on tap
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, appWidgetId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.alarm_widget_root, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun createTextBitmap(
    context: Context,
    text: String,
    typeface: Typeface,
    textSizeSp: Float
): Bitmap {
    val textSizePx = textSizeSp * context.resources.displayMetrics.scaledDensity

    // Read color based on light/dark mode
    val isDarkMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    val textColor = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.color = textColor
    }

    val textWidth = paint.measureText(text)
    val top = paint.fontMetrics.top
    val bottom = paint.fontMetrics.bottom
    val textHeight = bottom - top

    val bitmap = Bitmap.createBitmap(
        maxOf(textWidth.toInt(), 1),
        maxOf(textHeight.toInt(), 1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawText(text, 0f, -top, paint)

    return bitmap
}