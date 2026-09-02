package com.cosmasbio.mark1.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** 검사 기록을 조회하는 관리자 웹 시스템 주소. */
const val ADMIN_CONSOLE_URL = "https://cosmas-d-free-investigation.kordokrip.workers.dev/"

/**
 * "관리자 시스템에서 보기" 동작.
 * 기본 브라우저로 관리자 페이지를 연다. 브라우저가 없는 기기에서는 안내만 띄우고 넘어간다.
 */
@Composable
fun rememberOpenAdminConsole(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(ADMIN_CONSOLE_URL))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
                Log.w("AdminConsole", "브라우저를 열 수 없습니다.", e)
                Toast.makeText(context, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
