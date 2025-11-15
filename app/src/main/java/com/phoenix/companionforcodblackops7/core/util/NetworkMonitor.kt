package com.phoenix.companionforcodblackops7.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Checks if internet connectivity is currently available
     * Checks for both WiFi and Mobile Data
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    /**
     * Flow that emits connectivity status changes
     */
    val isConnectedFlow: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isConnected)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransport(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Send initial state
        trySend(isConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

/**
 * Composable that observes network connectivity state
 */
@Composable
fun rememberNetworkState(networkMonitor: NetworkMonitor): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = networkMonitor.isConnected()) {
        networkMonitor.isConnectedFlow.collect { isConnected ->
            value = isConnected
        }
    }
}
