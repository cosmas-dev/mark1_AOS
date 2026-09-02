package com.cosmasbio.mark1.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cosmasbio.mark1.model.PersonInfo
import com.cosmasbio.mark1.ui.screens.AddDiagnosisDetailsScreen
import com.cosmasbio.mark1.ui.screens.AppInitScreen
import com.cosmasbio.mark1.ui.screens.GrayScaleResultScreen
import com.cosmasbio.mark1.ui.screens.HomeScreen
import com.cosmasbio.mark1.ui.screens.InsertSampleScreen
import com.cosmasbio.mark1.ui.screens.TestInfoScreen
import com.cosmasbio.mark1.ui.screens.ConnectDeviceScreen
import com.cosmasbio.mark1.ui.screens.DiagnoseScreen
import com.cosmasbio.mark1.ui.screens.ScanScreen
import com.cosmasbio.mark1.ui.screens.AnalysisProgressScreen
import com.cosmasbio.mark1.ui.screens.ReportResultScreen
import com.cosmasbio.mark1.ui.screens.IntroScreen
import com.cosmasbio.mark1.ui.screens.LoginScreen
import com.cosmasbio.mark1.ui.screens.MenuScreen
import com.cosmasbio.mark1.ui.screens.NotificationsScreen
import com.cosmasbio.mark1.ui.screens.PostTestActionScreen
import com.cosmasbio.mark1.ui.screens.SaveCompleteScreen

