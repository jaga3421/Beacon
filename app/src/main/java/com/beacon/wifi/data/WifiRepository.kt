package com.beacon.wifi.data

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads live WiFi data via [WifiManager]. The UI is responsible for requesting the
 * runtime location / NEARBY_WIFI_DEVICES permission; this class only reads.
 */
class WifiRepository(private val appContext: Context) {

    private val wifi = appContext.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _networks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val networks: StateFlow<List<WifiNetwork>> = _networks.asStateFlow()

    private val _connected = MutableStateFlow<ConnectedInfo?>(null)
    val connected: StateFlow<ConnectedInfo?> = _connected.asStateFlow()

    private val _state = MutableStateFlow(WifiState.OK)
    val state: StateFlow<WifiState> = _state.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    // CHANGE 2 — Fix permission check logic for API 33+
    fun hasLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.NEARBY_WIFI_DEVICES) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }

    private fun airplaneModeOn(): Boolean =
        Settings.Global.getInt(appContext.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

    // CHANGE 3 — Check EXTRA_RESULTS_UPDATED flag before calling refresh()
    fun register(onUpdate: () -> Unit) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val fresh = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
                if (fresh) {
                    refresh()
                    onUpdate()
                }
            }
        }
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        ContextCompat.registerReceiver(
            appContext, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregister() {
        receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        receiver = null
    }

    /** Triggers a scan. Returns false if it could not be started. */
    fun startScan(): Boolean {
        val mgr = wifi ?: run { _state.value = WifiState.UNSUPPORTED; return false }
        if (airplaneModeOn() && !mgr.isWifiEnabled) { _state.value = WifiState.AIRPLANE_MODE; return false }
        if (!mgr.isWifiEnabled) { _state.value = WifiState.NO_WIFI; return false }
        if (!hasLocationPermission()) { _state.value = WifiState.PERMISSION_DENIED; return false }
        @Suppress("DEPRECATION")
        val started = runCatching { mgr.startScan() }.getOrDefault(false)
        if (!started) {
            // Throttled — fall back to the last cached results rather than failing hard.
            refresh()
        }
        return started
    }

    /** Re-reads cached scan results + connection info into the flows. */
    fun refresh() {
        val mgr = wifi ?: run { _state.value = WifiState.UNSUPPORTED; return }
        if (!mgr.isWifiEnabled) {
            _state.value = if (airplaneModeOn()) WifiState.AIRPLANE_MODE else WifiState.NO_WIFI
            _networks.value = emptyList()
            _connected.value = null
            return
        }
        if (!hasLocationPermission()) {
            _state.value = WifiState.PERMISSION_DENIED
            return
        }

        val current = readConnected()
        _connected.value = current

        val results: List<ScanResult> = runCatching {
            @Suppress("MissingPermission")
            mgr.scanResults
        }.getOrDefault(emptyList())

        if (results.isEmpty() && current == null) {
            _state.value = WifiState.SCAN_FAILED
        } else {
            _state.value = WifiState.OK
        }

        @Suppress("DEPRECATION")
        val currentBssid = current?.let { mgr.connectionInfo?.bssid }
        _networks.value = results
            .filter { it.frequency > 0 }
            .map { it.toModel(currentBssid) }
            .sortedByDescending { it.rssi }
    }

    // CHANGE 1 — Fix Android 13+ WiFi connectionInfo API
    @Suppress("DEPRECATION")
    private fun readConnected(): ConnectedInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Try modern API first; fall back to legacy if it returns null
            // (handles cases where ConnectivityManager returns null but WifiManager still has data)
            readConnectedModern() ?: readConnectedLegacy()
        } else {
            readConnectedLegacy()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun readConnectedModern(): ConnectedInfo? {
        val cm = appContext.getSystemService(ConnectivityManager::class.java) ?: return null
        val network = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(network) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val wifiInfo = caps.transportInfo as? WifiInfo ?: return null

        val rssi = wifiInfo.rssi
        val freq = wifiInfo.frequency

        // Validate signal: not genuinely connected if RSSI/frequency look invalid
        if (rssi == 0 || rssi < -120 || freq <= 0) return null

        var ssid = wifiInfo.ssid?.removePrefix("\"")?.removeSuffix("\"")

        // SSID may be redacted to "<unknown ssid>" when neverForLocation flag is set on
        // NEARBY_WIFI_DEVICES, or when the device's location mode is off. Try fallbacks.
        if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") {
            // Fallback 1: legacy WifiManager.connectionInfo (deprecated but still works on most devices)
            ssid = runCatching {
                @Suppress("DEPRECATION")
                wifi?.connectionInfo?.ssid
                    ?.removePrefix("\"")?.removeSuffix("\"")
                    ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
            }.getOrNull()
        }

        if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") {
            // Fallback 2: look up SSID from cached scan results via BSSID
            val bssid = wifiInfo.bssid
            if (!bssid.isNullOrBlank() && bssid != "02:00:00:00:00:00") {
                ssid = runCatching {
                    @Suppress("MissingPermission")
                    wifi?.scanResults
                        ?.firstOrNull { it.BSSID == bssid }
                        ?.SSID
                        ?.removePrefix("\"")?.removeSuffix("\"")
                        ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
                }.getOrNull()
            }
        }

        if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") {
            // Fallback 3: RSSI is valid so device is connected — show placeholder rather than
            // falsely reporting "not connected". The legacy path may fill in the real SSID.
            ssid = if (rssi in -100..-10) "(connected)" else return null
        }

        val channel = channelFor(freq)
        return ConnectedInfo(
            ssid = ssid ?: return null,
            rssi = rssi,
            frequencyMhz = freq,
            channel = channel,
            linkSpeedMbps = wifiInfo.linkSpeed,
            band = bandFor(freq),
        )
    }

    @Suppress("DEPRECATION")
    private fun readConnectedLegacy(): ConnectedInfo? {
        val info = wifi?.connectionInfo ?: return null
        if (info.networkId == -1) return null
        val rssi = info.rssi
        val freq = info.frequency
        // Validate signal fields
        if (rssi == 0 || rssi < -120 || freq <= 0) return null
        val ssid = info.ssid?.removePrefix("\"")?.removeSuffix("\"")
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
            ?: if (rssi in -100..-10) "(connected)" else return null
        val channel = channelFor(freq)
        return ConnectedInfo(
            ssid = ssid,
            rssi = rssi,
            frequencyMhz = freq,
            channel = channel,
            linkSpeedMbps = info.linkSpeed,
            band = bandFor(freq),
        )
    }

    private fun ScanResult.toModel(currentBssid: String?): WifiNetwork {
        @Suppress("DEPRECATION")
        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiSsid?.toString()?.removeSurrounding("\"") ?: SSID
        } else SSID
        val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (channelWidth) {
                ScanResult.CHANNEL_WIDTH_20MHZ -> 20
                ScanResult.CHANNEL_WIDTH_40MHZ -> 40
                ScanResult.CHANNEL_WIDTH_80MHZ -> 80
                ScanResult.CHANNEL_WIDTH_160MHZ -> 160
                ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
                else -> 20
            }
        } else 20
        return WifiNetwork(
            ssid = name.ifBlank { "(hidden)" },
            bssid = BSSID ?: "",
            rssi = level,
            frequencyMhz = frequency,
            channel = channelFor(frequency),
            channelWidthMhz = width,
            security = Security.fromCapabilities(capabilities ?: ""),
            isCurrent = currentBssid != null && BSSID == currentBssid,
        )
    }
}
