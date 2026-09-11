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
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.R
import com.cosmasbio.mark1.model.AnalysisReport
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
    val context = LocalContext.current
    var saveMessage by remember(imagePath) { mutableStateOf<String?>(null) }

    fun saveImage() {
        val result = saveCaptureToGallery(context, imagePath)
        saveMessage = result.fold(
            onSuccess = { context.getString(R.string.gray_result_save_success) },
            onFailure = { error ->
                val errorMessage = error.message ?: context.getString(R.string.gray_result_unknown_error)
                context.getString(R.string.gray_result_save_failure, errorMessage)
            },
        )
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) saveImage() else saveMessage = context.getString(R.string.gray_result_permission_required)
    }

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
                title = { Text(stringResource(R.string.gray_result_title)) },
                navigationIcon = { TextButton(onClick = onBackHome) { Text(stringResource(R.string.gray_result_home_button)) } },
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
                            contentDescription = stringResource(R.string.gray_result_cropped_image_content_description),
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Text(stringResource(R.string.gray_result_image_unavailable))
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.gray_result_name_label, name))
                    Text(stringResource(R.string.gray_result_type_label, type))
                    Text(stringResource(R.string.gray_result_info_label, if (info.isBlank()) "-" else info))
                    Text(stringResource(R.string.gray_result_path_label, imagePath))
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.gray_result_analysis_heading))
                    if (analysis == null) {
                        Text(stringResource(R.string.gray_result_no_analysis))
                    } else {
                        // Text("채널: ${analysis.channelName}")
                        // Text("ROI: x=${analysis.roiX}, y=${analysis.roiY}, w=${analysis.roiW}, h=${analysis.roiH}")
                        // Text("Noise Sigma: ${analysis.noiseSigma}")
                        // Text("C 위치: ${analysis.cPosition ?: "-"}")
                        Text(stringResource(R.string.gray_result_c_snr_label, analysis.cSnr?.toString() ?: "-"))
                        // Text("T 위치: ${analysis.tPosition ?: "-"}")
                        Text(stringResource(R.string.gray_result_t_snr_label, analysis.tSnr?.toString() ?: "-"))
                        Text(
                            stringResource(
                                R.string.gray_result_t_detected_label,
                                if (analysis.tDetected) stringResource(R.string.gray_result_detected) else stringResource(R.string.gray_result_not_detected),
                            )
                        )
                        Text(
                            stringResource(
                                R.string.gray_result_t_weak_label,
                                if (analysis.tWeak) stringResource(R.string.gray_result_yes) else stringResource(R.string.gray_result_no),
                            )
                        )
                        // Text("H1 Split 유효: ${if (analysis.h1SplitValid) "예" else "아니오"}")
                        // Text("Peak Separation(px): ${analysis.peakSeparationPx}")
                        // Text("Peak 개수: ${analysis.numPeaks}")
                    }
                }
            }

            Button(
                onClick = {
                    if (
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        saveImage()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.gray_result_save_to_gallery_button))
            }

            saveMessage?.let { message -> Text(message) }

            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.gray_result_retry_button))
            }
            Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.gray_result_back_home_button))
            }
        }
    }
}

private fun saveCaptureToGallery(context: Context, imagePath: String): Result<Unit> =
    runCatching {
        val source = File(imagePath)
        require(source.exists() && source.length() > 0L) {
            context.getString(R.string.gray_result_file_not_found)
        }

        val isPng = source.extension.equals("png", ignoreCase = true)
        val extension = if (isPng) "png" else "jpg"
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val displayName = "COSMAS_${System.currentTimeMillis()}.$extension"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "COSMAS",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error(context.getString(R.string.gray_result_cannot_create_location))

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(source).use { input -> input.copyTo(output) }
                } ?: error(context.getString(R.string.gray_result_cannot_open_file))

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (error: Throwable) {
                resolver.delete(uri, null, null)
                throw error
            }
        } else {
            @Suppress("DEPRECATION")
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "COSMAS",
            ).apply { mkdirs() }
            val destination = File(directory, displayName)
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output -> input.copyTo(output) }
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destination.absolutePath),
                arrayOf(mimeType),
                null,
            )
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
