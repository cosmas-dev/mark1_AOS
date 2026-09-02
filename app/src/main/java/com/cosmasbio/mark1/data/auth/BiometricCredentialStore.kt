package com.cosmasbio.mark1.data.auth

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 생체인증으로 잠긴 로그인 정보 저장소.
 *
 * 비밀번호는 평문으로 저장하지 않는다. 안드로이드 Keystore 안에 생성한 키로 암호화하고,
 * 그 키는 `setUserAuthenticationRequired(true)` 라서 **생체인증을 통과해야만 사용할 수 있다.**
 * 따라서 기기를 루팅해 저장 파일을 꺼내가도 지문 없이는 복호화되지 않는다.
 */
class BiometricCredentialStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val biometricManager = BiometricManager.from(appContext)

    /** 이 기기에서 생체인증을 쓸 수 있는지. */
    fun canUseBiometric(): Boolean =
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun hasSavedCredential(): Boolean =
        prefs.contains(KEY_SECRET) && prefs.contains(KEY_IV)

    /** 저장된 계정의 이메일. 버튼에 표시하는 용도라 암호화하지 않는다. */
    fun savedEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun clear() {
        prefs.edit().clear().apply()
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        }
    }

    /** 저장용 Cipher. BiometricPrompt 에 넘겨 인증을 통과한 뒤에 [save] 로 전달한다. */
    fun encryptCipher(): Cipher? = runCatching {
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey(recreate = true)) }
    }.getOrElse {
        Log.w(TAG, "암호화 Cipher 생성 실패", it)
        null
    }

    /** 복호화용 Cipher. 저장해 둔 IV 를 사용한다. */
    fun decryptCipher(): Cipher? = runCatching {
        val iv = Base64.decode(prefs.getString(KEY_IV, null) ?: return null, Base64.NO_WRAP)
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey(recreate = false), GCMParameterSpec(TAG_BITS, iv))
        }
    }.getOrElse {
        // 지문을 새로 등록하면 키가 무효화된다. 이 경우 저장된 정보를 버리고 다시 받는다.
        Log.w(TAG, "복호화 Cipher 생성 실패 - 저장된 정보를 삭제한다", it)
        clear()
        null
    }

    fun save(cipher: Cipher, email: String, password: String): Boolean = runCatching {
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_SECRET, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
        true
    }.getOrElse {
        Log.w(TAG, "로그인 정보 저장 실패", it)
        false
    }

    /** @return 성공하면 (이메일, 비밀번호) */
    fun load(cipher: Cipher): Pair<String, String>? = runCatching {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val stored = Base64.decode(prefs.getString(KEY_SECRET, null) ?: return null, Base64.NO_WRAP)
        email to String(cipher.doFinal(stored), Charsets.UTF_8)
    }.getOrElse {
        Log.w(TAG, "로그인 정보 복호화 실패", it)
        null
    }

    private fun secretKey(recreate: Boolean): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!recreate) {
            (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // 생체인증을 통과해야만 이 키를 쓸 수 있다.
            .setUserAuthenticationRequired(true)
            // 지문이 추가/삭제되면 키를 무효화해, 남의 지문으로 열리는 것을 막는다.
            .setInvalidatedByBiometricEnrollment(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private companion object {
        const val TAG = "BiometricCredential"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "mark1_login_credential"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val PREFS = "mark1_biometric_login"
        const val KEY_EMAIL = "email"
        const val KEY_SECRET = "secret"
        const val KEY_IV = "iv"
    }
}
