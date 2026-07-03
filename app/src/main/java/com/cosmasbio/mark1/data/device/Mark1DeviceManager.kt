package com.cosmasbio.mark1.data.device

import android.util.Log
import com.cosmasbio.mark1.model.ConnectionStage
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
    }

    private val _status = MutableStateFlow(DeviceStatus())
    val status: StateFlow<DeviceStatus> = _status.asStateFlow()

    private var socket: Socket? = null
    private var input: BufferedInputStream? = null

    private fun update(transform: (DeviceStatus) -> DeviceStatus) {
        _status.value = transform(_status.value)
    }

    suspend fun connect(): Boolean =
            withContext(ioDispatcher) {
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

                    val newSocket = Socket()
                    newSocket.connect(InetSocketAddress(DEVICE_IP, DEVICE_PORT), CONNECT_TIMEOUT_MS)
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

    suspend fun loadDeviceInfo(): String? =
            withContext(ioDispatcher) {
                if (!ensureConnected()) return@withContext null

                val response =
                        sendCommandExpectResponse(groupCode = "A001", cmd = "DEV_INFO", data = "1")
                                ?: return@withContext null

                val modelName = extractBodyData(response).ifBlank { "COSMAS-1000" }

                update { it.copy(modelName = modelName, lastMessage = "장비 정보 조회 완료") }

                modelName
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
                    sendCommandWithoutResponse(groupCode = "B001", cmd = "CAM_CTRL", data = "1")

                    update { it.copy(lastMessage = "촬영 명령 전송 완료", lastError = null) }
                    true
                } catch (e: Exception) {
                    update { it.copy(lastMessage = "촬영 명령 전송 실패", lastError = e.message) }
                    false
                }
            }

    private suspend fun sendCommandWithoutResponse(groupCode: String, cmd: String, data: String) =
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
        val currentInput = input ?: throw IllegalStateException("input unavailable")

        // 1. 헤더 8바이트 읽기
        val headerBytes = ByteArray(8)
        readFully(currentInput, headerBytes)

        // 2. Kotlin에서는 ByteBuffer를 사용합니다.
        val buffer = java.nio.ByteBuffer.wrap(headerBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        val bodyLength = buffer.long.toInt() // 8바이트를 long으로 읽은 후 int로 변환

        if (bodyLength <= 0 || bodyLength > 10 * 1024 * 1024) { // 10MB 이상은 비정상으로 간주
            throw IOException("응답 길이가 올바르지 않음: $bodyLength")
        }

        // 3. 바디 읽기
        val bodyBytes = ByteArray(bodyLength)
        readFully(currentInput, bodyBytes)

        val bodyText = String(bodyBytes, Charsets.UTF_8)
        Log.d(TAG, "Read Body: $bodyText")
        return JSONObject(bodyText)
    }

    private fun readFully(input: BufferedInputStream, buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val read = input.read(buffer, offset, buffer.size - offset)
            if (read == -1) {
                throw IOException("소켓이 종료되어 데이터를 끝까지 읽지 못함")
            }
            offset += read
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
