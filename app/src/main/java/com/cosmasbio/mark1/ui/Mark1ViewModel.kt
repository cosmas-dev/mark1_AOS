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
import com.cosmasbio.mark1.data.local.ReaderPreferences
import com.cosmasbio.mark1.data.repository.AuthRepository
import com.cosmasbio.mark1.data.repository.ExamRepository
import com.cosmasbio.mark1.data.repository.LoginResult
import com.cosmasbio.mark1.data.sync.SyncWorker
import com.cosmasbio.mark1.iot.GistFlutterCaptureClient
import com.cosmasbio.mark1.model.AppUiState
import com.cosmasbio.mark1.model.CaptureResult
import com.cosmasbio.mark1.model.CaptureUiState
import com.cosmasbio.mark1.model.ExamHistoryRow
import com.cosmasbio.mark1.model.LoginUiState
import com.cosmasbio.mark1.model.PersonEditTarget
import com.cosmasbio.mark1.model.PersonInfo
import com.cosmasbio.mark1.model.TestDraft
import com.cosmasbio.mark1.R
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
    private val _editTarget = MutableStateFlow<PersonEditTarget?>(null)

    val events = _events.asSharedFlow()
    val captureResults = _captureResults.asSharedFlow()
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()
    val editTarget: StateFlow<PersonEditTarget?> = _editTarget.asStateFlow()

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

    val examHistory: StateFlow<List<ExamHistoryRow>> = examRepository.examHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
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
                    _loginState.value = LoginUiState(
                        error = getApplication<Application>().getString(R.string.vm_login_generic_error)
                    )
                    return@launch
                }

            when (result) {
                is LoginResult.Success -> {
                    _loginState.value = LoginUiState()
                    onSuccess()
                }

                LoginResult.InvalidCredentials -> {
                    _loginState.value =
                        LoginUiState(
                            error = getApplication<Application>().getString(R.string.vm_login_invalid_credentials)
                        )
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
                                 getApplication<Application>().getString(R.string.vm_wifi_connect_failed_socket)
                             )
                             return@launch
                         }

                         _events.emit(getApplication<Application>().getString(R.string.vm_reader_connected))
                         onConnected()

                         // 장비 모델 조회는 연결 성공과 분리
                         launch { refreshDeviceInfo() }
                     } catch (e: Exception) {
                         Log.e("Mark1ViewModel", "리더기 연결 실패", e)
                         _events.emit(
                             e.message ?: getApplication<Application>().getString(R.string.vm_reader_connect_failed)
                         )
                     } finally {
                         isCosmasConnectionProcessing = false
                     }
                 }
            },

            onUnavailable = {
                isCosmasConnectionProcessing = false

                viewModelScope.launch {
                    _events.emit(getApplication<Application>().getString(R.string.vm_wifi_not_found))
                }
            },

            onLost = {
                isCosmasConnectionProcessing = false

                viewModelScope.launch {
                    // 먼저 열린 소켓을 닫고 Network 참조를 제거
                    deviceManager.disconnect()
                    deviceManager.clearNetwork()

                    _events.emit(getApplication<Application>().getString(R.string.vm_wifi_disconnected))
                }
            },
        )
    }

    fun restoreKnownDeviceConnection() {
        if (isCosmasConnectionProcessing) return

        // 소켓이 이미 연결돼 있다면 재연결은 필요 없지만, 화면을 다시 열 때마다
        // 장비 정보(이름 등)는 최신값으로 다시 받아온다.
        if (deviceManager.status.value.socketConnected) {
            viewModelScope.launch { refreshDeviceInfo() }
            return
        }

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

    private suspend fun refreshDeviceInfo() {
        runCatching {
            deviceManager.loadDeviceInfo()
        }.onSuccess { info ->
            Log.d("Mark1ViewModel", "DEV_INFO 조회 결과: $info")
            // 기기가 알려준 이름을 리더기 이름으로 그대로 반영한다.
            if (info != null && info.name.isNotBlank()) {
                ReaderPreferences.setReaderName(getApplication(), info.name)
            }
        }.onFailure { error ->
            Log.w("Mark1ViewModel", "DEV_INFO 응답을 받지 못했습니다.", error)
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
                if (!ok) _events.emit(getApplication<Application>().getString(R.string.vm_backlight_control_failed))
            } catch (e: Exception) {
                _events.emit(e.message ?: getApplication<Application>().getString(R.string.vm_backlight_control_failed))
            }

        }
    }

    fun captureImage() {
        viewModelScope.launch {
            try {
                val ok = deviceManager.captureImage()
                val app = getApplication<Application>()
                _events.emit(if (ok) app.getString(R.string.vm_capture_command_sent) else app.getString(R.string.vm_capture_failed))
            } catch (e: Exception) {
                _events.emit(e.message ?: getApplication<Application>().getString(R.string.vm_capture_failed))
            }
        }
    }

    fun readTemperature() {
        viewModelScope.launch {
            try {
                val temp = deviceManager.readTemperature()
                val app = getApplication<Application>()
                _events.emit(
                    if (!temp.isNullOrBlank()) app.getString(R.string.vm_temperature_current, temp)
                    else app.getString(R.string.vm_temperature_missing)
                )
            } catch (e: Exception) {
                _events.emit(e.message ?: getApplication<Application>().getString(R.string.vm_temperature_check_failed))
            }
        }
    }

    fun applySetting(groupCode: String, key: String, value: String) {
        viewModelScope.launch {
            try {
                val ok = deviceManager.sendSetting(groupCode, key, value)
                val app = getApplication<Application>()
                _events.emit(if (ok) app.getString(R.string.vm_setting_sent, key) else app.getString(R.string.vm_setting_send_failed))
            } catch (e: Exception) {
                _events.emit(e.message ?: getApplication<Application>().getString(R.string.vm_setting_send_failed))
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
                    error = e.message ?: getApplication<Application>().getString(R.string.vm_capture_analysis_failed),
                )
                _events.emit(e.message ?: getApplication<Application>().getString(R.string.vm_capture_analysis_failed))
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

    /** Diagnosis Report에서 과거 검사 기록을 선택했을 때 그 결과를 다시 불러온다. */
    suspend fun loadCaptureResult(captureId: String): CaptureResult? =
        examRepository.captureResultOf(captureId)

    suspend fun loadPersonInfo(personId: String?): PersonInfo? =
        examRepository.personInfo(personId)

    /** 대상자 정보 수정 화면으로 넘어가기 전에 편집 대상을 기억해둔다. */
    fun setEditTarget(personId: String?, captureId: String?, info: PersonInfo) {
        _editTarget.value = PersonEditTarget(personId, captureId, info)
    }

    fun clearEditTarget() {
        _editTarget.value = null
    }

    /**
     * 대상자 정보를 저장한다. captureId가 있으면(검사 이력에서 연 경우) 저장/매칭된 인물로
     * 그 촬영을 다시 연결해서, 처음에 personId가 비어 있던 촬영도 다음에 열면 방금 저장한
     * 값이 그대로 보이게 한다.
     */
    suspend fun savePersonInfo(info: PersonInfo, personId: String?, captureId: String?): String? {
        val resolvedPersonId = examRepository.savePersonInfo(info, personId)
        if (captureId != null && resolvedPersonId != null && resolvedPersonId != personId) {
            examRepository.relinkCapturePerson(captureId, resolvedPersonId)
        }
        return resolvedPersonId
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { deviceManager.disconnect() }
    }
}
