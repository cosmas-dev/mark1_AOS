package com.cosmasbio.mark1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        PersonEntity::class,
        CaptureEntity::class,
        AnalysisEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class Mark1Database : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun personDao(): PersonDao
    abstract fun captureDao(): CaptureDao
    abstract fun analysisDao(): AnalysisDao

    companion object {
        @Volatile
        private var instance: Mark1Database? = null

        /**
         * v1 -> v2: 로그인 계정 테이블 추가.
         * 기존에 저장된 검사 데이터를 유지해야 하므로 destructive migration 대신
         * 테이블만 새로 만든다.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `accounts` (
                        `accountId` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `passwordIterations` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastLoginAt` INTEGER,
                        PRIMARY KEY(`accountId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_email` " +
                        "ON `accounts` (`email`)"
                )
            }
        }

        fun getInstance(context: Context): Mark1Database =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Mark1Database::class.java,
                    "mark1.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
