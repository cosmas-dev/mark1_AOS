package com.cosmasbio.mark1.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

@Composable
fun Mark1App(
    viewModel: Mark1ViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.captureResults.collect {
            navController.navigate("result")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        NavHost(
            navController = navController,
//            startDestination = if (uiState.loading) "init" else "home",
            startDestination = "home",
        ) {
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
                            name = profile.name,
                            type = profile.diagnosisType,
                            info = profile.diagnosisItems,
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
                        viewModel.updateDraft(name = details.name)
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
                AnalysisProgressScreen(
                    onClose = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() },
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
                    onBack = { navController.popBackStack() },
                    onRestart = {
                        navController.navigate("diagnose") {
                            popUpTo("diagnose") { inclusive = true }
                        }
                    },
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
