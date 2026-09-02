package com.cosmasbio.mark1.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Network
import com.cosmasbio.mark1.analyzer.IvdAnalyzerRepository
import com.cosmasbio.mark1.data.device.CosmasWifiConnector
import com.cosmasbio.mark1.data.device.Mark1DeviceManager
import com.cosmasbio.mark1.data.local.Mark1Database
import com.cosmasbio.mark1.data.repository.AuthRepository
import com.cosmasbio.mark1.data.repository.ExamRepository
import com.cosmasbio.mark1.data.repository.LoginResult
import com.cosmasbio.mark1.data.sync.SyncWorker
import com.cosmasbio.mark1.iot.GistFlutterCaptureClient
import com.cosmasbio.mark1.model.AppUiState
import com.cosmasbio.mark1.model.CaptureResult
import com.cosmasbio.mark1.model.CaptureUiState
import com.cosmasbio.mark1.model.LoginUiState
import com.cosmasbio.mark1.model.PersonInfo
import com.cosmasbio.mark1.model.TestDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Mark1ViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = Mark1DeviceManager()
    private val captureClient = GistFlutterCaptureClient(
        application.applicationContext,
        deviceManager,
    )
    private val analyzerRepository = IvdAnalyzerRepository()
    private val database = Mark1Database.getInstance(application.applicationContext)
    private val examRepository = ExamRepository(database)
    private val authRepository = AuthRepository(database)

    private val loading = MutableStateFlow(true)
    private val draft = MutableStateFlow(TestDraft())
    private val captureState = MutableStateFlow(CaptureUiState())
    private val latestCaptureResult = MutableStateFlow<CaptureResult?>(null)
    private val _events = MutableSharedFlow<String>()
    private val _captureResults = MutableSharedFlow<Unit>()

    private val wifiConnector = CosmasWifiConnector(application.applicationContext)

    private var isCosmasConnectionProcessing = false
    private var captureJob: Job? = null

    private val _loginState = MutableStateFlow(LoginUiState())

    val events = _events.asSharedFlow()
    val captureResults = _captureResults.asSharedFlow()
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

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
                // Wi-Fi 연결 전 필요한 초기화만 수행
                runCatching { authRepository.seedTestAccountIfMissing() }
                    .onFailure { Log.w("Mark1ViewModel", "테스트 계정 생성 실패", it) }
            } finally {
                loading.value = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (_loginState.value.submitting) return

        _loginState.value = LoginUiState(submitting = true)
        viewModelScope.launch {
            val result = runCatching { authRepository.login(email, password) }
                .getOrElse { error ->
                    Log.e("Mark1ViewModel", "로그인 처리 실패", error)
                    _loginState.value = LoginUiState(error = "로그인 중 문제가 발생했습니다.")
                    return@launch
                }

            when (result) {
                is LoginResult.Success -> {
                    _loginState.value = LoginUiState()
                    onSuccess()
                }

                LoginResult.InvalidCredentials -> {
                    _loginState.value =
                        LoginUiState(error = "이메일 또는 비밀번호가 올바르지 않습니다.")
                }
            }
        }
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = null)
    }

    fun connectCosmasWifi(
        onConnected: () -> Unit,
    ) {
        if (isCosmasConnectionProcessing) return

        isCosmasConnectionProcessing = true

        wifiConnector.connect(
            ssid = "COSMAS",
            password = null,

            onConnected = { network ->
                deviceManager.setNetwork(network)

                 viewModelScope.launch {
                     try {
                         val socketConnected = deviceManager.connect()

                         if (!socketConnected) {
                             _events.emit(
                                 "COSMAS Wi-Fi에는 연결됐지만 리더기 소켓 연결에 실패했습니다."
                             )
                             return@launch
                         }

                         _events.emit("COSMAS 리더기 연결 완료")
                         onConnected()

                         // 장비 모델 조회는 연결 성공과 분리
                         launch {
                             runCatching {
 //                                deviceManager.loadDeviceInfo()
                             }.onFailure { error ->
                                 Log.w(
                                     "Mark1ViewModel",
                                     "DEV_INFO 응답을 받지 못했습니다.",
                                     error
                                 )
                             }
                         }
                     } catch (e: Exception) {
                         Log.e("Mark1ViewModel", "리더기 연결 실패", e)
                         _events.emit(e.message ?: "리더기 연결에 실패했습니다.")
                     } finally {
                         isCosmasConnectionProcessing = false
                     }
                 }
            },

            onUnavailable = {
                isCosmasConnectionProcessing = false

                viewModelScope.launch {
                    _events.emit("COSMAS Wi-Fi를 찾을 수 없습니다.")
                }
            },

            onLost = {
                isCosmasConnectionProcessing = false

                viewModelScope.launch {
                    // 먼저 열린 소켓을 닫고 Network 참조를 제거
                    deviceManager.disconnect()
                    deviceManager.clearNetwork()

                    _events.emit("COSMAS Wi-Fi 연결이 끊어졌습니다.")
                }
            },
        )
    }

    fun restoreKnownDeviceConnection() {
        if (deviceManager.status.value.socketConnected || isCosmasConnectionProcessing) return

        // 앱 세션 내 연결 기록이 없어도, 시스템이 이미 COSMAS Wi-Fi에 붙어 있다면
        // 사용자가 Connect를 다시 누르지 않도록 바로 연결을 시도한다.
        if (wifiConnector.isConnectedToSsid("COSMAS")) {
            connectCosmasWifi(onConnected = {})
            return
        }

        viewModelScope.launch {
            runCatching { deviceManager.restoreKnownConnection() }
                .onFailure { error ->
                    Log.w("Mark1ViewModel", "기존 리더기 연결 복원 실패", error)
                }
        }
    }

    fun reconnectDevice() {
        viewModelScope.launch {
            // try {
            //     val connected = deviceManager.connect()

            //     if (connected) {
            //         deviceManager.loadDeviceInfo()
            //         _events.emit("리더기를 다시 연결했어요.")
            //     } else {
            //         _events.emit("리더기 연결에 실패했습니다.")
            //     }
            // } catch (e: Exception) {
            //     _events.emit(
            //         e.message ?: "재연결 실패"
            //     )
            // }
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

    /** AddDiagnosisDetailsScreen 에서 입력한 대상자 정보 전체를 보관한다. */
    fun updatePersonInfo(person: PersonInfo) {
        draft.value = draft.value.copy(name = person.name, person = person)
    }

    fun startFlutterStyleCapture() {
        if (captureState.value.capturing || captureState.value.analyzing) return

        captureJob = viewModelScope.launch {
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

                val finalResult = rawCapture.copy(analysis = analysis)
                latestCaptureResult.value = finalResult

                // 네트워크 성공 여부와 무관하게 검사 데이터를 Room 에 먼저 저장한다.
                val personInfo = currentDraft.person.takeIf { !it.isEmpty }
                    ?: PersonInfo(name = currentDraft.name)
                val captureId = examRepository.saveExam(
                    person = personInfo,
                    capture = finalResult,
                    analysis = analysis,
                    deviceId = deviceManager.status.value.modelName,
                )
                examRepository.markUploadPending(captureId)
                SyncWorker.enqueue(getApplication())
                Log.d("Mark1ViewModel", "exam saved locally captureId=$captureId")

                captureState.value = CaptureUiState()
                _captureResults.emit(Unit)
            } catch (e: CancellationException) {
                // 사용자가 화면을 벗어나 취소한 경우이므로 오류로 표시하지 않는다.
                throw e
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

    /** 분석 화면을 벗어날 때 진행 중인 촬영/분석을 정리한다. */
    fun cancelCapture() {
        captureJob?.cancel()
        captureJob = null
        captureState.value = CaptureUiState()
    }

    fun clearCaptureError() {
        captureState.value = captureState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { deviceManager.disconnect() }
    }
}
