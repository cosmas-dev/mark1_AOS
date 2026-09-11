//package com.cosmasbio.mark1.model
//
//enum class ConnectionStage {
//    Idle,
//    ConnectingWifi,
//    WifiConnected,
//    SocketConnected,
//    Failed
//}
//
//data class DeviceStatus(
//    val stage: ConnectionStage = ConnectionStage.Idle,
//    val wifiConnected: Boolean = false,
//    val socketConnected: Boolean = false,
//    val modelName: String = "COSMAS-1000",
//    val lightOn: Boolean = false,
//    val temperature: String = "",
//    val lastMessage: String = "",
//    val lastError: String? = null,
//)
//
//data class TestDraft(
//    val name: String = "",
//    val type: String = "Cortisol",
//    val info: String = "",
//    val delaySeconds: String = "0",
//)
//
//data class CaptureResult(
//    val imagePath: String = "",
//    val name: String = "",
//    val type: String = "",
//    val info: String = "",
//)
//
//data class CaptureUiState(
//    val capturing: Boolean = false,
//    val remainingSeconds: Int = 0,
//    val error: String? = null,
//)
//
//data class AppUiState(
//    val loading: Boolean = true,
//    val deviceStatus: DeviceStatus = DeviceStatus(),
//    val draft: TestDraft = TestDraft(),
//    val capture: CaptureUiState = CaptureUiState(),
//)
package com.cosmasbio.mark1.model

enum class ConnectionStage {
    Idle,
    ConnectingWifi,
    WifiConnected,
    SocketConnected,
    Failed
}

data class DeviceStatus(
    val stage: ConnectionStage = ConnectionStage.Idle,
    val wifiConnected: Boolean = false,
    val socketConnected: Boolean = false,
    val modelName: String = "COSMAS-1000",
    val lightOn: Boolean = false,
    val temperature: String = "",
    val lastMessage: String = "",
    val lastError: String? = null,
)

/** DEV_INFO 응답(body.DATA)을 그대로 옮긴 구조체. */
data class DeviceInfo(
    val device: String = "",
    val name: String = "",
    val light: String = "",
    val kit: String = "",
    val mode: String = "",
    val calib: String = "",
    val status: String = "",
)

data class TestDraft(
    val name: String = "",
    val type: String = "Cortisol",
    val info: String = "",
    val delaySeconds: String = "0",
    // AddDiagnosisDetailsScreen 에서 입력한 대상자 정보 전체
    val person: PersonInfo = PersonInfo(),
)

data class AnalysisReport(
    val rawJson: String = "",
    val imagePath: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val roiX: Int = 0,
    val roiY: Int = 0,
    val roiW: Int = 0,
    val roiH: Int = 0,
    val channelName: String = "",
    val noiseSigma: Double = 0.0,
    val cPosition: Int? = null,
    val cSnr: Double? = null,
    val tPosition: Int? = null,
    val tSnr: Double? = null,
    val tDetected: Boolean = false,
    val tWeak: Boolean = false,
    val h1SplitValid: Boolean = false,
    val peakSeparationPx: Double = 0.0,
    val numPeaks: Int = 0,
)

data class CaptureResult(
    val imagePath: String = "",
    val name: String = "",
    val type: String = "",
    val info: String = "",
    val analysis: AnalysisReport? = null,
    // UUID 기반 촬영 식별자(파일명과 동일). 개인정보는 파일명에 포함하지 않는다.
    val captureId: String = "",
    // 촬영 직후 계산한 원본 이미지 SHA-256 (무결성 검증용)
    val imageSha256: String = "",
    val capturedAtMillis: Long = 0L,
)

data class CaptureUiState(
    val capturing: Boolean = false,
    val analyzing: Boolean = false,
    val remainingSeconds: Int = 0,
    val error: String? = null,
)

data class AppUiState(
    val loading: Boolean = true,
    val deviceStatus: DeviceStatus = DeviceStatus(),
    val draft: TestDraft = TestDraft(),
    val capture: CaptureUiState = CaptureUiState(),
    val latestCaptureResult: CaptureResult? = null,
)
data class LoginUiState(
    val submitting: Boolean = false,
    val error: String? = null,
)

/** Diagnosis Report 화면의 검사 이력 한 줄. captures/persons/analysis_results를 조인한 결과. */
data class ExamHistoryRow(
    val captureId: String,
    val personId: String?,
    val personName: String,
    val organization: String,
    val diagnosisType: String,
    val diagnosisItems: String,
    val capturedAt: Long,
    val positive: Boolean,
)
