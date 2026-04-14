package com.essentialwidgets.org

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.RemoteViews
import java.util.Calendar

class CalendarWidgetProvider : AppWidgetProvider() {

    // Called when widget is updated by the system
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach {
            updateCalendarWidget(context, appWidgetManager, it)
        }
    }

    // Handles system time/date changes to refresh widget
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val manager = AppWidgetManager.getInstance(context)

            val ids = manager.getAppWidgetIds(
                ComponentName(context, CalendarWidgetProvider::class.java)
            )

            ids.forEach {
                updateCalendarWidget(context, manager, it)
            }
        }
    }

    // Called when widget size changes (resize support)
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateCalendarWidget(context, appWidgetManager, appWidgetId)
    }
}

/**
 * Builds and pushes the calendar widget UI.
 * Draws the current month grid with today highlighted.
 * Internal so ThemeWatcherService can call it directly.
 */
internal fun updateCalendarWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.calendar_widget)

    // Get widget size in dp
    val options = appWidgetManager.getAppWidgetOptions(widgetId)

    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

    // Convert dp to pixels
    val density = context.resources.displayMetrics.density
    val widthPx = (minWidth * density).toInt()
    val heightPx = (minHeight * density).toInt()

    // Render calendar bitmap based on real widget size
    val bitmap = drawCalendar(context, widthPx, heightPx)
    views.setImageViewBitmap(R.id.widget_canvas, bitmap)

    // Set rounded background drawable
    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_2x2_background)

    // Open system calendar app on tap
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_CALENDAR)
    }

    views.setOnClickPendingIntent(
        R.id.widget_root,
        context.openAppIntent(0, intent)
    )

    appWidgetManager.updateAppWidget(widgetId, views)
}

/**
 * Draws the full calendar month onto a Bitmap.
 * Includes a header with the day and month name,
 * and a 7-column grid with today circled in red.
 */
private fun drawCalendar(context: Context, W: Int, H: Int): Bitmap {

    // Create bitmap canvas based on widget real size
    val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Clear background (transparent)
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val monthName = android.text.format.DateFormat.format("MMM", calendar).toString()
    val dayName = android.text.format.DateFormat.format("EEEE", calendar).toString()

    val textColor = context.themeColor()

    // Dynamic layout scaling
    val padding = W * 0.02f
    val headerY = H * 0.15f
    val gridStartY = H * 0.35f

    val colW = (W - padding * 2) / 7f
    val rowH = (H - gridStartY) / 6f

    val headerSize = W * 0.15f
    val textDimension = W * 0.065f

    // Header paint
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = headerSize
        textAlign = Paint.Align.LEFT
        typeface = context.font(R.font.serif_regular)
    }

    // Day numbers paint
    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = textDimension
        textAlign = Paint.Align.CENTER
        typeface = context.font(R.font.inter)
    }

    // Today highlight circle
    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D71921")
    }

    // Draw header (weekday + month)
    canvas.drawText(
        "$dayName, $monthName",
        padding,
        headerY,
        headerPaint
    )

    // Compute first day position
    val firstDay = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val startDow = (firstDay.get(Calendar.DAY_OF_WEEK) - 2).let {
        if (it < 0) 6 else it
    }

    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)

    var col = startDow
    var row = 0

    // Draw calendar grid
    for (day in 1..daysInMonth) {

        val cx = padding + col * colW + colW / 2
        val cy = gridStartY + row * rowH

        if (day == today) {
            // Highlight current day
            canvas.drawCircle(cx, cy - textDimension/2, textDimension * 0.9f, highlightPaint)
            numPaint.color = Color.WHITE
        } else {
            numPaint.color = textColor
        }

        canvas.drawText(day.toString(), cx, cy, numPaint)

        col++
        if (col > 6) {
            col = 0
            row++
        }
    }

    return bitmap
}