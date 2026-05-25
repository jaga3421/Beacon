package com.beacon.wifi.data

import androidx.compose.ui.graphics.Color
import com.beacon.wifi.ui.theme.SignalDead
import com.beacon.wifi.ui.theme.SignalExcellent
import com.beacon.wifi.ui.theme.SignalFair
import com.beacon.wifi.ui.theme.SignalGood
import com.beacon.wifi.ui.theme.SignalWeak

enum class SignalQuality(val label: String, val color: Color) {
    EXCELLENT("Excellent", SignalExcellent),
    GOOD("Good", SignalGood),
    FAIR("Fair", SignalFair),
    WEAK("Weak", SignalWeak),
    DEAD("Dead zone", SignalDead);

    companion object {
        fun fromRssi(rssi: Int): SignalQuality = when {
            rssi >= -50 -> EXCELLENT
            rssi >= -60 -> GOOD
            rssi >= -70 -> FAIR
            rssi >= -80 -> WEAK
            else -> DEAD
        }
    }
}

enum class Band(val label: String) { GHZ_24("2.4 GHz"), GHZ_5("5 GHz"), GHZ_6("6 GHz"), UNKNOWN("—") }

enum class Security(val label: String, val isOpen: Boolean) {
    OPEN("Open", true),
    WEP("WEP", false),
    WPA("WPA", false),
    WPA2("WPA2", false),
    WPA3("WPA3", false),
    WPA2_WPA3("WPA2/WPA3", false),
    ENTERPRISE("WPA2-Enterprise", false),
    UNKNOWN("—", false);

    companion object {
        fun fromCapabilities(caps: String): Security {
            val c = caps.uppercase()
            return when {
                c.contains("EAP") -> ENTERPRISE
                c.contains("WPA3") && c.contains("WPA2") -> WPA2_WPA3
                c.contains("SAE") || c.contains("WPA3") -> WPA3
                c.contains("RSN") || c.contains("WPA2") -> WPA2
                c.contains("WPA") -> WPA
                c.contains("WEP") -> WEP
                else -> OPEN
            }
        }
    }
}

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,                 // dBm
    val frequencyMhz: Int,
    val channel: Int,
    val channelWidthMhz: Int,
    val security: Security,
    val isCurrent: Boolean = false,
    val linkSpeedMbps: Int? = null,
    val vendor: String? = null,
) {
    val band: Band get() = bandFor(frequencyMhz)
    val quality: SignalQuality get() = SignalQuality.fromRssi(rssi)
    /** 0..100 score derived from RSSI. */
    val score: Int get() = scoreForRssi(rssi)
}

fun bandFor(freq: Int): Band = when {
    freq in 2400..2500 -> Band.GHZ_24
    freq in 4900..5900 -> Band.GHZ_5
    freq in 5925..7125 -> Band.GHZ_6
    else -> Band.UNKNOWN
}

fun channelFor(freq: Int): Int = when {
    freq == 2484 -> 14
    freq in 2412..2472 -> (freq - 2407) / 5
    freq in 5180..5885 -> (freq - 5000) / 5
    freq in 5955..7115 -> (freq - 5950) / 5
    else -> 0
}

/** Maps -30 dBm → 100, -90 dBm → 0, clamped. */
fun scoreForRssi(rssi: Int): Int {
    val s = ((rssi + 90) * 100f / 60f)
    return s.coerceIn(0f, 100f).toInt()
}

enum class HealthLevel(val label: String, val color: Color) {
    EXCELLENT("excellent", SignalExcellent),
    GOOD("good", SignalGood),
    FAIR("fair", SignalFair),
    POOR("poor", SignalWeak),
    CRITICAL("critical", SignalDead);

    companion object {
        fun fromScore(score: Int): HealthLevel = when {
            score >= 80 -> EXCELLENT
            score >= 65 -> GOOD
            score >= 50 -> FAIR
            score >= 30 -> POOR
            else -> CRITICAL
        }
    }
}

enum class RecSeverity { HEALTHY, INFO, WARNING, CRITICAL }

enum class ActionType {
    TRIGGER_SCAN,       // re-run a scan
    OPEN_WIFI_SETTINGS, // Intent to Settings.ACTION_WIFI_SETTINGS
    REQUEST_PERMISSION, // re-trigger location permission
    OPEN_ROUTER,        // open http://192.168.1.1 in browser
    NONE,
}

/** A plain-English diagnostic, the product's core differentiator. */
data class Recommendation(
    val title: String,
    val body: String,
    val severity: RecSeverity,
    val confidence: Int,            // 0..100
    val impact: String,             // e.g. "+18% signal"
    val actionLabel: String? = null,
    val actionType: ActionType = ActionType.NONE,
)

/** Edge / empty states the UI must handle gracefully. */
enum class WifiState { OK, NO_WIFI, PERMISSION_DENIED, AIRPLANE_MODE, NO_INTERNET, SCAN_FAILED, UNSUPPORTED }

data class ConnectedInfo(
    val ssid: String,
    val rssi: Int,
    val frequencyMhz: Int,
    val channel: Int,
    val linkSpeedMbps: Int,
    val band: Band,
) {
    val score: Int get() = scoreForRssi(rssi)
    val quality: SignalQuality get() = SignalQuality.fromRssi(rssi)
}
