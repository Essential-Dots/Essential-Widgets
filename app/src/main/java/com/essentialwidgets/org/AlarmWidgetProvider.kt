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
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmWidgetProvider : AppWidgetProvider() {

    /** Called by the system when the widget needs to be updated */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAlarmWidget(context, appWidgetManager, it) }
    }

    /** Listens for alarm clock changes to keep the widget in sync */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, AlarmWidgetProvider::class.java))
                .forEach { updateAlarmWidget(context, manager, it) }
        }
    }
}

/**
 * Builds and pushes the alarm widget UI.
 * Shows the next alarm time with AM/PM and a red dot indicator,
 * or "No Alarm" if no alarm is set.
 * Internal so ThemeWatcherService can call it directly.
 */
internal fun updateAlarmWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views    = RemoteViews(context.packageName, R.layout.alarm_widget_layout)
    val iconColor = context.themeColor()

    // Fetch the next scheduled alarm from the system
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val nextAlarm    = alarmManager.nextAlarmClock

    val clockText: String
    val amPmText: String
    val isAlarmSet: Boolean

    if (nextAlarm != null) {
        // Format the alarm time into separate time and AM/PM strings
        val time   = nextAlarm.triggerTime
        clockText  = SimpleDateFormat("h:mm", Locale.getDefault()).format(time)
        amPmText   = SimpleDateFormat("a", Locale.getDefault()).format(time)
        isAlarmSet = true
        views.setImageViewResource(R.id.alarm_icon, R.drawable.ic_alarm_on)
    } else {
        // No alarm set — show placeholder text and off icon
        clockText  = "No Alarm"
        amPmText   = ""
        isAlarmSet = false
        views.setImageViewResource(R.id.alarm_icon, R.drawable.ic_alarm_off)
    }

    // Tint the alarm icon to match the current theme
    views.setInt(R.id.alarm_icon, "setColorFilter", iconColor)

    val serifTf = context.font(R.font.serif_headline)
    val interTf = context.font(R.font.inter)

    // Render the clock time as a bitmap with the serif font
    views.setImageViewBitmap(
        R.id.alarm_status_text_as_image,
        createTextBitmap(context, clockText, serifTf, 24f)
    )

    // Render AM/PM only when an alarm is active
    if (isAlarmSet && amPmText.isNotEmpty()) {
        views.setImageViewBitmap(
            R.id.alarm_am_text,
            createTextBitmap(context, amPmText, interTf, 12f)
        )
        views.setViewVisibility(R.id.alarm_am_text, View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.alarm_am_text, View.GONE)
    }

    // Show the red dot indicator only when an alarm is active
    views.setViewVisibility(R.id.red_circle_icon, if (isAlarmSet) View.VISIBLE else View.GONE)

    // Tap opens the clock app to set or edit an alarm
    views.setOnClickPendingIntent(
        R.id.alarm_widget_root,
        context.openAppIntent(appWidgetId, Intent(AlarmClock.ACTION_SET_ALARM))
    )

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

/**
 * Renders a string onto a Bitmap using the given typeface and size.
 * Size is expressed in dp to stay immune to system font scaling.
 * Text color adapts automatically to light/dark mode.
 */
internal fun createTextBitmap(context: Context, text: String, typeface: Typeface, textSizeDp: Float): Bitmap {
    // Convert dp to px — using density (not scaledDensity) to ignore user font scale
    val textSizePx = textSizeDp * context.resources.displayMetrics.density

    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.color    = context.themeColor()
    }

    // Use font metrics to size the bitmap precisely around the text
    val top    = paint.fontMetrics.top
    val bottom = paint.fontMetrics.bottom

    val bitmap = Bitmap.createBitmap(
        maxOf(paint.measureText(text).toInt(), 1),
        maxOf((bottom - top).toInt(), 1),
        Bitmap.Config.ARGB_8888
    )

    // Draw the text baseline-aligned within the bitmap
    Canvas(bitmap).drawText(text, 0f, -top, paint)
    return bitmap
}