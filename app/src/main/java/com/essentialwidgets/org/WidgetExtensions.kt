package com.essentialwidgets.org

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.util.TypedValue
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

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