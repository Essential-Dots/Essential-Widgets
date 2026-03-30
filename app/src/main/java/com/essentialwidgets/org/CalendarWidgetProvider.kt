package com.essentialwidgets.org

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.util.TypedValue
import android.widget.RemoteViews
import java.util.Calendar
import android.graphics.Color


class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateCalendarWidget(context, appWidgetManager, it) }
    }

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
            ids.forEach { updateCalendarWidget(context, manager, it) }
        }
    }
}

// ── fuori dalla classe ──

internal fun updateCalendarWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int
) {
    val isDark = (context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    val textColor = if (isDark) Color.WHITE else Color.BLACK

    val bitmap = drawCalendar(context, textColor)

    val views = RemoteViews(context.packageName, R.layout.calendar_widget)
    views.setImageViewBitmap(R.id.widget_canvas, bitmap)
    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_background)

    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_CALENDAR)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pi = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, pi)

    appWidgetManager.updateAppWidget(widgetId, views)
}

private fun drawCalendar(
    context: Context,
    textColor: Int,
): Bitmap {
    val W = 800
    val H = 800
    val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val ntypeTf = context.resources.getFont(R.font.ntype82_regular)

    val interTf = context.resources.getFont(R.font.inter)

    val calendar = Calendar.getInstance()
    val today      = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName  = android.text.format.DateFormat.format("MMM", calendar).toString()
    val dayName    = android.text.format.DateFormat.format("EEEE", calendar).toString()

    // Header piccolo: "Tuesday, Oct"
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ntypeTf
        color = textColor
        textSize = spCalendar(context, 42f)
    }
    canvas.drawText("$dayName, $monthName", 14f, 120f, headerPaint)

    // Griglia
    val firstDay = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDow    = (firstDay.get(Calendar.DAY_OF_WEEK) - 2).let { if (it < 0) 6 else it }
    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)

    val colW       = W / 7f
    val rowH       = 105f
    val gridStartY = 240f
    val gridStartX = 10f

    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = interTf
        textSize = spCalendar(context, 20f)
        typeface = Typeface.create(interTf, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
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
            canvas.drawCircle(cx, cy - 24f, 48f, dotPaint)
            numPaint.color = Color.WHITE
        }else{
            numPaint.color = textColor
            numPaint.typeface = interTf
        }

        canvas.drawText(day.toString(), cx, cy, numPaint)

        col++
        if (col > 6) { col = 0; row++ }
    }

    return bitmap
}

private fun spCalendar(context: Context, value: Float): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics
    )