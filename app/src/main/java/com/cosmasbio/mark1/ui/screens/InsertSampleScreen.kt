//package com.cosmasbio.mark1.ui.screens
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.cosmasbio.mark1.model.CaptureUiState
//import com.cosmasbio.mark1.model.TestDraft
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun InsertSampleScreen(
//    draft: TestDraft,
//    captureUiState: CaptureUiState,
//    onBack: () -> Unit,
//    onStartCapture: () -> Unit,
//    onDismissError: () -> Unit,
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("촬영 준비") },
//                navigationIcon = { TextButton(onClick = { if (!captureUiState.capturing) onBack() }) { Text("뒤로") } },
//            )
//        },
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            Card(modifier = Modifier.fillMaxWidth()) {
//                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text("1. 샘플을 키트에 떨어뜨려 주세요.", style = MaterialTheme.typography.titleMedium)
//                    Text("2. 키트를 리더기에 넣고 닫아 주세요.")
//                    Text("3. 이미지 추가하기")
//                    if (draft.delaySeconds.isNotBlank() && draft.delaySeconds != "0") {
//                        Text("지연 시간: ${draft.delaySeconds}초")
//                    }
//                }
//            }
//
//            if (captureUiState.capturing) {
//                Card(modifier = Modifier.fillMaxWidth()) {
//                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        CircularProgressIndicator()
//                        if (captureUiState.remainingSeconds > 0) Text("촬영까지 ${captureUiState.remainingSeconds}초")
//                        else Text("촬영 진행중...")
//                    }
//                }
//            }
//
//            if (!captureUiState.error.isNullOrBlank()) {
//                Card(modifier = Modifier.fillMaxWidth()) {
//                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Text("오류: ${captureUiState.error}", color = MaterialTheme.colorScheme.error)
//                        TextButton(onClick = onDismissError) { Text("닫기") }
//                    }
//                }
//            }
//
//            Button(
//                onClick = onStartCapture,
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !captureUiState.capturing,
//            ) {
//                Text(if (captureUiState.capturing) "촬영 진행중..." else "촬영 시작")
//            }
//        }
//    }
//}
package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.model.CaptureUiState
import com.cosmasbio.mark1.model.TestDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertSampleScreen(
    draft: TestDraft,
    captureUiState: CaptureUiState,
    onBack: () -> Unit,
    onStartCapture: () -> Unit,
    onDismissError: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("촬영 준비") },
                navigationIcon = {
                    TextButton(onClick = { if (!captureUiState.capturing && !captureUiState.analyzing) onBack() }) {
                        Text("뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. 샘플을 키트에 떨어뜨려 주세요.", style = MaterialTheme.typography.titleMedium)
                    Text("2. 키트를 리더기에 넣고 닫아 주세요.")
                    Text("3. 촬영 후 자동 분석이 진행됩니다.")
                    if (draft.delaySeconds.isNotBlank() && draft.delaySeconds != "0") {
                        Text("지연 시간: ${draft.delaySeconds}초")
                    }
                }
            }

            if (captureUiState.capturing || captureUiState.analyzing) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        when {
                            captureUiState.remainingSeconds > 0 ->
                                Text("촬영까지 ${captureUiState.remainingSeconds}초")
                            captureUiState.analyzing ->
                                Text("이미지 분석중...")
                            else ->
                                Text("촬영 진행중...")
                        }
                    }
                }
            }

            if (!captureUiState.error.isNullOrBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("오류: ${captureUiState.error}", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onDismissError) { Text("닫기") }
                    }
                }
            }

            Button(
                onClick = onStartCapture,
                modifier = Modifier.fillMaxWidth(),
                enabled = !captureUiState.capturing && !captureUiState.analyzing,
            ) {
                Text(
                    when {
                        captureUiState.analyzing -> "분석 진행중..."
                        captureUiState.capturing -> "촬영 진행중..."
                        else -> "촬영 시작"
                    }
                )
            }
        }
    }
}