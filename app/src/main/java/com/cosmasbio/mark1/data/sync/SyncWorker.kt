package com.cosmasbio.mark1.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cosmasbio.mark1.data.local.AnalysisEntity
import com.cosmasbio.mark1.data.local.CaptureEntity
import com.cosmasbio.mark1.data.local.Mark1Database
import com.cosmasbio.mark1.data.remote.AnalysisUploadDto
import com.cosmasbio.mark1.data.remote.ApiClient
import com.cosmasbio.mark1.data.remote.CaptureCreateDto
import com.cosmasbio.mark1.data.remote.LoginRequestDto
import com.cosmasbio.mark1.data.remote.Mark1Api
import com.cosmasbio.mark1.data.remote.PersonCreateDto
import com.cosmasbio.mark1.data.remote.SyncConfig
import com.cosmasbio.mark1.data.repository.ExamRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Room 에 저장된 미전송 검사 데이터를 서버로 업로드하는 백그라운드 작업.
 *
 * - 앱 종료/네트워크 장애 이후에도 WorkManager 가 재시도한다.
 * - 전송 순서: 대상자 → 검사 메타데이터 → 이미지 → 분석 결과 (모두 멱등)
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = ExamRepository(Mark1Database.getInstance(applicationContext))
        val pending = repository.pendingCaptures()
        if (pending.isEmpty()) return Result.success()

        val baseUrl = SyncConfig.serverUrl(applicationContext)

        val api: Mark1Api = try {
            val token = ApiClient.create(baseUrl).login(
                LoginRequestDto(
                    username = SyncConfig.username(applicationContext),
                    password = SyncConfig.password(applicationContext),
                )
            ).accessToken
            ApiClient.create(baseUrl, token)
        } catch (e: Exception) {
            Log.w(TAG, "서버 로그인 실패 - 다음 주기에 재시도", e)
            return Result.retry()
        }

        var anyFailed = false
        for (capture in pending) {
            try {
                repository.markUploading(capture.captureId)
                uploadOne(api, repository, capture)
                repository.markSynced(capture.captureId, remoteImageKeyOf(capture))
                Log.i(TAG, "synced capture=${capture.captureId}")
            } catch (e: Exception) {
                Log.w(TAG, "sync failed capture=${capture.captureId}", e)
                repository.markFailed(capture.captureId, e.message)
                anyFailed = true
            }
        }
        return if (anyFailed) Result.retry() else Result.success()
    }

    private suspend fun uploadOne(
        api: Mark1Api,
        repository: ExamRepository,
        capture: CaptureEntity,
    ) {
        // 1) 대상자 (있을 때만)
        val person = repository.person(capture.personId)
        val serverPersonId = person?.let {
            api.createPerson(
                PersonCreateDto(
                    personId = it.personId,
                    name = it.name,
                    dateOfBirth = it.dateOfBirth,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    organization = it.organization,
                )
            ).personId
        }

        val analysis = repository.analysisOf(capture.captureId)

        // 2) 검사 메타데이터 (촬영 직후 계산한 SHA-256 포함)
        api.createCapture(
            CaptureCreateDto(
                captureId = capture.captureId,
                personId = serverPersonId,
                deviceId = capture.deviceId,
                capturedAt = Instant.ofEpochMilli(capture.capturedAt).toString(),
                imageSha256 = capture.imageSha256.ifBlank { null },
                imageContentType = contentTypeOf(capture.localImagePath),
                testType = analysis?.testType ?: "",
            )
        )

        // 3) 원본 이미지
        val imageFile = File(capture.localImagePath)
        if (imageFile.exists()) {
            val body = imageFile.asRequestBody(contentTypeOf(capture.localImagePath).toMediaType())
            api.uploadImage(
                capture.captureId,
                MultipartBody.Part.createFormData("file", imageFile.name, body),
            )
        }

        // 4) 분석 결과
        if (analysis != null) {
            api.uploadAnalysis(capture.captureId, analysis.toDto())
        }
    }

    private fun remoteImageKeyOf(capture: CaptureEntity): String {
        val ext = if (capture.localImagePath.endsWith(".png")) "png" else "jpg"
        return "captures/${capture.captureId}.$ext"
    }

    private fun contentTypeOf(path: String): String =
        if (path.endsWith(".png")) "image/png" else "image/jpeg"

    private fun AnalysisEntity.toDto() = AnalysisUploadDto(
        testType = testType,
        testInfo = testInfo,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        roiX = roiX,
        roiY = roiY,
        roiW = roiW,
        roiH = roiH,
        channelName = channelName,
        noiseSigma = noiseSigma,
        cPosition = cPosition,
        cSnr = cSnr,
        tPosition = tPosition,
        tSnr = tSnr,
        tDetected = tDetected,
        tWeak = tWeak,
        h1SplitValid = h1SplitValid,
        peakSeparationPx = peakSeparationPx,
        numPeaks = numPeaks,
        rawJson = rawJson,
    )

    companion object {
        private const val TAG = "SyncWorker"
        private const val UNIQUE_WORK_NAME = "mark1_exam_sync"

        /**
         * 동기화 예약. 네트워크 연결 시 실행되고, 실패하면 지수 백오프로 재시도한다.
         * 앱이 종료되어도 WorkManager 가 큐를 유지한다.
         */
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request,
            )
        }
    }
}
