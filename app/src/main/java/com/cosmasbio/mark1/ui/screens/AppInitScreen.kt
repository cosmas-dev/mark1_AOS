package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmasbio.mark1.R
import com.cosmasbio.mark1.model.DeviceStatus

@Composable
fun AppInitScreen(
    loading: Boolean,
    deviceStatus: DeviceStatus,
    onReady: () -> Unit,
) {
    LaunchedEffect(loading) {
        if (!loading) onReady()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.app_init_loading_device_info), style = MaterialTheme.typography.titleMedium)
        Text(deviceStatus.lastMessage, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
