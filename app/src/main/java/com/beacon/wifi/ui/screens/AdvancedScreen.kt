package com.beacon.wifi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.GlassCard
import com.beacon.wifi.ui.components.Mono
import com.beacon.wifi.ui.components.Pill
import com.beacon.wifi.ui.components.SectionHeader
import com.beacon.wifi.ui.theme.BeaconTheme
import com.beacon.wifi.ui.theme.MonoFamily
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdvancedScreen(vm: com.beacon.wifi.ui.BeaconViewModel, onBack: () -> Unit = {}, modifier: Modifier = Modifier) {
    val c = BeaconTheme.colors
    val ui = vm.ui
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = c.textSecondary,
                )
            }
        }

        // 1 — Header
        Column {
            Text(
                "Advanced tools",
                color = c.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Power-user diagnostics — kept out of the way",
                color = c.textSecondary,
                fontSize = 13.sp
            )
        }

        // 2 — RF Info card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("RF info")
                Spacer(Modifier.height(2.dp))
                RfRow("SSID", ui.connected?.ssid ?: "—", c.textTertiary)
                RfRow("Signal", ui.connected?.rssi?.let { "$it dBm" } ?: "—", c.textTertiary)
                RfRow("Band", ui.connected?.band?.label ?: "—", c.textTertiary)
                RfRow("Channel", ui.connected?.channel?.toString() ?: "—", c.textTertiary)
                RfRow("Frequency", ui.connected?.frequencyMhz?.let { "$it MHz" } ?: "—", c.textTertiary)
                RfRow("Link speed", ui.connected?.linkSpeedMbps?.let { "$it Mbps" } ?: "—", c.textTertiary)
            }
        }

        // 3 — Ping card
        var pingHost by remember { mutableStateOf("8.8.8.8") }
        var pingMs by remember { mutableStateOf<Long?>(null) }
        var pinging by remember { mutableStateOf(false) }
        var pingError by remember { mutableStateOf<String?>(null) }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Ping")
                // CHANGE 3: cap input at 253 chars (DNS hostname limit)
                StyledTextField(
                    value = pingHost,
                    onValueChange = { if (it.length <= 253) pingHost = it },
                    placeholder = "Host",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // CHANGE 3: validate before launching
                            val host = pingHost.trim()
                            if (host.isBlank()) {
                                pingError = "Enter a hostname or IP"
                                return@Button
                            }
                            pinging = true
                            pingMs = null
                            pingError = null
                            scope.launch {
                                // CHANGE 5: measurePing now dispatches to IO internally
                                val result = measurePing(host)
                                pinging = false
                                if (result == null) {
                                    pingError = "unreachable"
                                } else {
                                    pingMs = result
                                }
                            }
                        },
                        enabled = !pinging,
                        colors = ButtonDefaults.buttonColors(containerColor = c.accent)
                    ) {
                        Text(if (pinging) "Running…" else "Run", color = c.bg)
                    }
                    when {
                        // CHANGE 2: replace !! with safe .let patterns
                        pingError != null -> pingError?.let { err -> Mono(err, color = c.signalDead) }
                        pingMs != null -> pingMs?.let { ms ->
                            val color = when {
                                ms < 50 -> c.signalExcellent
                                ms < 120 -> c.signalFair
                                else -> c.signalDead
                            }
                            Mono("$ms ms", color = color, size = 14, weight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 4 — DNS Lookup card
        var dnsHost by remember { mutableStateOf("google.com") }
        var dnsResult by remember { mutableStateOf<String?>(null) }
        var dnsError by remember { mutableStateOf<String?>(null) }
        var resolving by remember { mutableStateOf(false) }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("DNS lookup")
                // CHANGE 3: cap input at 253 chars
                StyledTextField(
                    value = dnsHost,
                    onValueChange = { if (it.length <= 253) dnsHost = it },
                    placeholder = "Domain",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // CHANGE 3: validate before launching
                            val domain = dnsHost.trim()
                            if (domain.isBlank()) {
                                dnsError = "Enter a domain or hostname"
                                return@Button
                            }
                            resolving = true
                            dnsResult = null
                            dnsError = null
                            scope.launch {
                                // CHANGE 5: resolveDns now dispatches to IO internally
                                dnsResult = resolveDns(domain)
                                resolving = false
                            }
                        },
                        enabled = !resolving,
                        colors = ButtonDefaults.buttonColors(containerColor = c.accent)
                    ) {
                        Text(if (resolving) "Resolving…" else "Resolve", color = c.bg)
                    }
                }
                // CHANGE 2: replace !! with safe ?.let pattern
                dnsError?.let { err -> Mono(err, color = c.signalDead, size = 12) }
                dnsResult?.let { result -> Mono(result, color = c.textSecondary, size = 12) }
            }
        }

        // 5 — Speed / Link card
        var rttMs by remember { mutableStateOf<Long?>(null) }
        var checkingRtt by remember { mutableStateOf(false) }
        var rttError by remember { mutableStateOf<String?>(null) }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Speed / Link")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Link speed", color = c.textTertiary, fontSize = 13.sp)
                    Mono(
                        ui.connected?.linkSpeedMbps?.let { "$it Mbps" } ?: "—",
                        color = c.textPrimary,
                        size = 20,
                        weight = FontWeight.Bold
                    )
                }
                Text("negotiated PHY rate", color = c.textTertiary, fontSize = 11.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            checkingRtt = true
                            rttMs = null
                            rttError = null
                            scope.launch {
                                // CHANGE 1+5: measureHttpRtt now dispatches to IO and uses try/finally
                                val result = measureHttpRtt()
                                checkingRtt = false
                                if (result == null) rttError = "failed" else rttMs = result
                            }
                        },
                        enabled = !checkingRtt,
                        colors = ButtonDefaults.buttonColors(containerColor = c.accentSecondary)
                    ) {
                        Text(if (checkingRtt) "Checking…" else "Quick check", color = c.bg)
                    }
                    when {
                        // CHANGE 2: replace !! with safe ?.let patterns
                        rttError != null -> rttError?.let { err -> Mono(err, color = c.signalDead) }
                        rttMs != null -> rttMs?.let { ms ->
                            Mono("Round-trip $ms ms", color = c.textSecondary, size = 12)
                        }
                    }
                }
            }
        }

        // 6 — Coming soon cards
        ComingSoonCard(
            icon = Icons.Outlined.NetworkCheck,
            title = "Port scanner",
            description = "Check open TCP ports on any host on your network."
        )
        ComingSoonCard(
            icon = Icons.Outlined.DevicesOther,
            title = "Device scanner",
            description = "Discover all devices currently active on your LAN."
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ---- Private helpers ----

@Composable
private fun RfRow(label: String, value: String, labelColor: androidx.compose.ui.graphics.Color) {
    val c = BeaconTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontSize = 13.sp)
        Mono(value, color = c.textPrimary, size = 13)
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val c = BeaconTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = c.textPrimary,
            fontFamily = MonoFamily,
            fontSize = 13.sp
        ),
        cursorBrush = SolidColor(c.accent),
        modifier = modifier,
        decorationBox = { inner ->
            Column {
                if (value.isEmpty()) {
                    Text(placeholder, color = c.textTertiary, fontSize = 13.sp, fontFamily = MonoFamily)
                }
                inner()
                Spacer(Modifier.height(4.dp))
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxWidth().height(1.dp)
                ) { drawRect(color = c.stroke) }
            }
        }
    )
}

