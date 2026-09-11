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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.R
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
                title = { Text(stringResource(R.string.insert_sample_title)) },
                navigationIcon = {
                    TextButton(onClick = { if (!captureUiState.capturing && !captureUiState.analyzing) onBack() }) {
                        Text(stringResource(R.string.insert_sample_back_button))
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
                    Text(stringResource(R.string.insert_sample_step1), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.insert_sample_step2))
                    Text(stringResource(R.string.insert_sample_step3))
                    if (draft.delaySeconds.isNotBlank() && draft.delaySeconds != "0") {
                        Text(stringResource(R.string.insert_sample_delay_seconds, draft.delaySeconds))
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
                                Text(stringResource(R.string.insert_sample_countdown_seconds, captureUiState.remainingSeconds))
                            captureUiState.analyzing ->
                                Text(stringResource(R.string.insert_sample_analyzing))
                            else ->
                                Text(stringResource(R.string.insert_sample_capturing))
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
                        Text(stringResource(R.string.insert_sample_error_prefix, captureUiState.error ?: ""), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onDismissError) { Text(stringResource(R.string.insert_sample_close_button)) }
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
                        captureUiState.analyzing -> stringResource(R.string.insert_sample_analyzing_button)
                        captureUiState.capturing -> stringResource(R.string.insert_sample_capturing)
                        else -> stringResource(R.string.insert_sample_start_button)
                    }
                )
            }
        }
    }
}