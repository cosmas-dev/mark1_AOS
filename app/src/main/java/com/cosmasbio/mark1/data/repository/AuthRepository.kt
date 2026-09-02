package com.cosmasbio.mark1.data.repository

import com.cosmasbio.mark1.data.auth.PasswordHasher
import com.cosmasbio.mark1.data.local.AccountEntity
import com.cosmasbio.mark1.data.local.Mark1Database
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface LoginResult {
    data class Success(val accountId: String, val displayName: String) : LoginResult
    data object InvalidCredentials : LoginResult
}

/**
 * 로그인 계정 저장소.
 *
 * 비밀번호는 PBKDF2 해시로만 보관하며, 평문은 검증 순간에만 메모리에 존재한다.
 */
class AuthRepository(private val db: Mark1Database) {

    /**
     * 테스트 계정이 없으면 생성한다. 이미 있으면 아무것도 하지 않는다.
     *
     * 주의: 아래 비밀번호는 소스에 그대로 들어가므로 APK 를 뜯으면 확인할 수 있다.
     * 개발/시연용으로만 사용하고, 실제 배포 전에는 서버 인증으로 교체해야 한다.
     */
    suspend fun seedTestAccountIfMissing() = withContext(Dispatchers.IO) {
        val dao = db.accountDao()
        if (dao.findByEmail(TEST_EMAIL) != null) return@withContext

        val salt = PasswordHasher.newSalt()
        dao.insert(
            AccountEntity(
                accountId = UUID.randomUUID().toString(),
                email = TEST_EMAIL,
                displayName = "코스마스",
                passwordHash = PasswordHasher.hash(TEST_PASSWORD, salt),
                passwordSalt = salt,
                passwordIterations = PasswordHasher.DEFAULT_ITERATIONS,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val account = db.accountDao().findByEmail(normalizedEmail)

        if (account == null) {
            // 계정이 없을 때 즉시 실패시키면 응답 시간 차이로 가입 여부가 드러난다.
            // 동일한 비용의 해시를 한 번 수행해 타이밍을 맞춘다.
            PasswordHasher.hash(password, DUMMY_SALT)
            return@withContext LoginResult.InvalidCredentials
        }

        val matched = PasswordHasher.verify(
            password = password,
            salt = account.passwordSalt,
            iterations = account.passwordIterations,
            expectedHash = account.passwordHash,
        )
        if (!matched) return@withContext LoginResult.InvalidCredentials

        db.accountDao().updateLastLogin(account.accountId, System.currentTimeMillis())
        LoginResult.Success(account.accountId, account.displayName)
    }

    private companion object {
        const val TEST_EMAIL = "info@cosmasbio.com"
        const val TEST_PASSWORD = "Cosmasbio0222!"

        /** 계정이 없을 때 타이밍을 맞추기 위한 더미 salt. */
        const val DUMMY_SALT = "AAAAAAAAAAAAAAAAAAAAAA=="
    }
}
