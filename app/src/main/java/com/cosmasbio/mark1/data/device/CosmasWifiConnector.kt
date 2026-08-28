package com.cosmasbio.mark1.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import java.net.Socket

class CosmasWifiConnector(
    context: Context,
) {
    private val connectivityManager =
        context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var cosmasNetwork: Network? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    fun connect(
        ssid: String = "COSMAS",
        password: String? = null,
        onConnected: (Network) -> Unit,
        onUnavailable: () -> Unit,
        onLost: () -> Unit = {},
    ) {
        disconnect()

        val specifierBuilder = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)

        if (!password.isNullOrBlank()) {
            specifierBuilder.setWpa2Passphrase(password)
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            .setNetworkSpecifier(specifierBuilder.build())
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                cosmasNetwork = network
                onConnected(network)
            }

            override fun onUnavailable() {
                cosmasNetwork = null
                onUnavailable()
            }

            override fun onLost(network: Network) {
                if (cosmasNetwork == network) {
                    cosmasNetwork = null
                }

                onLost()
            }
        }

        networkCallback = callback
        connectivityManager.requestNetwork(request, callback)
    }

    /**
     * 앱이 요청한 세션과 무관하게, 기기가 시스템 레벨에서 이미 해당 SSID의
     * Wi-Fi에 붙어 있는지 확인한다. (위치 권한이 없으면 SSID가 가려져 false를 반환할 수 있다.)
     */
    fun isConnectedToSsid(ssid: String): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return false

        val wifiInfo = capabilities.transportInfo as? WifiInfo ?: return false
        val currentSsid = wifiInfo.ssid?.trim('"')

        return currentSsid != null && currentSsid.equals(ssid, ignoreCase = true)
    }

    fun createDeviceSocket(
        host: String = "192.168.0.1",
        port: Int = 9191,
    ): Socket {
        val network = requireNotNull(cosmasNetwork) {
            "COSMAS Wi-Fi가 연결되지 않았습니다."
        }

        return network.socketFactory.createSocket(host, port)
    }

    fun disconnect() {
        networkCallback?.let { callback ->
            runCatching {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }

        networkCallback = null
        cosmasNetwork = null
    }
}