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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class DiagnosisDetailsInput(
    val name: String = "",
    val dateOfBirth: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val company: String = "",
)

private val DetailsTop = Color(0xFFE8F0F5)
private val DetailsBottom = Color(0xFFC5D1D9)
private val DetailsText = Color(0xFF202326)
private val DetailsMutedText = Color(0xFF8A9096)
private val DetailsFieldBorder = Color(0xFFDCE1E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiagnosisDetailsScreen(
    onClose: () -> Unit,
    onRegister: (DiagnosisDetailsInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val canRegister = name.trim().isNotEmpty()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DetailsTop, DetailsBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding(),
        ) {
            DetailsHeader(onClose = onClose)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Basic Info")

                LabeledField(
                    label = "Name",
                    value = name,
                    onValueChange = { name = it },
                )
                LabeledField(
                    label = "Date of Birth",
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it.filter { ch -> ch.isDigit() || ch == '/' } },
                    placeholder = "MM/DD/YYYY",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = "Select date",
                            tint = DetailsMutedText,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showDatePicker = true },
                                ),
                        )
                    },
                )
                LabeledField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                )
                LabeledField(
                    label = "Phone Number",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it.filter { ch -> ch.isDigit() || ch == '-' || ch == '+' } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )

                Spacer(Modifier.height(8.dp))
                SectionTitle("Company")

                Column {
                    FieldLabel("Company")
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FieldBox(modifier = Modifier.weight(1f)) {
                            FieldContent(
                                value = company,
                                onValueChange = { company = it },
                                placeholder = "Company name",
                            )
                        }
                        SearchButton(onClick = { /* TODO: company lookup */ })
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            RegisterButton(
                enabled = canRegister,
                onClick = {
                    onRegister(
                        DiagnosisDetailsInput(
                            name = name,
                            dateOfBirth = dateOfBirth,
                            email = email,
                            phoneNumber = phoneNumber,
                            company = company,
                        )
                    )
                },
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val localDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                dateOfBirth = localDate.format(dateOfBirthFormatter)
                            }
                            showDatePicker = false
                        },
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

private val dateOfBirthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

@Composable
private fun DetailsHeader(onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(72.dp)) {
        Text(
            text = "Add Diagnosis Details",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color.Black,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = DetailsText,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = DetailsText, fontSize = 15.sp)
}

@Composable
private fun FieldBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .55f))
            .border(1.dp, DetailsFieldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
private fun FieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, color = DetailsMutedText, fontSize = 16.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = DetailsText,
                    fontSize = 16.sp,
                ),
                singleLine = true,
                keyboardOptions = keyboardOptions,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(DetailsText),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            trailingIcon()
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        FieldBox {
            FieldContent(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                keyboardOptions = keyboardOptions,
                trailingIcon = trailingIcon,
            )
        }
    }
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD8DEE3))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Search", color = Color(0xFF6B7278), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RegisterButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(if (enabled) Color.Black else Color(0xFFC7CDD2))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Register",
            color = if (enabled) Color.White else Color.White.copy(alpha = .7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
