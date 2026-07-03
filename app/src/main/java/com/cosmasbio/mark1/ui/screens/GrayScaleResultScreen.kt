//package com.cosmasbio.mark1.ui.screens
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun GrayScaleResultScreen(
//    imagePath: String,
//    name: String,
//    type: String,
//    info: String,
//    onRetry: () -> Unit,
//    onBackHome: () -> Unit,
//) {
//    val croppedBitmap = remember(imagePath) {
//        runCatching {
//            val original = BitmapFactory.decodeFile(imagePath) ?: return@runCatching null
//            cropCenterBitmap(
//                source = original,
//                widthRatio = 0.3f,
//                heightRatio = 0.3f
//            )
//        }.getOrNull()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("촬영 결과") },
//                navigationIcon = { TextButton(onClick = onBackHome) { Text("홈") } },
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(16.dp)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            Card(modifier = Modifier.fillMaxWidth()) {
//                Column(
//                    modifier = Modifier.padding(12.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    if (croppedBitmap != null) {
//                        Image(
//                            bitmap = croppedBitmap.asImageBitmap(),
//                            contentDescription = "cropped captured image",
//                            modifier = Modifier.fillMaxWidth(),
//                            contentScale = ContentScale.FillWidth
//                        )
//                    } else {
//                        Text("이미지를 표시할 수 없습니다.")
//                    }
//                }
//            }
//
//            Card(modifier = Modifier.fillMaxWidth()) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text("이름: $name")
//                    Text("타입: $type")
//                    Text("설명: ${if (info.isBlank()) "-" else info}")
//                    Text("저장 경로: $imagePath")
//                }
//            }
//
//            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
//                Text("다시 촬영하기")
//            }
//            Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
//                Text("홈으로")
//            }
//        }
//    }
//}
//
//private fun cropCenterBitmap(
//    source: Bitmap,
//    widthRatio: Float = 0.5f,
//    heightRatio: Float = 0.5f
//): Bitmap {
//    val cropWidth = (source.width * widthRatio).toInt().coerceAtLeast(1)
//    val cropHeight = (source.height * heightRatio).toInt().coerceAtLeast(1)
//
//    val left = ((source.width - cropWidth) / 2).coerceAtLeast(0)
//    val top = ((source.height - cropHeight) / 2).coerceAtLeast(0)
//
//    return Bitmap.createBitmap(
//        source,
//        left,
//        top,
//        cropWidth.coerceAtMost(source.width - left),
//        cropHeight.coerceAtMost(source.height - top)
//    )
//}
package com.cosmasbio.mark1.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.model.AnalysisReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrayScaleResultScreen(
    imagePath: String,
    name: String,
    type: String,
    info: String,
    analysis: AnalysisReport?,
    onRetry: () -> Unit,
    onBackHome: () -> Unit,
) {
    val croppedBitmap = remember(imagePath) {
        runCatching {
            val original = BitmapFactory.decodeFile(imagePath) ?: return@runCatching null
            cropCenterBitmap(
                source = original,
                widthRatio = 0.3f,
                heightRatio = 0.3f
            )
        }.getOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("촬영 결과") },
                navigationIcon = { TextButton(onClick = onBackHome) { Text("홈") } },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (croppedBitmap != null) {
                        Image(
                            bitmap = croppedBitmap.asImageBitmap(),
                            contentDescription = "cropped captured image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Text("이미지를 표시할 수 없습니다.")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("이름: $name")
                    Text("타입: $type")
                    Text("설명: ${if (info.isBlank()) "-" else info}")
                    Text("저장 경로: $imagePath")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("분석 결과")
                    if (analysis == null) {
                        Text("분석 결과가 없습니다.")
                    } else {
                        Text("채널: ${analysis.channelName}")
                        Text("ROI: x=${analysis.roiX}, y=${analysis.roiY}, w=${analysis.roiW}, h=${analysis.roiH}")
                        Text("Noise Sigma: ${analysis.noiseSigma}")
                        Text("C 위치: ${analysis.cPosition ?: "-"}")
                        Text("C SNR: ${analysis.cSnr ?: "-"}")
                        Text("T 위치: ${analysis.tPosition ?: "-"}")
                        Text("T SNR: ${analysis.tSnr ?: "-"}")
                        Text("T 검출: ${if (analysis.tDetected) "검출" else "미검출"}")
                        Text("약한 T: ${if (analysis.tWeak) "예" else "아니오"}")
                        Text("H1 Split 유효: ${if (analysis.h1SplitValid) "예" else "아니오"}")
                        Text("Peak Separation(px): ${analysis.peakSeparationPx}")
                        Text("Peak 개수: ${analysis.numPeaks}")
                    }
                }
            }

            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("다시 촬영하기")
            }
            Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                Text("홈으로")
            }
        }
    }
}

private fun cropCenterBitmap(
    source: Bitmap,
    widthRatio: Float = 0.5f,
    heightRatio: Float = 0.5f
): Bitmap {
    val cropWidth = (source.width * widthRatio).toInt().coerceAtLeast(1)
    val cropHeight = (source.height * heightRatio).toInt().coerceAtLeast(1)

    val left = ((source.width - cropWidth) / 2).coerceAtLeast(0)
    val top = ((source.height - cropHeight) / 2).coerceAtLeast(0)

    return Bitmap.createBitmap(
        source,
        left,
        top,
        cropWidth.coerceAtMost(source.width - left),
        cropHeight.coerceAtMost(source.height - top)
    )
}