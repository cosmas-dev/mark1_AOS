package com.cosmasbio.mark1.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 서버 전송 상태.
 * 촬영/분석 완료 즉시 Room 에 먼저 저장되므로,
 * 네트워크 장애나 앱 종료가 있어도 현장 데이터는 유실되지 않는다.
 */
enum class SyncStatus {
    LOCAL,           // 로컬 저장만 완료
    UPLOAD_PENDING,  // 전송 대기(WorkManager 예약됨)
    UPLOADING,       // 전송 중
    SYNCED,          // 서버 저장 완료
    FAILED,          // 전송 실패(다음 동기화 때 재시도)
}

/**
 * 로그인 계정.
 *
 * 비밀번호는 평문으로 저장하지 않고, 계정마다 다른 salt 로 PBKDF2 해싱한 값만 보관한다.
 * (해시 생성/검증은 [com.cosmasbio.mark1.data.auth.PasswordHasher] 참고)
 */
@Entity(
    tableName = "accounts",
    indices = [Index("email", unique = true)],
)
data class AccountEntity(
    @PrimaryKey val accountId: String,
    /** 대소문자 구분 없이 조회하기 위해 소문자로 정규화해 저장한다. */
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val passwordSalt: String,
    val passwordIterations: Int,
    val createdAt: Long,
    val lastLoginAt: Long? = null,
)

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey val personId: String,
    val name: String,
    val dateOfBirth: String,
    val email: String,
    val phoneNumber: String,
    val organization: String,
    val createdAt: Long,
)

@Entity(
    tableName = "captures",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["personId"],
            childColumns = ["personId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("personId"), Index("syncStatus"), Index("capturedAt")],
)
data class CaptureEntity(
    @PrimaryKey val captureId: String,
    val personId: String?,
    val deviceId: String,
    val capturedAt: Long,
    val localImagePath: String,
    val remoteImageKey: String?,
    val imageSha256: String,
    val syncStatus: SyncStatus,
    val syncError: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["captureId"],
            childColumns = ["captureId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("captureId", unique = true)],
)
data class AnalysisEntity(
    @PrimaryKey val analysisId: String,
    val captureId: String,
    val testType: String,
    val testInfo: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val roiX: Int,
    val roiY: Int,
    val roiW: Int,
    val roiH: Int,
    val channelName: String,
    val noiseSigma: Double,
    val cPosition: Int?,
    val cSnr: Double?,
    val tPosition: Int?,
    val tSnr: Double?,
    val tDetected: Boolean,
    val tWeak: Boolean,
    val h1SplitValid: Boolean,
    val peakSeparationPx: Double,
    val numPeaks: Int,
    val rawJson: String,
    val analyzedAt: Long,
)
