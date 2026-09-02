package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.model.LoginUiState

private val LoginInk = Color(0xFF16181A)
private val LoginLabel = Color(0xFF7C8287)
private val LoginFieldBorder = Color(0xFFE3E7EA)
private val LoginDisabledBg = Color(0xFFE9EEF2)
private val LoginDisabledText = Color(0xFF9AA3AA)
private val LoginCheckBlue = Color(0xFF4360C5)
private val LoginError = Color(0xFFE0403F)

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onBack: () -> Unit,
    onSubmit: (email: String, password: String, saveBiometric: Boolean) -> Unit,
    onClearError: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var saveBiometric by rememberSaveable { mutableStateOf(false) }

    val biometric = rememberBiometricLoginController()
    val emailValid = remember(email) { isValidEmail(email) }
    val canSubmit = email.isNotBlank() && password.isNotBlank() && !uiState.submitting

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
    ) {
        LoginHeader(onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))

            LoginField(
                label = "이메일",
                value = email,
                onValueChange = {
                    email = it.trim()
                    if (uiState.error != null) onClearError()
                },
                keyboardType = KeyboardType.Email,
                trailing = {
                    if (emailValid) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "사용 가능한 이메일",
                            tint = LoginCheckBlue,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
            )

            Spacer(Modifier.height(18.dp))

            LoginField(
                label = "비밀번호",
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.error != null) onClearError()
                },
                keyboardType = KeyboardType.Password,
                masked = true,
            )

            if (uiState.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = uiState.error,
                    color = LoginError,
                    fontSize = 13.sp,
                )
            }

            if (biometric.available && !biometric.hasSaved) {
                Spacer(Modifier.height(14.dp))
                SaveBiometricToggle(
                    checked = saveBiometric,
                    onCheckedChange = { saveBiometric = it },
                )
            }

            Spacer(Modifier.height(34.dp))
            AccountHelpLinks()

            Spacer(Modifier.height(28.dp))
            SubmitButton(
                enabled = canSubmit,
                submitting = uiState.submitting,
                onClick = { onSubmit(email, password, saveBiometric) },
            )

            if (biometric.hasSaved) {
                Spacer(Modifier.height(12.dp))
                BiometricLoginButton(
                    email = biometric.savedEmail.orEmpty(),
                    enabled = !uiState.submitting,
                    onClick = {
                        biometric.login(
                            onSuccess = { savedEmail, savedPassword ->
                                email = savedEmail
                                password = savedPassword
                                onSubmit(savedEmail, savedPassword, false)
                            },
                            onError = onShowMessage,
                        )
                    },
                    onClear = {
                        biometric.clear()
                        onShowMessage("저장된 생체인증 로그인을 삭제했습니다.")
                    },
                )
            }
        }
    }
}

@Composable
private fun LoginHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "뒤로",
                tint = LoginInk,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "로그인",
            modifier = Modifier.align(Alignment.Center),
            color = LoginInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    masked: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column {
        Text(text = label, color = LoginLabel, fontSize = 13.sp)
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, LoginFieldBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = LoginInk, fontSize = 16.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (masked) {
                    PasswordVisualTransformation()
                } else {
                    androidx.compose.ui.text.input.VisualTransformation.None
                },
                cursorBrush = SolidColor(LoginInk),
            )
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

@Composable
private fun AccountHelpLinks() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HelpLink("아이디 찾기") { /* TODO: 아이디 찾기 */ }
        Text(
            text = "l",
            modifier = Modifier.padding(horizontal = 14.dp),
            color = LoginLabel,
            fontSize = 14.sp,
        )
        HelpLink("비밀번호 찾기") { /* TODO: 비밀번호 찾기 */ }
    }
}

@Composable
private fun HelpLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        color = LoginInk,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun SubmitButton(enabled: Boolean, submitting: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(if (enabled) Color.Black else LoginDisabledBg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (submitting) "로그인 중..." else "로그인",
            color = if (enabled) Color.White else LoginDisabledText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Composable
private fun SaveBiometricToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) LoginCheckBlue else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (checked) LoginCheckBlue else LoginFieldBorder,
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "다음부터 생체인증으로 로그인",
            color = LoginLabel,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun BiometricLoginButton(
    email: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .border(1.5.dp, LoginCheckBlue, RoundedCornerShape(30.dp))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Fingerprint,
            contentDescription = null,
            tint = LoginCheckBlue,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "생체인증으로 로그인",
            color = LoginCheckBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }

    Spacer(Modifier.height(10.dp))
    Text(
        text = if (email.isBlank()) "저장된 로그인 삭제" else "$email · 저장 해제",
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClear,
            ),
        color = LoginLabel,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

private fun isValidEmail(value: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
