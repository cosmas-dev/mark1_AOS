package com.cosmasbio.mark1.data.device

import android.net.Network
import android.util.Log
import com.cosmasbio.mark1.model.ConnectionStage
import com.cosmasbio.mark1.model.DeviceInfo
import com.cosmasbio.mark1.model.DeviceStatus
import java.io.BufferedInputStream
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.json.JSONObject

class Mark1DeviceManager(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    companion object {
        private const val TAG = "Mark1DeviceManager"
        private const val DEVICE_IP = "192.168.0.1"
        private const val DEVICE_PORT = 9191
        private const val CONNECT_TIMEOUT_MS = 4000
        private const val READ_TIMEOUT_MS = 4000
        private const val IMAGE_READ_TIMEOUT_MS = 15000
        private const val MAX_FRAME_SIZE = 50L * 1024L * 1024L
    }

    private val _status = MutableStateFlow(DeviceStatus())
    val status: StateFlow<DeviceStatus> = _status.asStateFlow()

    private var socket: Socket? = null
    @Volatile
    private var network: Network? = null
    fun setNetwork(network: Network) {
        this.network = network
    }

    fun clearNetwork() {
        this.network = null
    }
    private var input: BufferedInputStream? = null

    private fun update(transform: (DeviceStatus) -> DeviceStatus) {
        _status.value = transform(_status.value)
    }

    suspend fun connect(): Boolean =
        withContext(ioDispatcher) {
            val currentSocket = socket
            if (currentSocket != null && currentSocket.isConnected && !currentSocket.isClosed) {
                Log.d(TAG, "connect(): existing socket reused")
                update {
                    it.copy(
                        stage = ConnectionStage.SocketConnected,
                        wifiConnected = true,
                        socketConnected = true,
                        lastMessage = "리더기 연결 완료",
                        lastError = null
                    )
                }
                return@withContext true
            }

            Log.d(TAG, "network = $network")
            Log.d(TAG, "ip = $DEVICE_IP")
            Log.d(TAG, "port = $DEVICE_PORT")
            try {
                Log.d(TAG, "connect() start: $DEVICE_IP:$DEVICE_PORT")
                closeInternal()
                delay(100)

                update {
                    it.copy(
                        stage = ConnectionStage.ConnectingWifi,
                        lastMessage = "리더기 연결 확인 중",
                        lastError = null
                    )
                }

                val currentNetwork = network
                    ?: throw IllegalStateException("COSMAS Wi-Fi Network가 설정되지 않았습니다.")

                val newSocket = currentNetwork.socketFactory.createSocket()

                Log.d(TAG, "socket created")

                newSocket.connect(
                    InetSocketAddress(DEVICE_IP, DEVICE_PORT),
                    CONNECT_TIMEOUT_MS
                )

                Log.d(TAG, "socket connected")

                newSocket.soTimeout = READ_TIMEOUT_MS

                socket = newSocket
                input = BufferedInputStream(newSocket.getInputStream())

                Log.d(TAG, "connect() success")

                update {
                    it.copy(
                        stage = ConnectionStage.SocketConnected,
                        wifiConnected = true,
                        socketConnected = true,
                        lastMessage = "리더기 연결 완료",
                        lastError = null
                    )
                }

                true
            } catch (e: ConnectException) {
                Log.e(TAG, "connect() refused", e)
                closeInternal()

                update {
                    it.copy(
                        stage = ConnectionStage.WifiConnected,
                        wifiConnected = true,
                        socketConnected = false,
                        lastMessage = "Wi-Fi 연결됨 / 제어 연결 실패",
                        lastError = e.message
                    )
                }

                false
            } catch (e: Exception) {
                Log.e(TAG, "connect() failed", e)
                closeInternal()

                update {
                    it.copy(
                        stage = ConnectionStage.Failed,
                        wifiConnected = false,
                        socketConnected = false,
                        lastMessage = "장비 연결 실패",
                        lastError = e.message
                    )
                }

                false
            }
        }

    suspend fun disconnect() =
        withContext(ioDispatcher) {
            closeInternal()
            update {
                it.copy(
                    stage = ConnectionStage.Idle,
                    wifiConnected = false,
                    socketConnected = false,
                    lastMessage = "연결 해제",
                    lastError = null
                )
            }
        }

    suspend fun ensureConnected(): Boolean {
        val currentSocket = socket
        if (currentSocket != null && currentSocket.isConnected && !currentSocket.isClosed) {
            return true
        }
        return connect()
    }

    /**
     * 화면 재진입 시 기존 COSMAS Network/Socket만 조용히 복원한다.
     * 저장된 Network가 없으면 Wi-Fi 요청창을 띄우지 않고 false를 반환한다.
     */
    suspend fun restoreKnownConnection(): Boolean =
        withContext(ioDispatcher) {
            val currentSocket = socket
            if (
                currentSocket != null &&
                currentSocket.isConnected &&
                !currentSocket.isClosed &&
                !currentSocket.isInputShutdown &&
                !currentSocket.isOutputShutdown
            ) {
                update {
                    it.copy(
                        stage = ConnectionStage.SocketConnected,
                        wifiConnected = true,
                        socketConnected = true,
                        lastMessage = "리더기 연결 완료",
                        lastError = null
                    )
                }
                return@withContext true
            }

            if (network == null) return@withContext false
            connect()
        }

    suspend fun loadDeviceInfo(): DeviceInfo? =
        withContext(ioDispatcher) {
            if (!ensureConnected()) return@withContext null

            val response =
                sendCommandExpectResponse(groupCode = "A001", cmd = "DEV_INFO", data = "1")
                    ?: return@withContext null

            // DEV_INFO의 DATA는 문자열이 아니라 객체로 온다: {"device","name","light","kit","mode","calib","status"}
            val data = response.optJSONObject("body")?.optJSONObject("DATA")
                ?: return@withContext null

            val info = DeviceInfo(
                device = data.optString("device"),
                name = data.optString("name"),
                light = data.optString("light"),
                kit = data.optString("kit"),
                mode = data.optString("mode"),
                calib = data.optString("calib"),
                status = data.optString("status"),
            )

            update {
                it.copy(
                    modelName = info.name.ifBlank { info.device }.ifBlank { it.modelName },
                    lastMessage = "장비 정보 조회 완료",
                )
            }

            info
        }

    suspend fun toggleBacklight(): Boolean =
        withContext(ioDispatcher) {
            if (!ensureConnected()) return@withContext false

            val nextOn = !_status.value.lightOn
            val dataString = if (nextOn) "1" else "0" // "1" 또는 "0" 문자열

            try {
                sendCommandWithoutResponse(
                    groupCode = "B001",
                    cmd = "BL_CTRL",
                    data = dataString
                )

                update {
                    it.copy(
                        lightOn = nextOn,
                        lastMessage = if (nextOn) "백라이트 켜짐" else "백라이트 꺼짐",
                        lastError = null
                    )
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "백라이트 명령 중 에러: ${e.message}")
                update { it.copy(lastMessage = "백라이트 제어 실패", lastError = e.message) }
                false
            }
        }

    suspend fun readTemperature(): String? =
        withContext(ioDispatcher) {
            if (!ensureConnected()) return@withContext null

            return@withContext try {
                val response =
                    sendCommandExpectResponse(
                        groupCode = "E001",
                        cmd = "TEMP_READ",
                        data = "1"
                    )
                        ?: return@withContext null

                val temperature = extractBodyData(response)

                update {
                    it.copy(
                        temperature = temperature,
                        lastMessage = "온도 조회 완료",
                        lastError = null
                    )
                }

                temperature
            } catch (e: Exception) {
                update { it.copy(lastMessage = "온도 조회 실패", lastError = e.message) }
                null
            }
        }

    suspend fun sendSetting(groupCode: String, settingKey: String, value: String): Boolean =
        withContext(ioDispatcher) {
            if (!ensureConnected()) return@withContext false

            return@withContext try {
                val payloadData =
                    JSONObject().put("KEY", settingKey).put("VALUE", value).toString()

                sendCommandExpectResponse(
                    groupCode = groupCode,
                    cmd = "SET_PARM",
                    data = payloadData
                )

                update { it.copy(lastMessage = "$settingKey 설정 완료", lastError = null) }
                true
            } catch (e: Exception) {
                update { it.copy(lastMessage = "$settingKey 설정 실패", lastError = e.message) }
                false
            }
        }

    suspend fun captureImage(): Boolean =
        withContext(ioDispatcher) {
            if (!ensureConnected()) return@withContext false

            return@withContext try {
                sendCommandWithoutResponse(groupCode = "B001", cmd = "CAM_CTRL", data = "0")

                update { it.copy(lastMessage = "촬영 명령 전송 완료", lastError = null) }
                true
            } catch (e: Exception) {
                update { it.copy(lastMessage = "촬영 명령 전송 실패", lastError = e.message) }
                false
            }
        }

    /**
     * Flutter 구현과 동일하게 하나의 장비 소켓에서 촬영 명령과 이미지 수신을 처리한다.
     */
    suspend fun captureImageBytes(): ByteArray =
        withContext(ioDispatcher) {
            if (!ensureConnected()) {
                throw IOException("리더기 소켓에 연결할 수 없습니다.")
            }

            val currentSocket = socket ?: throw IOException("리더기 소켓이 없습니다.")
            val previousTimeout = currentSocket.soTimeout

            try {
                currentSocket.soTimeout = IMAGE_READ_TIMEOUT_MS
                sendCommandWithoutResponse("B001", "BL_CTRL", 1)
                delay(100)
                sendCommandWithoutResponse("B001", "CAM_CTRL", 1)
                delay(2000)
                sendCommandWithoutResponse("B001", "IMG_TRANS", 1)
                Log.d(TAG, "IMG_TRANS sent; waiting for 8-byte frame header")

                var imageBytes: ByteArray? = null
                while (imageBytes == null) {
                    val frame = readFrame()
                    val normalizedFrame = normalizeImageFrame(frame)
                    if (isJpeg(normalizedFrame) || isPng(normalizedFrame)) {
                        Log.d(
                            TAG,
                            "image frame received: raw=${frame.size}, normalized=${normalizedFrame.size} bytes"
                        )
                        update { it.copy(lastMessage = "이미지 수신 완료", lastError = null) }
                        imageBytes = normalizedFrame
                        continue
                    }

                    // 장비가 이미지 전에 JSON 상태 프레임을 보내는 경우 건너뛴다.
                    val text = frame.toString(Charsets.UTF_8)
                    runCatching { JSONObject(text) }
                        .onSuccess { Log.d(TAG, "image transfer status: $text") }
                        .onFailure { Log.w(TAG, "unknown frame ignored: ${frame.size} bytes") }
                }
                imageBytes
            } catch (e: SocketTimeoutException) {
                val error = IOException("IMG_TRANS 전송 후 이미지 응답 시간 초과", e)
                Log.e(TAG, "captureImageBytes(): timed out waiting for frame", e)
                update { it.copy(lastMessage = "이미지 응답 시간 초과", lastError = error.message) }
                closeInternal()
                throw error
            } catch (e: Exception) {
                Log.e(TAG, "captureImageBytes() failed", e)
                update { it.copy(lastMessage = "이미지 수신 실패", lastError = e.message) }
                throw e
            } finally {
                if (!currentSocket.isClosed) {
                    currentSocket.soTimeout = previousTimeout
                    runCatching { sendCommandWithoutResponse("B001", "BL_CTRL", 0) }
                }
            }
        }

    /**
     * COSMAS IMG_TRANS 본문은 펌웨어에 따라 이미지 앞에 uint32/uint64 길이를
     * 한 번 더 포함한다. 실제 PNG/JPEG 시그니처가 시작되는 위치로 정규화한다.
     */
    private fun normalizeImageFrame(frame: ByteArray): ByteArray =
        when {
            isJpeg(frame) || isPng(frame) -> frame
            frame.size > 4 &&
                (isJpegAt(frame, 4) || isPngAt(frame, 4)) -> {
                Log.d(TAG, "normalize image: stripped nested uint32 length prefix")
                frame.copyOfRange(4, frame.size)
            }
            frame.size > 8 &&
                (isJpegAt(frame, 8) || isPngAt(frame, 8)) -> {
                Log.d(TAG, "normalize image: stripped nested uint64 length prefix")
                frame.copyOfRange(8, frame.size)
            }
            else -> frame
        }

    private fun isJpegAt(data: ByteArray, offset: Int): Boolean =
        data.size >= offset + 2 &&
            data[offset] == 0xFF.toByte() && data[offset + 1] == 0xD8.toByte()

    private fun isPngAt(data: ByteArray, offset: Int): Boolean =
        data.size >= offset + 8 &&
            data[offset] == 0x89.toByte() && data[offset + 1] == 0x50.toByte() &&
            data[offset + 2] == 0x4E.toByte() && data[offset + 3] == 0x47.toByte() &&
            data[offset + 4] == 0x0D.toByte() && data[offset + 5] == 0x0A.toByte() &&
            data[offset + 6] == 0x1A.toByte() && data[offset + 7] == 0x0A.toByte()

    private suspend fun sendCommandWithoutResponse(groupCode: String, cmd: String, data: Any) =
        withContext(ioDispatcher) {
            // 소켓이 없거나 닫혀있으면 새로 연결 시도
            if (socket == null || socket?.isClosed == true || socket?.isConnected == false) {
                connect()
            }

            val currentSocket = socket ?: throw IllegalStateException("소켓 연결 불가")

            val json =
                JSONObject()
                    .put("header", groupCode)
                    .put("body", JSONObject().put("CMD", cmd).put("DATA", data))

            // 명령어 끝에 \n을 붙여서 전송 (기기가 명령의 끝을 알 수 있게 함)
            val request = json.toString() + "\n"

            Log.d(TAG, "전송 데이터: $request")

            try {
                val outputStream = currentSocket.getOutputStream()
                outputStream.write(request.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } catch (e: Exception) {
                Log.e(TAG, "데이터 전송 실패", e)
                closeInternal() // 에러 발생 시 소켓 초기화
                throw e
            }
        }

    private suspend fun sendCommandExpectResponse(
        groupCode: String,
        cmd: String,
        data: String
    ): JSONObject? =
        withContext(ioDispatcher) {
            val currentSocket = socket ?: throw IllegalStateException("socket unavailable")

            val json =
                JSONObject()
                    .put("header", groupCode)
                    .put("body", JSONObject().put("CMD", cmd).put("DATA", data))

            val request = json.toString() + "\n"
            Log.d(TAG, "sendCommandExpectResponse(): $request")
            currentSocket.getOutputStream().write(request.toByteArray(Charsets.UTF_8))
            currentSocket.getOutputStream().flush()

            try {
                val response = readPacket()
                Log.d(TAG, "sendCommandExpectResponse(): response=$response")
                response
            } catch (e: SocketTimeoutException) {
                throw IOException("장비 응답 시간 초과", e)
            }
        }

    private fun readPacket(): JSONObject? {
        val bodyBytes = readFrame()

        val bodyText = String(bodyBytes, Charsets.UTF_8)
        Log.d(TAG, "Read Body: $bodyText")
        return JSONObject(bodyText)
    }

    private fun readFrame(): ByteArray {
        val currentInput = input ?: throw IllegalStateException("input unavailable")
        val headerBytes = ByteArray(8)
        readFully(currentInput, headerBytes)

        val firstLength = java.nio.ByteBuffer.wrap(headerBytes, 0, 4)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .int
            .toLong() and 0xFFFF_FFFFL
        val secondValue = java.nio.ByteBuffer.wrap(headerBytes, 4, 4)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .int
            .toLong() and 0xFFFF_FFFFL
        val legacyLength = java.nio.ByteBuffer.wrap(headerBytes)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .long

        val headerHex = headerBytes.joinToString(" ") {
            "%02X".format(it.toInt() and 0xFF)
        }
        Log.d(
            TAG,
            "recv header: hex=[$headerHex], firstLength=$firstLength, " +
                "secondValue=$secondValue, uint64=$legacyLength"
        )

        /*
         * 실제 COSMAS IMG_TRANS 응답:
         * [uint32 = 4][uint32 = image byte length][image bytes]
         * 예: 04 00 00 00 B3 80 01 00 -> image length 98,483 bytes
         */
        if (firstLength == 4L && secondValue in 1..MAX_FRAME_SIZE) {
            Log.d(TAG, "recv format=image-size-frame, imageLength=$secondValue")
            val nestedLengthBytes = ByteArray(4)
            readFully(currentInput, nestedLengthBytes)
            val nestedLength = java.nio.ByteBuffer.wrap(nestedLengthBytes)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                .int
                .toLong() and 0xFFFF_FFFFL

            if (nestedLength == secondValue) {
                // [4][N][N][PNG/JPEG N bytes]
                return ByteArray(secondValue.toInt()).also { image ->
                    readFully(currentInput, image)
                    Log.d(
                        TAG,
                        "recv nested image body: ${image.size} bytes, " +
                            "first=${image.take(8).joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }}"
                    )
                }
            }

            // 중첩 길이가 없는 변형: 방금 읽은 4바이트가 이미지 본문의 시작이다.
            val image = ByteArray(secondValue.toInt())
            nestedLengthBytes.copyInto(image, endIndex = minOf(4, image.size))
            if (image.size > 4) readFully(currentInput, image, 4, image.size - 4)
            Log.d(TAG, "recv non-nested image body: ${image.size} bytes")
            return image
        }

        // 이전 Flutter 방식: uint64 little-endian 길이 + body
        if (legacyLength in 1..MAX_FRAME_SIZE && legacyLength <= Int.MAX_VALUE) {
            Log.d(TAG, "recv format=uint64-frame, bodyLength=$legacyLength")
            return ByteArray(legacyLength.toInt()).also { readFully(currentInput, it) }
        }

        // 일부 펌웨어: uint32 길이 + body. body의 앞 4바이트는 이미 headerBytes에 포함됨.
        if (firstLength in 4..MAX_FRAME_SIZE && firstLength <= Int.MAX_VALUE) {
            val body = ByteArray(firstLength.toInt())
            val copied = minOf(4, body.size)
            headerBytes.copyInto(body, 0, 4, 4 + copied)
            if (body.size > copied) {
                readFully(currentInput, body, copied, body.size - copied)
            }
            Log.d(TAG, "recv format=uint32-frame, bodyLength=$firstLength")
            return body
        }

        throw IOException(
            "알 수 없는 장비 응답: hex=[$headerHex], " +
                "firstLength=$firstLength, secondValue=$secondValue"
        )
    }

    private fun isJpeg(data: ByteArray): Boolean =
        data.size >= 4 &&
                data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() &&
                data[data.size - 2] == 0xFF.toByte() && data[data.size - 1] == 0xD9.toByte()

    private fun isPng(data: ByteArray): Boolean =
        data.size >= 8 &&
                data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
                data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() &&
                data[4] == 0x0D.toByte() && data[5] == 0x0A.toByte() &&
                data[6] == 0x1A.toByte() && data[7] == 0x0A.toByte()

    private fun readFully(input: BufferedInputStream, buffer: ByteArray) {
        readFully(input, buffer, 0, buffer.size)
    }

    private fun readFully(
        input: BufferedInputStream,
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ) {
        var currentOffset = offset
        val endOffset = offset + length
        while (currentOffset < endOffset) {
            val read = input.read(buffer, currentOffset, endOffset - currentOffset)
            if (read == -1) {
                throw IOException("소켓이 종료되어 데이터를 끝까지 읽지 못함")
            }
            currentOffset += read
        }
    }

    private fun extractBodyData(response: JSONObject): String {
        return response.optJSONObject("body")?.opt("DATA")?.toString() ?: ""
    }

    private fun closeInternal() {
        try {
            input?.close()
        } catch (_: Exception) {} finally {
            input = null
        }

        try {
            socket?.close()
        } catch (_: Exception) {} finally {
            socket = null
        }
    }
}
