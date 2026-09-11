package com.cosmasbio.mark1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cosmasbio.mark1.model.ExamHistoryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): AccountEntity?

    @Query("UPDATE accounts SET lastLoginAt = :now WHERE accountId = :accountId")
    suspend fun updateLastLogin(accountId: String, now: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int
}

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(person: PersonEntity)

    @Query("SELECT * FROM persons WHERE personId = :personId")
    suspend fun findById(personId: String): PersonEntity?

    @Query("SELECT * FROM persons WHERE name = :name AND dateOfBirth = :dateOfBirth LIMIT 1")
    suspend fun findByNameAndBirth(name: String, dateOfBirth: String): PersonEntity?

    // upsert(REPLACE)는 같은 personId로 충돌할 때 내부적으로 delete+insert를 하기 때문에,
    // captures.personId(ON DELETE SET NULL)가 끊어질 수 있다. 기존 인물 수정은 반드시 이 UPDATE로 한다.
    @Query(
        "UPDATE persons SET name = :name, dateOfBirth = :dateOfBirth, email = :email, " +
            "phoneNumber = :phoneNumber, organization = :organization WHERE personId = :personId"
    )
    suspend fun update(
        personId: String,
        name: String,
        dateOfBirth: String,
        email: String,
        phoneNumber: String,
        organization: String,
    )
}

@Dao
interface CaptureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(capture: CaptureEntity)

    @Query("SELECT * FROM captures WHERE captureId = :captureId")
    suspend fun findById(captureId: String): CaptureEntity?

    @Query(
        "SELECT * FROM captures WHERE syncStatus IN ('LOCAL', 'UPLOAD_PENDING', 'FAILED') " +
            "ORDER BY capturedAt ASC"
    )
    suspend fun findPendingSync(): List<CaptureEntity>

    @Query("UPDATE captures SET syncStatus = :status, syncError = :error, updatedAt = :now WHERE captureId = :captureId")
    suspend fun updateSyncStatus(
        captureId: String,
        status: SyncStatus,
        error: String? = null,
        now: Long = System.currentTimeMillis(),
    )

    @Query(
        "UPDATE captures SET syncStatus = 'SYNCED', remoteImageKey = :remoteImageKey, " +
            "syncError = NULL, updatedAt = :now WHERE captureId = :captureId"
    )
    suspend fun markSynced(
        captureId: String,
        remoteImageKey: String?,
        now: Long = System.currentTimeMillis(),
    )

    @Query("SELECT COUNT(*) FROM captures WHERE syncStatus != 'SYNCED'")
    suspend fun countUnsynced(): Int

    /** 검사 이력 화면에서 대상자 정보를 새로 등록/매칭한 뒤, 해당 촬영을 그 인물과 연결한다. */
    @Query("UPDATE captures SET personId = :personId, updatedAt = :now WHERE captureId = :captureId")
    suspend fun updatePersonLink(
        captureId: String,
        personId: String?,
        now: Long = System.currentTimeMillis(),
    )

    @Query(
        "SELECT c.captureId AS captureId, " +
            "c.personId AS personId, " +
            "COALESCE(p.name, '') AS personName, " +
            "COALESCE(p.organization, '') AS organization, " +
            "COALESCE(a.testType, '') AS diagnosisType, " +
            "COALESCE(a.testInfo, '') AS diagnosisItems, " +
            "c.capturedAt AS capturedAt, " +
            "COALESCE(a.tDetected, 0) AS positive " +
            "FROM captures c " +
            "LEFT JOIN persons p ON c.personId = p.personId " +
            "LEFT JOIN analysis_results a ON a.captureId = c.captureId " +
            "ORDER BY c.capturedAt DESC"
    )
    fun observeExamHistory(): Flow<List<ExamHistoryRow>>
}

@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(analysis: AnalysisEntity)

    @Query("SELECT * FROM analysis_results WHERE captureId = :captureId")
    suspend fun findByCaptureId(captureId: String): AnalysisEntity?
}
