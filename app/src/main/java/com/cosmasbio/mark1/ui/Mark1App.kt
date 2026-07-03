//package com.cosmasbio.mark1.ui
//
//import android.net.Uri
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import com.cosmasbio.mark1.ui.screens.AppInitScreen
//import com.cosmasbio.mark1.ui.screens.GrayScaleResultScreen
//import com.cosmasbio.mark1.ui.screens.HomeScreen
//import com.cosmasbio.mark1.ui.screens.InsertSampleScreen
//import com.cosmasbio.mark1.ui.screens.TestInfoScreen
//
//@Composable
//fun Mark1App(
//    viewModel: Mark1ViewModel = viewModel(),
//) {
//    val navController = rememberNavController()
//    val uiState by viewModel.uiState.collectAsState()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    LaunchedEffect(Unit) {
//        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.captureResults.collect { result ->
//            navController.navigate(
//                "result?imagePath=${Uri.encode(result.imagePath)}&name=${Uri.encode(result.name)}&type=${Uri.encode(result.type)}&info=${Uri.encode(result.info)}"
//            )
//        }
//    }
//
//    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
//        NavHost(
//            navController = navController,
//            startDestination = if (uiState.loading) "init" else "home",
//        ) {
//            composable("init") {
//                AppInitScreen(
//                    onReady = {
//                        navController.navigate("home") {
//                            popUpTo("init") { inclusive = true }
//                        }
//                    },
//                    loading = uiState.loading,
//                    deviceStatus = uiState.deviceStatus,
//                )
//            }
//            composable("home") {
//                HomeScreen(
//                    deviceStatus = uiState.deviceStatus,
//                    onReconnect = viewModel::reconnectDevice,
//                    onCapture = viewModel::captureImage,
//                    onToggleLight = viewModel::toggleBacklight,
//                    onReadTemperature = viewModel::readTemperature,
//                    onApplySetting = { groupCode, key, value ->
//                        viewModel.applySetting(groupCode, key, value)
//                    },
//                    onStartDiagnosis = { navController.navigate("testInfo") },
//                )
//            }
//            composable("testInfo") {
//                TestInfoScreen(
//                    draft = uiState.draft,
//                    onBack = { navController.popBackStack() },
//                    onDraftChange = viewModel::updateDraft,
//                    onNext = { navController.navigate("insertSample") },
//                )
//            }
//            composable("insertSample") {
//                InsertSampleScreen(
//                    draft = uiState.draft,
//                    captureUiState = uiState.capture,
//                    onBack = { navController.popBackStack() },
//                    onStartCapture = viewModel::startFlutterStyleCapture,
//                    onDismissError = viewModel::clearCaptureError,
//                )
//            }
//            composable(
//                route = "result?imagePath={imagePath}&name={name}&type={type}&info={info}",
//                arguments = listOf(
//                    navArgument("imagePath") { type = NavType.StringType },
//                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
//                    navArgument("type") { type = NavType.StringType; defaultValue = "" },
//                    navArgument("info") { type = NavType.StringType; defaultValue = "" },
//                )
//            ) { backStackEntry ->
//                GrayScaleResultScreen(
//                    imagePath = Uri.decode(backStackEntry.arguments?.getString("imagePath") ?: ""),
//                    name = Uri.decode(backStackEntry.arguments?.getString("name") ?: ""),
//                    type = Uri.decode(backStackEntry.arguments?.getString("type") ?: ""),
//                    info = Uri.decode(backStackEntry.arguments?.getString("info") ?: ""),
//                    onRetry = {
//                        navController.navigate("testInfo") {
//                            popUpTo("testInfo") { inclusive = true }
//                        }
//                    },
//                    onBackHome = {
//                        navController.navigate("home") {
//                            popUpTo("home") { inclusive = true }
//                        }
//                    },
//                )
//            }
//        }
//    }
//}
package com.cosmasbio.mark1.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cosmasbio.mark1.ui.screens.AppInitScreen
import com.cosmasbio.mark1.ui.screens.GrayScaleResultScreen
import com.cosmasbio.mark1.ui.screens.HomeScreen
import com.cosmasbio.mark1.ui.screens.InsertSampleScreen
import com.cosmasbio.mark1.ui.screens.TestInfoScreen

@Composable
fun Mark1App(
    viewModel: Mark1ViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            startDestination = if (uiState.loading) "init" else "home",
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
                    onStartDiagnosis = { navController.navigate("testInfo") },
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