@Composable
fun Mark1App(
    viewModel: Mark1ViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val startDestination = "intro"
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.captureResults.collect {
            // 기존 GIST 테스트 경로(insertSample)에서만 원본 결과 화면으로 이동한다.
            // 진단 플로우는 analysisProgress 화면이 직접 다음 화면을 결정한다.
            if (navController.currentDestination?.route == "insertSample") {
                navController.navigate("result")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        NavHost(
            navController = navController,
//            startDestination = if (uiState.loading) "init" else "home",
            startDestination = startDestination,
        ) {
            composable("intro") {
                IntroScreen(
                    onLogin = { navController.navigate("login") },
                )
            }

            composable("login") {
                val loginState by viewModel.loginState.collectAsState()

                LoginScreen(
                    uiState = loginState,
                    onBack = { navController.popBackStack() },
                    onClearError = viewModel::clearLoginError,
                    onSubmit = { email, password ->
                        viewModel.login(email, password) {
                            navController.navigate("home") {
                                popUpTo("intro") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable("init") {
                AppInitScreen(
                    onReady = {
                        navController.navigate("home") {
                            popUpTo("init") { inclusive = true }
                        }
                    },
                    loading = uiState.loading,
                    deviceStatus = uiState.deviceStatus,
                )
            }

            composable("home") {
                HomeScreen(
                    deviceStatus = uiState.deviceStatus,
                    onReconnect = viewModel::reconnectDevice,
                    onCapture = viewModel::captureImage,
                    onToggleLight = viewModel::toggleBacklight,
                    onReadTemperature = viewModel::readTemperature,
                    onApplySetting = { groupCode, key, value ->
                        viewModel.applySetting(groupCode, key, value)
                    },
                    onStartDiagnosis = { navController.navigate("connectDevice") },
                    onNotificationClick = { navController.navigate("notifications") },
                    onMenuClick = { navController.navigate("menu") },
                )
            }

            composable("notifications") {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable("menu") {
                MenuScreen(
                    onBack = { navController.popBackStack() },
                    onNotifications = { navController.navigate("notifications") },
                )
            }

            composable("connectDevice") {
                LaunchedEffect(Unit) {
                    viewModel.restoreKnownDeviceConnection()
                }

                ConnectDeviceScreen(
                    isConnected = uiState.deviceStatus.socketConnected,

                    onClose = {
                        navController.popBackStack()
                    },

                    onConnect = {
                        viewModel.connectCosmasWifi(
                            onConnected = {
                                // 연결 성공 후 바로 이동하지 않고
                                // 현재 화면에서 체크와 Continue 버튼을 보여줌
//                                navController.navigate("testInfo") {
//                                    popUpTo("connectDevice") {
//                                        inclusive = true
//                                    }
//                                }
//                                // 여기까지 GIST 테스트용 => 안쓸 땐 주석처리
                            }
                        )
                    },

                    onContinue = {
                        navController.navigate("diagnose") {
                        // GIST 테스트용
//                        navController.navigate("testInfo") {
                            popUpTo("connectDevice") {
                                inclusive = true
                            }
                        }
                    },
                )
            }

            composable("diagnose") {
                DiagnoseScreen(
                    onClose = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onProfileClick = { profile ->
                        viewModel.updateDraft(
                            type = profile.diagnosisType,
                            info = profile.diagnosisItems,
                        )
                        // 이름만 넘기면 서버 persons 테이블에 생년월일/연락처가 비게 되므로
                        // 목록에서 고른 대상자도 상세 정보를 함께 채워 보낸다.
                        viewModel.updatePersonInfo(
                            PersonInfo(
                                name = profile.name,
                                dateOfBirth = profile.dateOfBirth,
                                email = profile.email,
                                phoneNumber = profile.phoneNumber,
                                organization = profile.organization,
                            )
                        )
                        navController.navigate("scan")
                    },
                    onAddClick = {
                        viewModel.updateDraft(
                            name = "",
                            type = "Multi-Panel Drug Test",
                            info = "",
                        )
                        navController.navigate("addDiagnosisDetails")
                    },
                )
            }

            composable("addDiagnosisDetails") {
                AddDiagnosisDetailsScreen(
                    onClose = { navController.popBackStack() },
                    onRegister = { details ->
                        viewModel.updatePersonInfo(
                            PersonInfo(
                                name = details.name,
                                dateOfBirth = details.dateOfBirth,
                                email = details.email,
                                phoneNumber = details.phoneNumber,
                                organization = details.company,
                            )
                        )
                        navController.navigate("insertSample")
                    },
                )
            }

            composable("scan") {
                ScanScreen(
                    onClose = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onContinue = { navController.navigate("analysisProgress") },
                )
            }

            composable("analysisProgress") {
                var analysisDone by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    viewModel.captureResults.collect { analysisDone = true }
                }

                AnalysisProgressScreen(
                    captureUiState = uiState.capture,
                    analysisDone = analysisDone,
                    onStartCapture = viewModel::startFlutterStyleCapture,
                    onDismissError = viewModel::clearCaptureError,
                    onClose = {
                        viewModel.cancelCapture()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onCancel = {
                        viewModel.cancelCapture()
                        navController.popBackStack()
                    },
                    onComplete = {
                        navController.navigate("reportResult") {
                            popUpTo("analysisProgress") { inclusive = true }
                        }
                    },
                )
            }

            composable("reportResult") {
                ReportResultScreen(
                    profileName = uiState.draft.name,
                    captureResult = uiState.latestCaptureResult,
                    onSaveAndAct = { navController.navigate("postTestAction") },
                    onBack = { navController.popBackStack() },
                    onRestart = {
                        navController.navigate("diagnose") {
                            popUpTo("diagnose") { inclusive = true }
                        }
                    },
                )
            }

            composable("postTestAction") {
                PostTestActionScreen(
                    onBack = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onRestart = {
                        navController.navigate("diagnose") {
                            popUpTo("diagnose") { inclusive = true }
                        }
                    },
                    onSave = { _, _ ->
                        // TODO: 조치 유형/메모를 검사 기록과 함께 저장하고 서버로 동기화한다.
                        navController.navigate("saveComplete/${newExamRecordId()}")
                    },
                )
            }

            composable("saveComplete/{examId}") { entry ->
                SaveCompleteScreen(
                    examId = entry.arguments?.getString("examId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onStartNewExam = {
                        navController.navigate("diagnose") {
                            popUpTo("diagnose") { inclusive = true }
                        }
                    },
                    onViewRecords = { /* TODO: 검사 기록 목록 화면 */ },
                )
            }

            composable("testInfo") {
                TestInfoScreen(
                    draft = uiState.draft,
                    onBack = { navController.popBackStack() },
                    onDraftChange = viewModel::updateDraft,
                    onNext = { navController.navigate("insertSample") },
                )
            }

            composable("insertSample") {
                InsertSampleScreen(
                    draft = uiState.draft,
                    captureUiState = uiState.capture,
                    onBack = { navController.popBackStack() },
                    onStartCapture = viewModel::startFlutterStyleCapture,
                    onDismissError = viewModel::clearCaptureError,
                )
            }

            composable("result") {
                val result = uiState.latestCaptureResult
                if (result != null) {
                    GrayScaleResultScreen(
                        imagePath = result.imagePath,
                        name = result.name,
                        type = result.type,
                        info = result.info,
                        analysis = result.analysis,
                        onRetry = {
                            navController.popBackStack("insertSample", inclusive = false)
                        },
                        onBackHome = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                            }
                        },
                    )
                }
            }
        }
    }
}

/** 검사 기록 ID. 예: TS-2026-0823-0007 */
private fun newExamRecordId(): String {
    val today = java.time.LocalDate.now()
    val sequence = kotlin.random.Random.nextInt(1, 10_000)
    return "TS-%04d-%02d%02d-%04d".format(
        today.year,
        today.monthValue,
        today.dayOfMonth,
        sequence,
    )
}
