package com.cosmasbio.mark1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}

@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(analysis: AnalysisEntity)

    @Query("SELECT * FROM analysis_results WHERE captureId = :captureId")
    suspend fun findByCaptureId(captureId: String): AnalysisEntity?
}
