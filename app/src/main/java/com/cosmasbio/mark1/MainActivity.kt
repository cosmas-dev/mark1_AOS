package com.cosmasbio.mark1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cosmasbio.mark1.ui.Mark1App
import com.cosmasbio.mark1.ui.theme.IotAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IotAppTheme {
                Mark1App()
            }
        }
    }
}
