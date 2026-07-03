package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.model.TestDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestInfoScreen(
    draft: TestDraft,
    onBack: () -> Unit,
    onDraftChange: (name: String?, type: String?, info: String?, delaySeconds: String?) -> Unit,
    onNext: () -> Unit,
) {
    val types = listOf("COVID-19", "Cortisol", "Drug", "Chronic")
    var expanded by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }

    val isNameBlank = draft.name.trim().isEmpty()
    val showNameError = nameTouched && isNameBlank

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("테스트 정보 입력") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = {
                    nameTouched = true
                    onDraftChange(it, null, null, null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("이름") },
                singleLine = true,
                isError = showNameError,
                supportingText = {
                    if (showNameError) {
                        Text("이름을 입력해주세요.")
                    }
                },
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = draft.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                onDraftChange(null, type, null, null)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = draft.info,
                onValueChange = { onDraftChange(null, null, it, null) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Memo") },
                minLines = 3,
            )

            OutlinedTextField(
                value = draft.delaySeconds,
                onValueChange = { onDraftChange(null, null, null, it.filter { ch -> ch.isDigit() }) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("촬영 지연 시간 (초)") },
                placeholder = { Text("0 = 즉시 촬영") },
                singleLine = true,
            )

            Button(
                onClick = {
                    nameTouched = true
                    if (!isNameBlank) {
                        onNext()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("촬영 시작 화면으로")
            }
        }
    }
}