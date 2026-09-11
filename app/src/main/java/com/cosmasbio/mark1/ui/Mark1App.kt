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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cosmasbio.mark1.data.local.ReaderPreferences
import com.cosmasbio.mark1.model.CaptureResult
import com.cosmasbio.mark1.model.PersonInfo
import kotlinx.coroutines.launch
import com.cosmasbio.mark1.ui.screens.AddDiagnosisDetailsScreen
import com.cosmasbio.mark1.ui.screens.DiagnosisDetailsInput
import com.cosmasbio.mark1.ui.screens.AppInitScreen
import com.cosmasbio.mark1.ui.screens.DiagnosisManagementScreen
import com.cosmasbio.mark1.ui.screens.DiagnosisReportScreen
import com.cosmasbio.mark1.ui.screens.GrayScaleResultScreen
import com.cosmasbio.mark1.ui.screens.HomeScreen
import com.cosmasbio.mark1.ui.screens.KitDetailsScreen
import com.cosmasbio.mark1.ui.screens.InsertSampleScreen
import com.cosmasbio.mark1.ui.screens.TestInfoScreen
import com.cosmasbio.mark1.ui.screens.ConnectDeviceScreen
import com.cosmasbio.mark1.ui.screens.DiagnoseScreen
import com.cosmasbio.mark1.ui.screens.ScanScreen
import com.cosmasbio.mark1.ui.screens.AnalysisProgressScreen
import com.cosmasbio.mark1.ui.screens.ReportResultScreen
import com.cosmasbio.mark1.ui.screens.IntroScreen
import com.cosmasbio.mark1.ui.screens.LoginScreen
import com.cosmasbio.mark1.ui.screens.rememberBiometricLoginController
import com.cosmasbio.mark1.ui.screens.MenuScreen
import com.cosmasbio.mark1.ui.screens.NotificationsScreen
import com.cosmasbio.mark1.ui.screens.PostTestActionScreen
import com.cosmasbio.mark1.ui.screens.ProductsScreen
import com.cosmasbio.mark1.ui.screens.ReaderInfoScreen
import com.cosmasbio.mark1.ui.screens.ReaderNameScreen
import com.cosmasbio.mark1.ui.screens.ReaderSettingsScreen
import com.cosmasbio.mark1.ui.screens.SaveCompleteScreen

