package com.essentialwidgets.org

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

object TorchController {

    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    private fun getCameraManager(context: Context): CameraManager {
        if (cameraManager == null) {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }
        return cameraManager!!
    }

    private fun getBackCameraId(context: Context): String? {
        if (cameraId != null) return cameraId
        val cm = getCameraManager(context)
        for (id in cm.cameraIdList) {
            val chars = cm.getCameraCharacteristics(id)
            if (chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                cameraId = id
                return id
            }
        }
        return null
    }

    // -------------------------------------------------------
    // Torcia standard con luminosità
    // Android 13+ supporta setTorchStrengthLevel
    // -------------------------------------------------------
    fun setTorch(context: Context, on: Boolean, brightness: Int) {
        try {
            val cm = getCameraManager(context)
            val id = getBackCameraId(context) ?: return

            if (!on) {
                cm.setTorchMode(id, false)
                return
            }

            // Android 13+ (API 33): native brightness
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                val chars = cm.getCameraCharacteristics(id)
                val maxLevel = chars.get(
                    CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL
                ) ?: 1

                // brightness 0..7 -> 1..maxLevel
                val level = (1 + (brightness.toFloat() / 7f) * (maxLevel - 1)).toInt()
                    .coerceIn(1, maxLevel)

                cm.turnOnTorchWithStrengthLevel(id, level)
            } else {
                // Sotto API 33: solo on/off
                cm.setTorchMode(id, true)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}