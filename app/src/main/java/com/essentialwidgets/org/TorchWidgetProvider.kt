package com.essentialwidgets.org

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TorchWidgetProvider : AppWidgetProvider() {

    companion object {

        const val ACTION_TORCH_CLICK = "com.essentialwidgets.org.TORCH_CLICK"
        const val ACTION_TORCH_DOUBLE_CLICK  = "com.essentialwidgets.org.TORCH_DOUBLE_CLICK"
        const val ACTION_BRIGHT_UP   = "com.essentialwidgets.org.BRIGHT_UP"
        const val ACTION_BRIGHT_DOWN = "com.essentialwidgets.org.BRIGHT_DOWN"

        const val PREF_NAME          = "torch_widget_prefs"
        const val PREF_TORCH_ON      = "torch_on"
        const val PREF_BRIGHTNESS    = "brightness"

        const val BRIGHTNESS_MIN     = 0
        const val BRIGHTNESS_MAX     = 7
        const val BRIGHTNESS_DEFAULT = 7

        // ClipDrawable usa livelli 0..10000
        // 0 = barra vuota, 10000 = barra piena
        private fun brightnessToLevel(brightness: Int): Int {
            return ((brightness.toFloat() / BRIGHTNESS_MAX) * 10000).toInt()
        }

        fun updateTorchWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs      = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val torchOn    = prefs.getBoolean(PREF_TORCH_ON, false)
            val isOn       = torchOn
            val brightness = prefs.getInt(PREF_BRIGHTNESS, BRIGHTNESS_DEFAULT)

            val views = RemoteViews(context.packageName, R.layout.torch_widget)

            // --- Red background ---
            val fillLevel = if (isOn) brightnessToLevel(brightness) else 0
            views.setInt(R.id.torch_fill, "setImageLevel", fillLevel)


            // --- Torch icon ---
            views.setImageViewResource(
                R.id.torch_btn_toggle,
                if (isOn) R.drawable.ic_flashlight_on
                else      R.drawable.ic_flashlight_off
            )

            // --- Icon color (gray if off) ---
            val iconColor = if (isOn)
                android.graphics.Color.WHITE
            else
                if (context.isDarkMode())
                    android.graphics.Color.WHITE
                else
                    android.graphics.Color.BLACK

            views.setInt(R.id.torch_btn_toggle, "setColorFilter", iconColor)
            views.setInt(R.id.torch_btn_up,     "setColorFilter", iconColor)
            views.setInt(R.id.torch_btn_down,   "setColorFilter", iconColor)

            // --- Click torch ---
            views.setOnClickPendingIntent(
                R.id.torch_btn_toggle,
                pendingIntent(context, ACTION_TORCH_CLICK, appWidgetId, 0)
            )

            // --- Freccia su ---
            views.setOnClickPendingIntent(
                R.id.torch_btn_up,
                pendingIntent(context, ACTION_BRIGHT_UP, appWidgetId, 2)
            )

            // --- Freccia giù ---
            views.setOnClickPendingIntent(
                R.id.torch_btn_down,
                pendingIntent(context, ACTION_BRIGHT_DOWN, appWidgetId, 3)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun pendingIntent(
            context: Context,
            action: String,
            appWidgetId: Int,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, TorchWidgetProvider::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            return PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateTorchWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        when (intent.action) {

            ACTION_TORCH_CLICK -> {
                val newState = !prefs.getBoolean(PREF_TORCH_ON, false)
                editor.putBoolean(PREF_TORCH_ON, newState).apply()

                val views = RemoteViews(context.packageName, R.layout.torch_widget)
                // --- Torch icon ---
                views.setImageViewResource(
                    R.id.torch_btn_toggle,
                    if (newState) R.drawable.ic_flashlight_on
                    else      R.drawable.ic_flashlight_off
                )
                TorchController.setTorch(
                    context, newState,
                    prefs.getInt(PREF_BRIGHTNESS, BRIGHTNESS_DEFAULT)
                )
            }

            ACTION_BRIGHT_UP -> {
                val brightness    = prefs.getInt(PREF_BRIGHTNESS, BRIGHTNESS_DEFAULT)
                val newBrightness = (brightness + 1).coerceAtMost(BRIGHTNESS_MAX)
                editor.putInt(PREF_BRIGHTNESS, newBrightness).apply()
                if (prefs.getBoolean(PREF_TORCH_ON, false))
                    TorchController.setTorch(context, true, newBrightness)
            }

            ACTION_BRIGHT_DOWN -> {
                val brightness    = prefs.getInt(PREF_BRIGHTNESS, BRIGHTNESS_DEFAULT)
                val newBrightness = (brightness - 1).coerceAtLeast(BRIGHTNESS_MIN)
                editor.putInt(PREF_BRIGHTNESS, newBrightness).apply()
                if (prefs.getBoolean(PREF_TORCH_ON, false))
                    TorchController.setTorch(context, true, newBrightness)
            }
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateTorchWidget(context, appWidgetManager, appWidgetId)
    }
}