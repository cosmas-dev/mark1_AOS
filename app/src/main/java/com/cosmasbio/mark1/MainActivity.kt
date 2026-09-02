package com.cosmasbio.mark1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.cosmasbio.mark1.ui.Mark1App
import com.cosmasbio.mark1.ui.theme.IotAppTheme

// BiometricPrompt 가 FragmentActivity 를 요구하므로 ComponentActivity 대신 상속한다.
// (FragmentActivity 는 ComponentActivity 의 하위 클래스라 기존 동작은 그대로다)
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // setContent 보다 먼저 호출해야 스플래시가 정상 표시된다.
        // (안드로이드 12 미만에서도 같은 화면이 나오도록 AndroidX 구현을 쓴다)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            IotAppTheme {
                Mark1App()
            }
        }
    }
}
