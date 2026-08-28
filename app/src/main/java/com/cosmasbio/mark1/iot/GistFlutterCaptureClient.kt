package com.cosmasbio.mark1.iot

import android.content.Context
import android.util.Log
import com.cosmasbio.mark1.data.device.Mark1DeviceManager
import com.cosmasbio.mark1.model.CaptureResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GistFlutterCaptureClient(
    private val context: Context,
    private val deviceManager: Mark1DeviceManager,
) {
    companion object {
        private const val TAG = "GistFlutterCapture"
    }

    suspend fun captureAndSave(
        name: String,
        type: String,
        info: String,
    ): CaptureResult = withContext(Dispatchers.IO) {
        val imageBytes = deviceManager.captureImageBytes()
        val file = saveImageBytes(name = name, type = type, info = info, data = imageBytes)
        CaptureResult(imagePath = file.absolutePath, name = name, type = type, info = info)
    }

    private fun saveImageBytes(
        name: String,
        type: String,
        info: String,
        data: ByteArray,
    ): File {
        val ext = if (isPng(data)) "png" else "jpg"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = listOf(safeMeta(name), safeMeta(type), safeMeta(info), timestamp)
            .filter { it.isNotBlank() }
            .joinToString("_") + ".$ext"
        val dir = File(context.filesDir, "captures").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(data)
        Log.d(TAG, "saved: ${file.absolutePath}")
        return file
    }

    private fun safeMeta(value: String): String {
        return value.trim()
            .replace(Regex("""[\\/:*?"<>|]+"""), "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    private fun isJpeg(data: ByteArray): Boolean =
        data.size > 4 &&
            data[0] == 0xFF.toByte() &&
            data[1] == 0xD8.toByte() &&
            data[data.size - 2] == 0xFF.toByte() &&
            data[data.size - 1] == 0xD9.toByte()

    private fun isPng(data: ByteArray): Boolean =
        data.size >= 8 &&
            data[0] == 0x89.toByte() &&
            data[1] == 0x50.toByte() &&
            data[2] == 0x4E.toByte() &&
            data[3] == 0x47.toByte() &&
            data[4] == 0x0D.toByte() &&
            data[5] == 0x0A.toByte() &&
            data[6] == 0x1A.toByte() &&
            data[7] == 0x0A.toByte()
}
