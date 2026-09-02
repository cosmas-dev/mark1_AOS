package com.cosmasbio.mark1.data.remote

import android.content.Context

/**
 * 서버 동기화 설정.
 *
 * 현재 기본값은 시연용 EC2(ap-northeast-2) 이며 Let's Encrypt 인증서로 HTTPS 통신한다.
 * 주의: 장비 계정 비밀번호가 소스에 상수로 들어가므로 APK 를 뜯으면 확인할 수 있다.
 * 운영 배포 전에는 기기별 자격증명 발급 방식으로 교체해야 한다.
 */
object SyncConfig {
    private const val PREFS = "mark1_sync_config"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    const val DEFAULT_SERVER_URL = "https://43-201-180-210.sslip.io"
    const val DEFAULT_USERNAME = "device-sync"
    const val DEFAULT_PASSWORD = "vI13EDCLh4WPicqE7DBy8TxN6QXK"

    fun serverUrl(context: Context): String =
        prefs(context).getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL

    fun username(context: Context): String =
        prefs(context).getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME

    fun password(context: Context): String =
        prefs(context).getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD

    fun update(context: Context, serverUrl: String? = null, username: String? = null, password: String? = null) {
        prefs(context).edit().apply {
            serverUrl?.let { putString(KEY_SERVER_URL, it) }
            username?.let { putString(KEY_USERNAME, it) }
            password?.let { putString(KEY_PASSWORD, it) }
        }.apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
