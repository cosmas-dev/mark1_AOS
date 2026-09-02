package com.cosmasbio.mark1.iot

import android.content.Context
import android.util.Log
import com.cosmasbio.mark1.data.device.Mark1DeviceManager
import com.cosmasbio.mark1.model.CaptureResult
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GistFlutterCaptureClient(
    private val context: Context,
    private val deviceManager: Mark1DeviceManager,
) {
    companion object {
        private const val TAG = "GistFlutterCapture"
    }

    /**
     * 촬영 후 저장.
     *
     * 개인정보 보호: 파일명에 성명/검사유형/검사정보 등 개인정보를 절대 포함하지 않고
     * UUID 기반 파일명(예: 550e8400-e29b-41d4-a716-446655440000.jpg)을 사용한다.
     * name/type/info 는 반환 객체(메모리)와 Room DB 에만 유지된다.
     * 무결성 검증을 위해 촬영 직후 SHA-256 을 계산한다.
     */
    suspend fun captureAndSave(
        name: String,
        type: String,
        info: String,
    ): CaptureResult = withContext(Dispatchers.IO) {
        val imageBytes = deviceManager.captureImageBytes()

        val captureId = UUID.randomUUID().toString()
        val ext = if (isPng(imageBytes)) "png" else "jpg"
        val dir = File(context.filesDir, "captures").apply { mkdirs() }
        val file = File(dir, "$captureId.$ext")
        file.writeBytes(imageBytes)

        val sha256 = sha256Hex(imageBytes)
        Log.d(TAG, "saved: ${file.absolutePath} sha256=$sha256")

        CaptureResult(
            imagePath = file.absolutePath,
            name = name,
            type = type,
            info = info,
            captureId = captureId,
            imageSha256 = sha256,
            capturedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun sha256Hex(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(data)
            .joinToString("") { "%02x".format(it) }

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