@Composable
fun Mark1App(
    viewModel: Mark1ViewModel = viewModel(),
) {
    val navController = rememberNavController()
    // TODO: 테스트 편의상 "home"으로 바꿔둠. 로그인 플로우 확인할 땐 "intro"로 되돌릴 것.
    val startDestination = "home"
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
                val biometric = rememberBiometricLoginController()

                LoginScreen(
                    uiState = loginState,
                    onBack = { navController.popBackStack() },
                    onClearError = viewModel::clearLoginError,
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                    onSubmit = { email, password, saveBiometric ->
                        viewModel.login(email, password) {
                            val goHome = {
                                navController.navigate("home") {
                                    popUpTo("intro") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            // 비밀번호가 맞는 것을 확인한 뒤에만 생체인증으로 잠가 저장한다.
                            if (saveBiometric) {
                                biometric.save(email, password) { goHome() }
                            } else {
                                goHome()
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
                    onSettingsClick = { navController.navigate("products") },
                    onDocumentClick = { navController.navigate("diagnosisReport") },
                    onChecklistClick = { navController.navigate("diagnosisManagement") },
                )
            }

            composable("products") {
                val context = LocalContext.current
                ProductsScreen(
                    onMenuClick = { navController.navigate("menu") },
                    onNotificationClick = { navController.navigate("notifications") },
                    onBackToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onOpenReaderSettings = { navController.navigate("readerSettings") },
                    onAddNewReader = { navController.navigate("connectDevice") },
                    onOpenKitDetails = { navController.navigate("kitDetails") },
                    onOpenDiagnosisReport = { navController.navigate("diagnosisReport") },
                    onOpenDiagnosisManagement = { navController.navigate("diagnosisManagement") },
                    // 화면에 재진입할 때마다 새로 읽어서, 이름을 바꾸거나 기기를 삭제하고
                    // 돌아오면 바로 반영된다.
                    readerName = remember { ReaderPreferences.getReaderName(context) },
                    isReaderRegistered = remember { ReaderPreferences.isReaderRegistered(context) },
                )
            }

            composable("kitDetails") {
                KitDetailsScreen(onBack = { navController.popBackStack() })
            }

            composable("diagnosisReport") {
                val examHistory by viewModel.examHistory.collectAsState()
                DiagnosisReportScreen(
                    examHistory = examHistory,
                    onMenuClick = { navController.navigate("menu") },
                    onAddClick = { navController.navigate("diagnose") },
                    onItemClick = { row -> navController.navigate("reportResult?captureId=${row.captureId}") },
                    onMoreClick = { row ->
                        scope.launch {
                            val info = viewModel.loadPersonInfo(row.personId)
                                ?: PersonInfo(name = row.personName)
                            viewModel.setEditTarget(row.personId, row.captureId, info)
                            navController.navigate("editDiagnosisDetails")
                        }
                    },
                    onHomeClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onChecklistClick = { navController.navigate("diagnosisManagement") },
                    onSettingsClick = { navController.navigate("products") },
                )
            }

            composable("diagnosisManagement") {
                val examHistory by viewModel.examHistory.collectAsState()
                DiagnosisManagementScreen(
                    examHistory = examHistory,
                    onMenuClick = { navController.navigate("menu") },
                    onNotificationClick = { navController.navigate("notifications") },
                    onCreateClick = { navController.navigate("diagnose") },
                    onItemClick = { row -> navController.navigate("reportResult?captureId=${row.captureId}") },
                    onMoreClick = { row ->
                        scope.launch {
                            val info = viewModel.loadPersonInfo(row.personId)
                                ?: PersonInfo(name = row.personName)
                            viewModel.setEditTarget(row.personId, row.captureId, info)
                            navController.navigate("editDiagnosisDetails")
                        }
                    },
                    onHomeClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onDocumentClick = { navController.navigate("diagnosisReport") },
                    onSettingsClick = { navController.navigate("products") },
                )
            }

            composable("readerSettings") {
                val context = LocalContext.current
                ReaderSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenReaderName = { navController.navigate("readerName") },
                    onOpenReaderInfo = { navController.navigate("readerInfo") },
                    onDeleteConfirmed = {
                        ReaderPreferences.setReaderRegistered(context, false)
                        navController.popBackStack("products", inclusive = false)
                    },
                    readerName = remember { ReaderPreferences.getReaderName(context) },
                )
            }

            composable("readerName") {
                val context = LocalContext.current
                ReaderNameScreen(
                    initialName = remember { ReaderPreferences.getReaderName(context) },
                    onBack = { navController.popBackStack() },
                    onSave = { newName ->
                        ReaderPreferences.setReaderName(context, newName)
                        navController.popBackStack()
                    },
                )
            }

            composable("readerInfo") {
                ReaderInfoScreen(onBack = { navController.popBackStack() })
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
                val context = LocalContext.current

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
                                // 실제 리더기와 연결됐으니 Products 화면 기준으로도 등록된 것으로 저장한다.
                                ReaderPreferences.setReaderRegistered(context, true)
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
                    onClose = { navController.popBackStack() },
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
                    onMoreClick = { profile ->
                        viewModel.setEditTarget(
                            personId = null,
                            captureId = null,
                            info = PersonInfo(
                                name = profile.name,
                                dateOfBirth = profile.dateOfBirth,
                                email = profile.email,
                                phoneNumber = profile.phoneNumber,
                                organization = profile.organization,
                            ),
                        )
                        navController.navigate("editDiagnosisDetails")
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

            composable("editDiagnosisDetails") {
                val editTarget by viewModel.editTarget.collectAsState()
                val target = editTarget
                if (target != null) {
                    AddDiagnosisDetailsScreen(
                        initialValue = DiagnosisDetailsInput(
                            name = target.info.name,
                            dateOfBirth = target.info.dateOfBirth,
                            email = target.info.email,
                            phoneNumber = target.info.phoneNumber,
                            company = target.info.organization,
                        ),
                        onClose = {
                            viewModel.clearEditTarget()
                            navController.popBackStack()
                        },
                        onRegister = { details ->
                            scope.launch {
                                viewModel.savePersonInfo(
                                    PersonInfo(
                                        name = details.name,
                                        dateOfBirth = details.dateOfBirth,
                                        email = details.email,
                                        phoneNumber = details.phoneNumber,
                                        organization = details.company,
                                    ),
                                    personId = target.personId,
                                    captureId = target.captureId,
                                )
                                viewModel.clearEditTarget()
                                navController.popBackStack()
                            }
                        },
                    )
                }
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

            composable(
                route = "reportResult?captureId={captureId}",
                arguments = listOf(
                    navArgument("captureId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) { backStackEntry ->
                val captureId = backStackEntry.arguments?.getString("captureId")

                // captureId가 있으면(Diagnosis Report에서 과거 기록을 눌러 들어온 경우)
                // 저장된 결과를 불러오고, 없으면(방금 촬영을 마친 흐름) 기존처럼 최신 결과를 그대로 쓴다.
                var historicalResult by remember { mutableStateOf<CaptureResult?>(null) }
                LaunchedEffect(captureId) {
                    if (captureId != null) {
                        historicalResult = viewModel.loadCaptureResult(captureId)
                    }
                }

                val captureResult = if (captureId != null) historicalResult else uiState.latestCaptureResult
                val profileName = if (captureId != null) {
                    historicalResult?.name.orEmpty()
                } else {
                    uiState.draft.name
                }

                ReportResultScreen(
                    profileName = profileName,
                    captureResult = captureResult,
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
