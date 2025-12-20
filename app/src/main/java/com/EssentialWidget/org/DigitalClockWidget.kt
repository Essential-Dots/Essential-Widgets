package com.EssentialWidget.org

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.text.TextUtils
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.*
import android.net.Uri
import android.provider.AlarmClock
import androidx.core.content.res.ResourcesCompat

class DigitalClockWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        // È FONDAMENTALE chiamare super.onReceive per gestire gli intent standard del widget
        super.onReceive(context, intent)

        // Controlla esplicitamente se è il nostro "tick" del minuto
        if (TextUtils.equals(intent.action, Intent.ACTION_TIME_TICK)) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DigitalClockWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            // Chiama onUpdate manualmente per tutti i widget attivi
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Itera su tutti i widget da aggiornare
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.dot_digital_clock_widget) // Usa il layout corretto

        // 1. Prepara il testo dell'ora
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeText = sdf.format(java.util.Date())

        // 2. Crea la bitmap con il font custom
        val textBitmap = createTextBitmap(context, timeText)

        // 3. Imposta la bitmap sull'ImageView invece che il testo sulla TextView
        views.setImageViewBitmap(R.id.timeImageView, textBitmap) // Usa il nuovo ID

        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Aggiorna il widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // Funzione helper per creare la bitmap
    private fun createTextBitmap(context: Context, text: String): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 100f // Imposta una dimensione del testo (puoi renderla dinamica)

        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // Modalità Scura: imposta il colore del testo su bianco (o un altro colore chiaro)
                paint.color = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                // Modalità Chiara: imposta il colore del testo su nero (o un altro colore scuro)
                paint.color = Color.BLACK
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                // Fallback: se la modalità non è definita, usa il nero come predefinito
                paint.color = Color.BLACK
            }
        }

        // 4. Carica il font custom dalla cartella res/font
        try {
            val typeface = ResourcesCompat.getFont(context, R.font.ndot_f)
            paint.typeface = typeface
        } catch (e: Exception) {
            // Fallback nel caso il font non venga trovato, per evitare crash
            // Potresti loggare l'errore: Log.e("WidgetFont", "Font non trovato", e)
        }

        // 5. Calcola le dimensioni del testo per creare una bitmap della giusta grandezza
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // 6. Crea la bitmap e disegna il testo su di essa
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, -bounds.left.toFloat(), -bounds.top.toFloat(), paint)

        return bitmap
    }

}
