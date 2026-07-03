package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.model.ConnectionStage
import com.cosmasbio.mark1.model.DeviceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    deviceStatus: DeviceStatus,
    onReconnect: () -> Unit,
    onCapture: () -> Unit,
    onToggleLight: () -> Unit,
    onReadTemperature: () -> Unit,
    onApplySetting: (String, String, String) -> Unit = { _, _, _ -> },
    onStartDiagnosis: () -> Unit,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf("FOCUS") }
    var settingValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("IoT 연결") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(
                deviceStatus = deviceStatus,
                onReconnect = onReconnect
            )
            ActionButton("사진 촬영", onCapture)
            ActionButton(
                if (deviceStatus.lightOn) "백라이트 끄기" else "백라이트 켜기",
                onToggleLight
            )
            ActionButton("테스트 시작", onStartDiagnosis)
            ActionButton("온도 체크", onReadTemperature)
            ActionButton("세팅") { showSettingsDialog = true }
            ActionButton("저장된 데이터") {}
        }
    }

    if (showSettingsDialog) {
        SettingValueDialog(
            selectedSetting = selectedSetting,
            value = settingValue,
            onDismiss = { showSettingsDialog = false },
            onSettingChange = { selectedSetting = it },
            onValueChange = { settingValue = it },
            onConfirm = {
                onApplySetting(
                    "A001",
                    selectedSetting,
                    settingValue
                )
                showSettingsDialog = false
            },
        )
    }
}

@Composable
private fun StatusCard(
    deviceStatus: DeviceStatus,
    onReconnect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("연결 상태", style = MaterialTheme.typography.titleMedium)

            val summary = when (deviceStatus.stage) {
                ConnectionStage.SocketConnected -> "리더기 연결 완료"
                ConnectionStage.WifiConnected -> "Wi-Fi 연결 확인됨"
                ConnectionStage.ConnectingWifi -> "리더기 연결 중"
                ConnectionStage.Failed -> "연결 필요"
                ConnectionStage.Idle -> "연결 대기"
            }

            Text(summary)

            if (deviceStatus.temperature.isNotBlank()) {
                Text("온도: ${deviceStatus.temperature}℃")
            }

            if (!deviceStatus.lastError.isNullOrBlank()) {
                Text("오류: ${deviceStatus.lastError}")
            }

            TextButton(
                onClick = onReconnect,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("다시 연결")
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
private fun SettingValueDialog(
    selectedSetting: String,
    value: String,
    onDismiss: () -> Unit,
    onSettingChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val options = listOf(
        "FOCUS",
        "EXPOSURE",
        "D_GAIN",
        "A_GAIN",
        "BL_DUTY",
        "ISO",
        "SHUTTER"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("세팅값 설정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    TextButton(
                        onClick = { onSettingChange(option) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedSetting == option) "✓ $option" else option)
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("$selectedSetting 값") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}