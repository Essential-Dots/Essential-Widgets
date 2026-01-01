package com.essentialwidgets.org.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.glance.layout.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.essentialwidgets.org.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NdotDigitalWidget : GlanceAppWidget() {

    companion object {
        val timeKey = longPreferencesKey("ndot_digital_clock_time")
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 60.dp),
            DpSize(120.dp, 120.dp),
            DpSize(120.dp, 180.dp),
            DpSize(60.dp, 120.dp),
            DpSize(60.dp, 60.dp),
            DpSize(60.dp, 180.dp),
        )
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            GlanceTheme(colors = WidgetTheme.colors) {
                DigitalClockContent()
            }
        }
    }

    @OptIn(ExperimentalGraphicsApi::class)
    @Composable
    fun DigitalClockContent() {
        val context = LocalContext.current
        val size = LocalSize.current

        val textSizeSp = 60f

        val textColor = WidgetTheme.colors.onSurface.getColor(context).toArgb()
        val time = when {
            size.width <= size.height  -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    .replace(':', '\n')
            }
            else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }

        val bitmap = createTextBitmap(context, time, textSizeSp, textColor)


        Box(
            modifier =  GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize()
                    .padding(8.dp)
            )
        }

    }


    fun createTextBitmap(
        context: Context,
        text: String,
        textSizeSp: Float,
        textColor: Int
    ): Bitmap {
        val metrics = context.resources.displayMetrics
        val scaledDensity = metrics.density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textSizeSp * scaledDensity
            color = textColor

            typeface = ResourcesCompat.getFont(context, R.font.ndot_f)
                ?: Typeface.MONOSPACE

            textAlign = Paint.Align.CENTER

            isSubpixelText = true
            isLinearText = true
            isFilterBitmap = true
        }

        val textLines = text.split('\n')

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)

        val totalLineHeight = paint.fontSpacing * textLines.size
        val maxWidth = textLines.maxOfOrNull { line -> paint.measureText(line) } ?: 0f

        val bitmap = Bitmap.createBitmap(
            maxWidth.toInt().coerceAtLeast(1),
            totalLineHeight.toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        var yPosition = paint.fontSpacing / 2 + textBounds.height() / 2 // Start position for the first line

        // Draws each line one by one
        for (line in textLines) {
            canvas.drawText(
                line,
                canvas.width / 2f, // Draws at the horizontal center of the canvas
                yPosition,
                paint
            )
            // Move yPosition down for the next line
            yPosition += paint.fontSpacing
        }

        return bitmap
    }


}
