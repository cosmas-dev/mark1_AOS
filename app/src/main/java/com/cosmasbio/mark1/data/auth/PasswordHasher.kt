package com.cosmasbio.mark1.data.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * 비밀번호 해시 유틸리티.
 *
 * 평문 비밀번호는 DB 를 포함해 어디에도 저장하지 않는다.
 * 계정마다 임의 salt 를 생성하고 PBKDF2-HMAC-SHA256 으로 반복 해싱해
 * 레인보우 테이블과 무차별 대입 공격 비용을 높인다.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val SALT_BYTES = 16
    private const val KEY_LENGTH_BITS = 256

    /**
     * 반복 횟수. 기기 성능이 좋아지면 올릴 수 있도록 계정마다 함께 저장해 두고,
     * 검증할 때는 저장된 값을 사용한다.
     */
    const val DEFAULT_ITERATIONS = 100_000

    fun newSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String, iterations: Int = DEFAULT_ITERATIONS): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, KEY_LENGTH_BITS)
        try {
            val key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
            return Base64.encodeToString(key, Base64.NO_WRAP)
        } finally {
            // PBEKeySpec 내부 char 배열을 지워 메모리에 평문이 남지 않게 한다.
            spec.clearPassword()
        }
    }

    /** 타이밍 공격을 피하기 위해 상수 시간 비교를 사용한다. */
    fun verify(
        password: String,
        salt: String,
        iterations: Int,
        expectedHash: String,
    ): Boolean = MessageDigest.isEqual(
        hash(password, salt, iterations).toByteArray(Charsets.UTF_8),
        expectedHash.toByteArray(Charsets.UTF_8),
    )
}