@Composable
private fun ComingSoonCard(icon: ImageVector, title: String, description: String) {
    val c = BeaconTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = c.textTertiary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Pill("Coming soon", fg = c.textTertiary, bg = c.surfaceElevated)
        }
    }
}

// CHANGE 5: wrap in withContext(Dispatchers.IO) so callers need no outer withContext
private suspend fun measurePing(host: String): Long? = withContext(Dispatchers.IO) {
    try {
        val times = LongArray(3)
        for (i in 0..2) {
            val t0 = System.nanoTime()
            Socket().use { s -> s.connect(InetSocketAddress(host, 53), 1500) }
            times[i] = (System.nanoTime() - t0) / 1_000_000L
        }
        times.average().toLong()
    } catch (_: Exception) { null }
}

// CHANGE 5: wrap in withContext(Dispatchers.IO) so callers need no outer withContext
private suspend fun resolveDns(domain: String): String = withContext(Dispatchers.IO) {
    try {
        InetAddress.getAllByName(domain).joinToString("\n") { addr ->
            addr.hostAddress?.takeIf { it.isNotEmpty() }
                ?: addr.toString().substringAfter('/')
        }.ifBlank { "no results" }
    } catch (_: Exception) { "could not resolve" }
}

// CHANGE 1: suspend + withContext(Dispatchers.IO) + try/finally to prevent socket leak
private suspend fun measureHttpRtt(url: String = "https://www.google.com"): Long? =
    withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as? HttpURLConnection ?: return@withContext null
        try {
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "HEAD"
            val t0 = System.currentTimeMillis()
            conn.connect()
            System.currentTimeMillis() - t0
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
