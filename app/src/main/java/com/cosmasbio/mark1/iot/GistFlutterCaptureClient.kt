package com.cosmasbio.mark1.iot

import android.content.Context
import android.util.Log
import com.cosmasbio.mark1.model.CaptureResult
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GistFlutterCaptureClient(
    private val context: Context,
) {
    companion object {
        private const val TAG = "GistFlutterCapture"
        private const val HOST = "192.168.0.1"
        private const val PORT = 9191
    }

    suspend fun captureAndSave(
        name: String,
        type: String,
        info: String,
    ): CaptureResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            Log.d(TAG, "connect() start: $HOST:$PORT")
            socket.connect(InetSocketAddress(HOST, PORT), 5000)
            socket.soTimeout = 15000
            Log.d(TAG, "connect() success")

            val imageDeferred = CompletableDeferred<ByteArray>()
            val readerThread = Thread {
                try {
                    readFrames(socket.getInputStream(), imageDeferred)
                } catch (t: Throwable) {
                    if (!imageDeferred.isCompleted) imageDeferred.completeExceptionally(t)
                }
            }
            readerThread.isDaemon = true
            readerThread.start()

            sendJson(socket, "B001", "BL_CTRL", 1)
            delay(100)
            sendJson(socket, "B001", "CAM_CTRL", 1)
            delay(2000)
            sendJson(socket, "B001", "IMG_TRANS", 1)

            val imageBytes = imageDeferred.await()

            try {
                sendJson(socket, "B001", "BL_CTRL", 0)
            } catch (_: Throwable) {
            }

            val file = saveImageBytes(name = name, type = type, info = info, data = imageBytes)
            CaptureResult(
                imagePath = file.absolutePath,
                name = name,
                type = type,
                info = info,
            )
        } finally {
            try {
                socket?.close()
            } catch (_: Throwable) {
            }
        }
    }

    private fun sendJson(socket: Socket, header: String, cmd: String, data: Any) {
        val payload = JSONObject()
            .put("header", header)
            .put("body", JSONObject().put("CMD", cmd).put("DATA", data))
            .toString() + "\n"
        Log.d(TAG, "send: $payload")
        socket.getOutputStream().write(payload.toByteArray(Charsets.UTF_8))
        socket.getOutputStream().flush()
    }

    private fun readFrames(input: InputStream, imageDeferred: CompletableDeferred<ByteArray>) {
        var buffer = ByteArray(0)
        val chunk = ByteArray(8192)

        while (!imageDeferred.isCompleted) {
            val read = input.read(chunk)
            if (read == -1) {
                throw IllegalStateException("socket closed")
            }
            buffer += chunk.copyOf(read)

            while (true) {
                if (buffer.size < 8) break

                val frameSize = littleEndianUInt32(buffer, 0)
                if (frameSize <= 0) {
                    Log.e(TAG, "invalid frameSize=$frameSize")
                    buffer = ByteArray(0)
                    break
                }
                if (buffer.size < 8 + frameSize) break

                val frameData = buffer.copyOfRange(8, 8 + frameSize)
                buffer = buffer.copyOfRange(8 + frameSize, buffer.size)

                try {
                    val text = frameData.toString(Charsets.UTF_8)
                    JSONObject(text)
                    Log.d(TAG, "recv json: $text")
                    continue
                } catch (_: Throwable) {
                }

                val imageBytes = normalizeImageBytes(frameData)
                if (isJpeg(imageBytes) || isPng(imageBytes)) {
                    Log.d(TAG, "image frame accepted len=${imageBytes.size}")
                    if (!imageDeferred.isCompleted) imageDeferred.complete(imageBytes)
                    return
                } else {
                    Log.d(TAG, "unknown binary frame len=${frameData.size}")
                }
            }
        }
    }

    private fun normalizeImageBytes(frameData: ByteArray): ByteArray {
        return when {
            isJpeg(frameData) || isPng(frameData) -> frameData
            frameData.size > 12 && (isJpeg(frameData.copyOfRange(4, frameData.size)) || isPng(frameData.copyOfRange(4, frameData.size))) ->
                frameData.copyOfRange(4, frameData.size)
            frameData.size > 16 && (isJpeg(frameData.copyOfRange(8, frameData.size)) || isPng(frameData.copyOfRange(8, frameData.size))) ->
                frameData.copyOfRange(8, frameData.size)
            else -> frameData
        }
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

    private fun littleEndianUInt32(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
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
