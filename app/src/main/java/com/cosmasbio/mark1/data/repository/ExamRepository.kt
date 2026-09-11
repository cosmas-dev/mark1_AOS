package com.cosmasbio.mark1.data.repository

import androidx.room.withTransaction
import com.cosmasbio.mark1.data.local.AnalysisEntity
import com.cosmasbio.mark1.data.local.CaptureEntity
import com.cosmasbio.mark1.data.local.Mark1Database
import com.cosmasbio.mark1.data.local.PersonEntity
import com.cosmasbio.mark1.data.local.SyncStatus
import com.cosmasbio.mark1.model.AnalysisReport
import com.cosmasbio.mark1.model.CaptureResult
import com.cosmasbio.mark1.model.ExamHistoryRow
import com.cosmasbio.mark1.model.PersonInfo
import kotlinx.coroutines.flow.Flow
import java.util.UUID


/**
 * 검사 데이터 로컬 저장소.
 *
 * 촬영/분석이 완료되면 네트워크 상태와 무관하게 반드시 Room 에 먼저 저장한다.
 * 서버 전송은 WorkManager(SyncWorker)가 별도로 수행한다.
 */
class ExamRepository(private val db: Mark1Database) {

    /**
     * 촬영 + 분석 결과를 하나의 트랜잭션으로 저장한다.
     * @return 저장된 captureId
     */
    suspend fun saveExam(
        person: PersonInfo,
        capture: CaptureResult,
        analysis: AnalysisReport?,
        deviceId: String,
    ): String {
        val now = System.currentTimeMillis()
        val captureId = capture.captureId.ifBlank { UUID.randomUUID().toString() }

        db.withTransaction {
            val personId = resolvePersonId(person, now)

            db.captureDao().upsert(
                CaptureEntity(
                    captureId = captureId,
                    personId = personId,
                    deviceId = deviceId,
                    capturedAt = capture.capturedAtMillis.takeIf { it > 0 } ?: now,
                    localImagePath = capture.imagePath,
                    remoteImageKey = null,
                    imageSha256 = capture.imageSha256,
                    syncStatus = SyncStatus.LOCAL,
                    updatedAt = now,
                )
            )

            if (analysis != null) {
                db.analysisDao().upsert(
                    AnalysisEntity(
                        analysisId = UUID.randomUUID().toString(),
                        captureId = captureId,
                        testType = capture.type,
                        testInfo = capture.info,
                        imageWidth = analysis.imageWidth,
                        imageHeight = analysis.imageHeight,
                        roiX = analysis.roiX,
                        roiY = analysis.roiY,
                        roiW = analysis.roiW,
                        roiH = analysis.roiH,
                        channelName = analysis.channelName,
                        noiseSigma = analysis.noiseSigma,
                        cPosition = analysis.cPosition,
                        cSnr = analysis.cSnr,
                        tPosition = analysis.tPosition,
                        tSnr = analysis.tSnr,
                        tDetected = analysis.tDetected,
                        tWeak = analysis.tWeak,
                        h1SplitValid = analysis.h1SplitValid,
                        peakSeparationPx = analysis.peakSeparationPx,
                        numPeaks = analysis.numPeaks,
                        rawJson = analysis.rawJson,
                        analyzedAt = now,
                    )
                )
            }
        }
        return captureId
    }

    private suspend fun resolvePersonId(person: PersonInfo, now: Long): String? {
        if (person.isEmpty) return null
        // 이름+생년월일이 같으면 동일 인물로 재사용
        val existing = if (person.dateOfBirth.isNotBlank()) {
            db.personDao().findByNameAndBirth(person.name, person.dateOfBirth)
        } else {
            null
        }
        if (existing != null) return existing.personId

        val personId = UUID.randomUUID().toString()
        db.personDao().upsert(
            PersonEntity(
                personId = personId,
                name = person.name,
                dateOfBirth = person.dateOfBirth,
                email = person.email,
                phoneNumber = person.phoneNumber,
                organization = person.organization,
                createdAt = now,
            )
        )
        return personId
    }

    suspend fun markUploadPending(captureId: String) {
        db.captureDao().updateSyncStatus(captureId, SyncStatus.UPLOAD_PENDING)
    }

