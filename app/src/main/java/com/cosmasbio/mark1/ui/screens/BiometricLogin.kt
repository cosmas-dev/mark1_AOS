package com.cosmasbio.mark1.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.cosmasbio.mark1.data.auth.BiometricCredentialStore

/** 로그인 화면에서 쓰는 생체인증 상태와 동작. */
class BiometricLoginController internal constructor(
    private val store: BiometricCredentialStore,
    private val activity: FragmentActivity?,
    private val refresh: () -> Unit,
) {
    /** 기기가 생체인증을 지원하고 등록된 지문/얼굴이 있는지. */
    val available: Boolean get() = activity != null && store.canUseBiometric()

    /** 저장된 로그인 정보가 있는지. */
    val hasSaved: Boolean get() = available && store.hasSavedCredential()

    val savedEmail: String? get() = store.savedEmail()

    /** 생체인증으로 저장된 로그인 정보를 꺼낸다. */
    fun login(onSuccess: (email: String, password: String) -> Unit, onError: (String) -> Unit) {
        val act = activity ?: return onError("생체인증을 사용할 수 없습니다.")
        val cipher = store.decryptCipher() ?: run {
            refresh()
            return onError("저장된 로그인 정보가 만료되었습니다. 비밀번호로 다시 로그인해 주세요.")
        }

        prompt(
            activity = act,
            title = "생체인증으로 로그인",
            subtitle = store.savedEmail().orEmpty(),
            cipher = cipher,
            onError = onError,
        ) { result ->
            val resultCipher = result.cryptoObject?.cipher ?: return@prompt onError("인증에 실패했습니다.")
            val loaded = store.load(resultCipher)
            if (loaded == null) {
                refresh()
                onError("저장된 로그인 정보를 읽지 못했습니다.")
            } else {
                onSuccess(loaded.first, loaded.second)
            }
        }
    }

    /** 로그인에 성공한 계정을 생체인증으로 잠가 저장한다. */
    fun save(email: String, password: String, onDone: (Boolean) -> Unit) {
        val act = activity ?: return onDone(false)
        val cipher = store.encryptCipher() ?: return onDone(false)

        prompt(
            activity = act,
            title = "생체인증 등록",
            subtitle = "다음 로그인부터 생체인증을 사용합니다",
            cipher = cipher,
            onError = { onDone(false) },
        ) { result ->
            val resultCipher = result.cryptoObject?.cipher
            val saved = resultCipher != null && store.save(resultCipher, email, password)
            refresh()
            onDone(saved)
        }
    }

    /** 저장된 로그인 정보를 삭제한다. */
    fun clear() {
        store.clear()
        refresh()
    }

    private fun prompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        cipher: javax.crypto.Cipher,
        onError: (String) -> Unit,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                onSuccess(result)

            override fun onAuthenticationError(code: Int, message: CharSequence) {
                // 사용자가 직접 취소한 경우는 오류로 알리지 않는다.
                if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                    code != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onError(message.toString())
                }
            }
        }

        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("취소")
                .setConfirmationRequired(false)
                .build(),
            BiometricPrompt.CryptoObject(cipher),
        )
    }
}

@Composable
fun rememberBiometricLoginController(): BiometricLoginController {
    val context = LocalContext.current
    // 상태가 바뀌면(저장/삭제) 화면이 다시 그려지도록 하는 트리거.
    var version by remember { mutableStateOf(0) }

    return remember(context, version) {
        BiometricLoginController(
            store = BiometricCredentialStore(context),
            activity = context.findActivity(),
            refresh = { version++ },
        )
    }
}

private fun Context.findActivity(): FragmentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
