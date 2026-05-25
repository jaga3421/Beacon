package com.beacon.wifi.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.beacon.wifi.data.ConnectedInfo
import com.beacon.wifi.data.HealthLevel
import com.beacon.wifi.data.Recommendation
import com.beacon.wifi.data.RecommendationEngine
import com.beacon.wifi.data.WifiNetwork
import com.beacon.wifi.data.WifiRepository
import com.beacon.wifi.data.WifiState
import com.beacon.wifi.ui.theme.BeaconAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ThemeMode { Device, Light, Dark }

data class BeaconUiState(
    val state: WifiState = WifiState.OK,
    val connected: ConnectedInfo? = null,
    val networks: List<WifiNetwork> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val score: Int = 0,
    val health: HealthLevel = HealthLevel.CRITICAL,
    val coChannel: Int = 0,
    val suggestedChannel: Int? = null,
    val rssiHistory: List<Int> = emptyList(),
    val scanning: Boolean = false,
    val permissionGranted: Boolean = false,
)

data class BeaconSettings(
    val themeMode: ThemeMode = ThemeMode.Device,
    val simpleMode: Boolean = false,
    val accent: BeaconAccent = BeaconAccent.Blue,
    val radarEffects: Boolean = true,
    val animationSpeed: Float = 1f,
)

class BeaconViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WifiRepository(app.applicationContext)

    var ui by mutableStateOf(BeaconUiState())
        private set

    var settings by mutableStateOf(BeaconSettings())
        private set

    private val history = ArrayDeque<Int>()

    // CHANGE 4 — Replace pollActive flag with a Job reference
    private var pollJob: Job? = null

    // CHANGE 7 — SharedPreferences persistence
    private val prefs = app.getSharedPreferences("beacon_prefs", android.content.Context.MODE_PRIVATE)

    // CHANGE 5 — Lifecycle-aware polling: pause when app is backgrounded
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            // Re-check permission every time the app comes to foreground —
            // catches revocations made from OS Settings while the app was backgrounded.
            val nowGranted = repo.hasLocationPermission()
            if (ui.permissionGranted != nowGranted) {
                ui = ui.copy(permissionGranted = nowGranted)
            }
            if (nowGranted) {
                startLivePolling()
            } else {
                pollJob?.cancel()
                pollJob = null
            }
        }
        override fun onStop(owner: LifecycleOwner) {
            pollJob?.cancel()
            pollJob = null
        }
    }

    init {
        // CHANGE 7 — Restore persisted settings
        settings = BeaconSettings(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs.getString("theme_mode", "") ?: "")
            }.getOrDefault(ThemeMode.Device),
            simpleMode = prefs.getBoolean("simple_mode", false),
            accent = runCatching {
                BeaconAccent.valueOf(prefs.getString("accent", "") ?: "")
            }.getOrDefault(BeaconAccent.Blue),
            radarEffects = prefs.getBoolean("radar_effects", true),
            animationSpeed = prefs.getFloat("anim_speed", 1f),
        )
    }

    fun onPermissionResult(granted: Boolean) {
        ui = ui.copy(permissionGranted = granted)
        if (granted) startLivePolling()
        recompute()
    }

    // CHANGE 7 — Persist each setting when it changes
    fun setAccent(a: BeaconAccent) {
        settings = settings.copy(accent = a)
        prefs.edit().putString("accent", a.name).apply()
    }
    fun setRadarEffects(on: Boolean) {
        settings = settings.copy(radarEffects = on)
        prefs.edit().putBoolean("radar_effects", on).apply()
    }
    fun setAnimationSpeed(s: Float) {
        settings = settings.copy(animationSpeed = s)
        prefs.edit().putFloat("anim_speed", s).apply()
    }
    fun setSimpleMode(simple: Boolean) {
        settings = settings.copy(simpleMode = simple)
        prefs.edit().putBoolean("simple_mode", simple).apply()
    }
    fun setThemeMode(mode: ThemeMode) {
        settings = settings.copy(themeMode = mode)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // CHANGE 7 — Onboarding persistence
    val onboardingCompleted: Boolean get() = prefs.getBoolean("onboarding_done", false)
    fun markOnboardingComplete() { prefs.edit().putBoolean("onboarding_done", true).apply() }

    // CHANGE 5 — Register lifecycle observer; initial refresh off main thread
    fun start() {
        repo.register { recompute() }
        ui = ui.copy(permissionGranted = repo.hasLocationPermission())
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        if (ui.permissionGranted) startLivePolling()
        viewModelScope.launch(Dispatchers.IO) {
            repo.refresh()
            withContext(Dispatchers.Main) { recompute() }
        }
    }

    // CHANGE 4 + CHANGE 5 — Cancel job and unregister lifecycle observer
    fun stop() {
        pollJob?.cancel()
        pollJob = null
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        repo.unregister()
    }

    fun triggerScan() {
        ui = ui.copy(scanning = true)
        repo.startScan()
        viewModelScope.launch {
            delay(2500)
            ui = ui.copy(scanning = false)
        }
    }

    // CHANGE 4 — Move refresh() off the main thread; guard with Job reference
    private fun startLivePolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                withContext(Dispatchers.IO) { repo.refresh() }
                recompute()
                delay(1500)
            }
        }
    }

    // CHANGE 6 — Equality guard: only write ui when data actually changed
    private fun recompute() {
        val connected = repo.connected.value
        val networks  = repo.networks.value
        val state     = repo.state.value

        connected?.let {
            history.addLast(it.rssi)
            while (history.size > 60) history.removeFirst()
        }

        val score = RecommendationEngine.overallScore(connected, networks)
        val newUi = ui.copy(
            state            = state,
            connected        = connected,
            networks         = networks,
            recommendations  = RecommendationEngine.build(connected, networks, state),
            score            = score,
            health           = HealthLevel.fromScore(score),
            coChannel        = RecommendationEngine.coChannelCount(connected, networks),
            suggestedChannel = RecommendationEngine.suggestChannel(connected, networks),
            rssiHistory      = history.toList(),
            permissionGranted = repo.hasLocationPermission(),
        )
        if (newUi != ui) ui = newUi
    }
}
