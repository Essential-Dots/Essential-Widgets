package com.essentialwidgets.org

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import java.util.Calendar

class CalendarWidgetProvider : AppWidgetProvider() {

    /** Called by the system when the widget needs to be updated */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateCalendarWidget(context, appWidgetManager, it) }
    }

    /** Listens for date/time changes to keep the calendar in sync */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
                .forEach { updateCalendarWidget(context, manager, it) }
        }
    }
}

/**
 * Builds and pushes the calendar widget UI.
 * Draws the current month grid with today highlighted.
 * Internal so ThemeWatcherService can call it directly.
 */
internal fun updateCalendarWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.calendar_widget)

    // Draw the calendar onto a bitmap and set it as the widget image
    views.setImageViewBitmap(R.id.widget_canvas, drawCalendar(context))

    // Apply the themed rounded background drawable
    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_2x2_background)

    // Tap opens the system calendar app
    val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_CALENDAR)
    }
    views.setOnClickPendingIntent(R.id.widget_root, context.openAppIntent(0, calendarIntent))

    appWidgetManager.updateAppWidget(widgetId, views)

}

/**
 * Draws the full calendar month onto a Bitmap.
 * Includes a header with the day and month name,
 * and a 7-column grid with today circled in red.
 */
private fun drawCalendar(context: Context): Bitmap {
    val W = 800; val H = 800
    val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Transparent background — the rounded shape comes from widget_background.xml
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val textColor = context.themeColor()
    val ntypeTf   = context.font(R.font.serif_regular)
    val interTf   = context.font(R.font.inter)

    val calendar  = Calendar.getInstance()
    val today     = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName = android.text.format.DateFormat.format("MMM", calendar).toString()
    val dayName   = android.text.format.DateFormat.format("EEEE", calendar).toString()

    // Draw the header — e.g. "Wednesday, Oct"
    canvas.drawText(
        "$dayName, $monthName",
        14f, 120f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ntypeTf
            color    = textColor
            textSize = context.dp(42f)
        }
    )

    // Compute grid layout for the current month
    val firstDay    = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDow    = (firstDay.get(Calendar.DAY_OF_WEEK) - 2).let { if (it < 0) 6 else it } // Mon = 0
    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)

    val colW       = W / 7f
    val rowH       = 105f
    val gridStartY = 260f
    val gridStartX = 10f

    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = interTf
        textSize  = context.dp(20f)
        textAlign = Paint.Align.CENTER
    }

    // Red circle used to highlight today
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D71921")
        style = Paint.Style.FILL
    }

    var col = startDow
    var row = 0

    for (day in 1..daysInMonth) {
        val cx = gridStartX + col * colW + colW / 2f
        val cy = gridStartY + row * rowH

        if (day == today) {
            // Draw red circle behind today's number
            canvas.drawCircle(cx, cy - 24f, 48f, dotPaint)
            numPaint.color = Color.WHITE
        } else {
            numPaint.color = textColor
        }

        canvas.drawText(day.toString(), cx, cy, numPaint)

        // Advance to the next column, wrapping to a new row after Saturday
        col++
        if (col > 6) { col = 0; row++ }
    }

    return bitmap
}