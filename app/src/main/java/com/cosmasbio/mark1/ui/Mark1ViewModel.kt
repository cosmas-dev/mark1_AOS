//package com.cosmasbio.mark1.ui
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.cosmasbio.mark1.data.device.Mark1DeviceManager
//import com.cosmasbio.mark1.iot.GistFlutterCaptureClient
//import com.cosmasbio.mark1.model.AppUiState
//import com.cosmasbio.mark1.model.CaptureResult
//import com.cosmasbio.mark1.model.CaptureUiState
//import com.cosmasbio.mark1.model.TestDraft
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//
//class Mark1ViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val deviceManager = Mark1DeviceManager()
//    private val captureClient = GistFlutterCaptureClient(application.applicationContext)
//
//    private val loading = MutableStateFlow(true)
//    private val draft = MutableStateFlow(TestDraft())
//    private val captureState = MutableStateFlow(CaptureUiState())
//    private val _events = MutableSharedFlow<String>()
//    private val _captureResults = MutableSharedFlow<CaptureResult>()
//
//    val events = _events.asSharedFlow()
//    val captureResults = _captureResults.asSharedFlow()
//
//    val uiState: StateFlow<AppUiState> =
//        combine(loading, deviceManager.status, draft, captureState) { loadingValue, statusValue, draftValue, captureValue ->
//            AppUiState(
//                loading = loadingValue,
//                deviceStatus = statusValue,
//                draft = draftValue,
//                capture = captureValue,
//            )
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = AppUiState()
//        )
//
//    init {
//        viewModelScope.launch {
//            try {
//                val isConnected = deviceManager.connect()
//                if (isConnected) {
//                    delay(500)
//                    deviceManager.loadDeviceInfo()
//                }
//            } catch (e: Exception) {
//                _events.emit("초기 연결 실패: ${e.message}")
//            } finally {
//                loading.value = false
//            }
//        }
//    }
//
//    fun reconnectDevice() {
//        viewModelScope.launch {
//            try {
//                deviceManager.connect()
//                deviceManager.loadDeviceInfo()
//                _events.emit("리더기를 다시 연결했어요.")
//            } catch (e: Exception) {
//                _events.emit(e.message ?: "재연결 실패")
//            }
//        }
//    }
//
//    fun toggleBacklight() {
//        viewModelScope.launch {
//            try {
//                val ok = deviceManager.toggleBacklight()
//                if (!ok) _events.emit("백라이트 제어 실패")
//            } catch (e: Exception) {
//                _events.emit(e.message ?: "백라이트 제어 실패")
//            }
//        }
//    }
//
//    fun captureImage() {
//        viewModelScope.launch {
//            try {
//                val ok = deviceManager.captureImage()
//                _events.emit(if (ok) "촬영 명령을 전송했어요." else "촬영 실패")
//            } catch (e: Exception) {
//                _events.emit(e.message ?: "촬영 실패")
//            }
//        }
//    }
//
//    fun readTemperature() {
//        viewModelScope.launch {
//            try {
//                val temp = deviceManager.readTemperature()
//                _events.emit(if (!temp.isNullOrBlank()) "현재 온도 ${temp}℃" else "온도 값을 받지 못했어요.")
//            } catch (e: Exception) {
//                _events.emit(e.message ?: "온도 확인 실패")
//            }
//        }
//    }
//
//    fun applySetting(groupCode: String, key: String, value: String) {
//        viewModelScope.launch {
//            try {
//                val ok = deviceManager.sendSetting(groupCode, key, value)
//                _events.emit(if (ok) "$key 설정을 보냈어요." else "설정 전송 실패")
//            } catch (e: Exception) {
//                _events.emit(e.message ?: "설정 전송 실패")
//            }
//        }
//    }
//
//    fun updateDraft(
//        name: String? = null,
//        type: String? = null,
//        info: String? = null,
//        delaySeconds: String? = null,
//    ) {
//        val current = draft.value
//        draft.value = current.copy(
//            name = name ?: current.name,
//            type = type ?: current.type,
//            info = info ?: current.info,
//            delaySeconds = delaySeconds ?: current.delaySeconds,
//        )
//    }
//
//    fun startFlutterStyleCapture() {
//        if (captureState.value.capturing) return
//
//        viewModelScope.launch {
//            try {
//                val currentDraft = draft.value
//                val delaySec = currentDraft.delaySeconds.toIntOrNull() ?: 0
//
//                captureState.value = CaptureUiState(
//                    capturing = true,
//                    remainingSeconds = delaySec,
//                    error = null,
//                )
//
//                var remaining = delaySec
//                while (remaining > 0) {
//                    delay(1000)
//                    remaining -= 1
//                    captureState.value = captureState.value.copy(remainingSeconds = remaining)
//                }
//
//                val result = captureClient.captureAndSave(
//                    name = currentDraft.name.ifBlank { "sample" },
//                    type = currentDraft.type,
//                    info = currentDraft.info,
//                )
//
//                captureState.value = CaptureUiState()
//                _captureResults.emit(result)
//            } catch (e: Exception) {
//                captureState.value = CaptureUiState(
//                    capturing = false,
//                    remainingSeconds = 0,
//                    error = e.message ?: "촬영 실패",
//                )
//                _events.emit(e.message ?: "촬영 실패")
//            }
//        }
//    }
//
//    fun clearCaptureError() {
//        captureState.value = captureState.value.copy(error = null)
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        viewModelScope.launch { deviceManager.disconnect() }
//    }
//}
package com.cosmasbio.mark1.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cosmasbio.mark1.analyzer.IvdAnalyzerRepository
import com.cosmasbio.mark1.data.device.Mark1DeviceManager
import com.cosmasbio.mark1.iot.GistFlutterCaptureClient
import com.cosmasbio.mark1.model.AppUiState
import com.cosmasbio.mark1.model.CaptureResult
import com.cosmasbio.mark1.model.CaptureUiState
import com.cosmasbio.mark1.model.TestDraft
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Mark1ViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = Mark1DeviceManager()
    private val captureClient = GistFlutterCaptureClient(application.applicationContext)
    private val analyzerRepository = IvdAnalyzerRepository()

    private val loading = MutableStateFlow(true)
    private val draft = MutableStateFlow(TestDraft())
    private val captureState = MutableStateFlow(CaptureUiState())
    private val latestCaptureResult = MutableStateFlow<CaptureResult?>(null)
    private val _events = MutableSharedFlow<String>()
    private val _captureResults = MutableSharedFlow<Unit>()

    val events = _events.asSharedFlow()
    val captureResults = _captureResults.asSharedFlow()

    val uiState: StateFlow<AppUiState> =
        combine(loading, deviceManager.status, draft, captureState, latestCaptureResult) {
                loadingValue,
                statusValue,
                draftValue,
                captureValue,
                latestResultValue ->
            AppUiState(
                loading = loadingValue,
                deviceStatus = statusValue,
                draft = draftValue,
                capture = captureValue,
                latestCaptureResult = latestResultValue,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState()
        )

    init {
        viewModelScope.launch {
            try {
                val isConnected = deviceManager.connect()
                if (isConnected) {
                    delay(500)
                    deviceManager.loadDeviceInfo()
                }
            } catch (e: Exception) {
                _events.emit("초기 연결 실패: ${e.message}")
            } finally {
                loading.value = false
            }
        }
    }

    fun reconnectDevice() {
        viewModelScope.launch {
            try {
                deviceManager.connect()
                deviceManager.loadDeviceInfo()
                _events.emit("리더기를 다시 연결했어요.")
            } catch (e: Exception) {
                _events.emit(e.message ?: "재연결 실패")
            }
        }
    }

    fun toggleBacklight() {
        viewModelScope.launch {
            try {
                val ok = deviceManager.toggleBacklight()
                if (!ok) _events.emit("백라이트 제어 실패")
            } catch (e: Exception) {
                _events.emit(e.message ?: "백라이트 제어 실패")
            }
        }
    }

    fun captureImage() {
        viewModelScope.launch {
            try {
                val ok = deviceManager.captureImage()
                _events.emit(if (ok) "촬영 명령을 전송했어요." else "촬영 실패")
            } catch (e: Exception) {
                _events.emit(e.message ?: "촬영 실패")
            }
        }
    }

    fun readTemperature() {
        viewModelScope.launch {
            try {
                val temp = deviceManager.readTemperature()
                _events.emit(if (!temp.isNullOrBlank()) "현재 온도 ${temp}℃" else "온도 값을 받지 못했어요.")
            } catch (e: Exception) {
                _events.emit(e.message ?: "온도 확인 실패")
            }
        }
    }

    fun applySetting(groupCode: String, key: String, value: String) {
        viewModelScope.launch {
            try {
                val ok = deviceManager.sendSetting(groupCode, key, value)
                _events.emit(if (ok) "$key 설정을 보냈어요." else "설정 전송 실패")
            } catch (e: Exception) {
                _events.emit(e.message ?: "설정 전송 실패")
            }
        }
    }

    fun updateDraft(
        name: String? = null,
        type: String? = null,
        info: String? = null,
        delaySeconds: String? = null,
    ) {
        val current = draft.value
        draft.value = current.copy(
            name = name ?: current.name,
            type = type ?: current.type,
            info = info ?: current.info,
            delaySeconds = delaySeconds ?: current.delaySeconds,
        )
    }

    fun startFlutterStyleCapture() {
        if (captureState.value.capturing || captureState.value.analyzing) return

        viewModelScope.launch {
            try {
                val currentDraft = draft.value
                val delaySec = currentDraft.delaySeconds.toIntOrNull() ?: 0

                captureState.value = CaptureUiState(
                    capturing = true,
                    analyzing = false,
                    remainingSeconds = delaySec,
                    error = null,
                )

                var remaining = delaySec
                while (remaining > 0) {
                    delay(1000)
                    remaining -= 1
                    captureState.value = captureState.value.copy(remainingSeconds = remaining)
                }

                val rawCapture = captureClient.captureAndSave(
                    name = currentDraft.name.ifBlank { "sample" },
                    type = currentDraft.type,
                    info = currentDraft.info,
                )

                captureState.value = captureState.value.copy(
                    capturing = false,
                    analyzing = true,
                    remainingSeconds = 0,
                    error = null,
                )

                val analysisOutputDir = File(
                    getApplication<Application>().filesDir,
                    "analysis"
                ).absolutePath

                val analysis = analyzerRepository.analyzeStrip(
                    imagePath = rawCapture.imagePath,
                    outputDir = analysisOutputDir,
                    manualRoi = null,
                )
                Log.d("ANALYSIS", "start analyze path=${rawCapture.imagePath}")
                val f = File(rawCapture.imagePath)
                Log.d("ANALYSIS", "exists=${f.exists()} size=${f.length()}")

                latestCaptureResult.value = rawCapture.copy(analysis = analysis)

                captureState.value = CaptureUiState()
                _captureResults.emit(Unit)
            } catch (e: Exception) {
                captureState.value = CaptureUiState(
                    capturing = false,
                    analyzing = false,
                    remainingSeconds = 0,
                    error = e.message ?: "촬영/분석 실패",
                )
                _events.emit(e.message ?: "촬영/분석 실패")
            }
        }
    }

    fun clearCaptureError() {
        captureState.value = captureState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { deviceManager.disconnect() }
    }
}