    suspend fun pendingCaptures(): List<CaptureEntity> = db.captureDao().findPendingSync()

    suspend fun person(personId: String?): PersonEntity? =
        personId?.let { db.personDao().findById(it) }

    suspend fun analysisOf(captureId: String): AnalysisEntity? =
        db.analysisDao().findByCaptureId(captureId)

    suspend fun markUploading(captureId: String) =
        db.captureDao().updateSyncStatus(captureId, SyncStatus.UPLOADING)

    suspend fun markSynced(captureId: String, remoteImageKey: String?) =
        db.captureDao().markSynced(captureId, remoteImageKey)

    suspend fun markFailed(captureId: String, error: String?) =
        db.captureDao().updateSyncStatus(captureId, SyncStatus.FAILED, error?.take(500))

    suspend fun unsyncedCount(): Int = db.captureDao().countUnsynced()

    fun examHistory(): Flow<List<ExamHistoryRow>> = db.captureDao().observeExamHistory()

    /** Diagnosis Report에서 과거 검사 하나를 눌렀을 때, 저장된 촬영/분석 결과를 다시 조립한다. */
    suspend fun captureResultOf(captureId: String): CaptureResult? {
        val capture = db.captureDao().findById(captureId) ?: return null
        val analysisEntity = db.analysisDao().findByCaptureId(captureId)
        val person = capture.personId?.let { db.personDao().findById(it) }

        val analysis = analysisEntity?.let {
            AnalysisReport(
                rawJson = it.rawJson,
                imagePath = capture.localImagePath,
                imageWidth = it.imageWidth,
                imageHeight = it.imageHeight,
                roiX = it.roiX,
                roiY = it.roiY,
                roiW = it.roiW,
                roiH = it.roiH,
                channelName = it.channelName,
                noiseSigma = it.noiseSigma,
                cPosition = it.cPosition,
                cSnr = it.cSnr,
                tPosition = it.tPosition,
                tSnr = it.tSnr,
                tDetected = it.tDetected,
                tWeak = it.tWeak,
                h1SplitValid = it.h1SplitValid,
                peakSeparationPx = it.peakSeparationPx,
                numPeaks = it.numPeaks,
            )
        }

        return CaptureResult(
            imagePath = capture.localImagePath,
            name = person?.name.orEmpty(),
            type = analysisEntity?.testType.orEmpty(),
            info = analysisEntity?.testInfo.orEmpty(),
            analysis = analysis,
            captureId = capture.captureId,
            imageSha256 = capture.imageSha256,
            capturedAtMillis = capture.capturedAt,
        )
    }

    suspend fun personInfo(personId: String?): PersonInfo? {
        val entity = person(personId) ?: return null
        return PersonInfo(
            name = entity.name,
            dateOfBirth = entity.dateOfBirth,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            organization = entity.organization,
        )
    }

    /**
     * 대상자 정보를 저장한다. personId가 있으면 그 사람 레코드를 그대로 갱신하고,
     * 없으면 이름+생년월일로 기존 인물을 찾아 재사용하거나 새로 만든다([resolvePersonId]).
     */
    suspend fun savePersonInfo(info: PersonInfo, personId: String?): String? {
        val now = System.currentTimeMillis()
        if (personId != null) {
            // upsert(REPLACE) 대신 UPDATE를 써서, 같은 사람의 다른 촬영 기록이
            // captures.personId(ON DELETE SET NULL)로 끊어지지 않게 한다.
            db.personDao().update(
                personId = personId,
                name = info.name,
                dateOfBirth = info.dateOfBirth,
                email = info.email,
                phoneNumber = info.phoneNumber,
                organization = info.organization,
            )
            return personId
        }
        return resolvePersonId(info, now)
    }

    /**
     * Diagnosis Report/Management에서 "..."로 정보를 수정했을 때, 원래 personId가 없었다면
     * (촬영 당시 대상자가 연결되지 않았던 경우) 새로 저장/매칭된 인물을 이 촬영에 다시 연결한다.
     */
    suspend fun relinkCapturePerson(captureId: String, personId: String?) {
        db.captureDao().updatePersonLink(captureId, personId)
    }
}
