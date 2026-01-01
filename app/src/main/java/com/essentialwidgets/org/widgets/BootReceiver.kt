// In app/src/main/java/com/essentialwidgets/org/widgets/BootReceiver.kt
package com.essentialwidgets.org.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // After reboot, restart the clock update service
            context.startForegroundService(Intent(context, ClockUpdateService::class.java))
        }
    }
}
