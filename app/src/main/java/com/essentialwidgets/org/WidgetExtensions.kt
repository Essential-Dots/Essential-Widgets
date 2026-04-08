package com.essentialwidgets.org

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import android.widget.RemoteViews
import android.app.AlarmManager
import android.content.ComponentName
import android.os.Build

/** Returns true if the system is currently in dark mode */
fun Context.isDarkMode(): Boolean =
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/** Returns white in dark mode, black in light mode */
fun Context.themeColor(): Int =
    if (isDarkMode()) android.graphics.Color.WHITE else android.graphics.Color.BLACK

/** Loads a font from res/font/, falls back to system default if not found */
fun Context.font(@FontRes resId: Int): Typeface =
    ResourcesCompat.getFont(this, resId) ?: Typeface.DEFAULT

/** Converts a dp value to pixels using the current display density */
fun Context.dp(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

/** Creates a PendingIntent that opens an app via the given Intent */
fun Context.openAppIntent(requestCode: Int, intent: Intent): PendingIntent =
    PendingIntent.getActivity(
        this, requestCode,
        intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

/**
 * Draws a repeating diagonal slash pattern over a solid background.
 * Used as the shared background for all widgets that need it.
 */
fun drawPatternBackground(width: Int, height: Int, isDark: Boolean): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Solid base color
    canvas.drawColor(if (isDark) Color.BLACK else Color.WHITE)

    // Subtle diagonal lines
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = if (isDark) Color.parseColor("#2A2A2A") else Color.parseColor("#E0E0E0")
        strokeWidth = 2f
        style       = Paint.Style.STROKE
    }

    val spacing = 28f
    var x = -height.toFloat()
    while (x < width + height) {
        canvas.drawLine(x, 0f, x + height, height.toFloat(), paint)
        x += spacing
    }

    return bitmap
}

/**
 * Draws the decorative SVG border onto the canvas scaled to fit.
 * Uses VectorDrawable so no path parsing needed.
 */

fun drawDecorativeBorder(canvas: Canvas, width: Int, height: Int, context: Context, isDark: Boolean) {
    val drawable = androidx.core.content.ContextCompat.getDrawable(
        context, R.drawable.ic_square_clock_border
    ) as android.graphics.drawable.VectorDrawable

    // Tint to match current theme
    androidx.core.graphics.drawable.DrawableCompat.setTint(
        drawable,
        if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    )

    // Scale to fill the canvas
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
}

/**
 * Applies the correct tint to the border ImageView based on the current theme.
 * Call this on any widget that has a border ImageView to tint.
 */
fun RemoteViews.applyBorderTint(viewId: Int, isDark: Boolean) {
    setInt(
        viewId, "setColorFilter",
        if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    )
}


/**
 * Schedules an exact alarm at the top of the next minute.
 * Falls back to setWindow if exact alarm permission is not granted (Android 12+).
 */
fun scheduleNextMinuteUpdate(context: Context, pendingIntent: PendingIntent) {
    val now          = System.currentTimeMillis()
    val nextMinute   = ((now / 60000) + 1) * 60000
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // setWindow is sufficient for a clock widget — no need for exact alarms
    alarmManager.setWindow(AlarmManager.RTC, nextMinute, 200 , pendingIntent)
}

/**
 * Builds the standard update PendingIntent for a widget provider class.
 * Each provider needs a unique requestCode to avoid PendingIntent collisions.
 */
fun buildWidgetUpdateIntent(context: Context, providerClass: Class<*>, requestCode: Int): PendingIntent {
    val intent = Intent(context, providerClass).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_IDS,
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, providerClass))
        )
    }
    return PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}