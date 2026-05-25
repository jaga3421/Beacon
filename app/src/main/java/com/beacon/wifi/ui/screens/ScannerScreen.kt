package com.beacon.wifi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.LocalSimpleMode
import com.beacon.wifi.data.Band
import com.beacon.wifi.data.WifiNetwork
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.ChannelCurve
import com.beacon.wifi.ui.components.ChannelOverlapChart
import com.beacon.wifi.ui.components.GlassCard
import com.beacon.wifi.ui.components.Mono
import com.beacon.wifi.ui.components.Pill
import com.beacon.wifi.ui.components.SectionHeader
import com.beacon.wifi.ui.components.SignalBars
import com.beacon.wifi.ui.components.rssiColor
import com.beacon.wifi.ui.components.scoreColor
import com.beacon.wifi.ui.theme.BeaconTheme
import com.beacon.wifi.ui.theme.BeaconThemeColors

private val FILTERS = listOf("All", "Secure", "Open", "Crowded")
private val SORTS   = listOf("strength", "name", "channel")

@Composable
fun ScannerScreen(vm: BeaconViewModel, modifier: Modifier = Modifier) {
    val ui       = vm.ui
    val colors   = BeaconTheme.colors
    val simpleMode = LocalSimpleMode.current

    // Filter / sort state
    var filter      by remember { mutableStateOf("All") }
    var sort        by remember { mutableStateOf("strength") }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val channelCounts = remember(ui.networks) {
        ui.networks.groupingBy { it.channel }.eachCount()
    }
    val filtered = remember(ui.networks, filter) {
        when (filter) {
            "Secure"  -> ui.networks.filter { !it.security.isOpen }
            "Open"    -> ui.networks.filter { it.security.isOpen }
            "Crowded" -> ui.networks.filter { (channelCounts[it.channel] ?: 0) >= 2 }
            else      -> ui.networks
        }
    }
    val sorted = remember(filtered, sort) {
        when (sort) {
            "name"    -> filtered.sortedBy { it.ssid }
            "channel" -> filtered.sortedBy { it.channel }
            else      -> filtered.sortedByDescending { it.rssi }
        }
    }
    // Deduplicate by BSSID before keying the list (checklist #14)
    val deduped = remember(sorted) {
        sorted.distinctBy { it.bssid.ifEmpty { it.ssid + it.channel } }
    }
    val curves = remember(ui.networks) {
        ui.networks.map { net ->
            ChannelCurve(
                channel   = net.channel,
                widthMhz  = net.channelWidthMhz,
                strength  = (net.score / 100f).coerceIn(0.1f, 1f),
                color     = rssiColor(net.rssi),
                emphasized = net.isCurrent,
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header ──────────────────────────────────────────────────────
        item("header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scanner",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    if (simpleMode) {
                        Text(
                            text = if (ui.scanning) "Checking nearby networks…"
                                   else "${ui.networks.size} networks found",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                        )
                    } else {
                        Mono(
                            text = "${ui.networks.size} networks · " +
                                   "${ui.connected?.band?.label ?: "scanning"} · " +
                                   if (ui.scanning) "live" else "tap ▶ to refresh",
                            color = colors.textSecondary,
                            size = 12,
                        )
                    }
                }
                IconButton(onClick = { vm.triggerScan() }) {
                    Icon(
                        imageVector = if (ui.scanning) Icons.Outlined.Radar else Icons.Outlined.Refresh,
                        contentDescription = if (ui.scanning) "Scanning" else "Start scan",
                        tint = colors.accent,
                    )
                }
            }
        }

        // ── Channel map card ─────────────────────────────────────────────
        item("channel-chart") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = if (simpleMode) "Channel Map" else "Channel Overlap",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary,
                            )
                            if (simpleMode) {
                                Text(
                                    text = "Which channels are crowded",
                                    color = colors.textTertiary,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        Pill(
                            text = if (ui.scanning) "SCANNING" else "LIVE",
                            fg = colors.accent,
                            bg = colors.accent.copy(alpha = 0.15f),
                        )
                    }

                    if (!simpleMode) {
                        val bandLabel   = ui.connected?.band?.label ?: "—"
                        val channelList = ui.networks.map { it.channel }.distinct().sorted()
                            .joinToString(", ").ifEmpty { "—" }
                        Mono(
                            text = "$bandLabel · channels $channelList",
                            color = colors.textTertiary,
                            size = 11,
                        )
                    }

                    if (ui.networks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (ui.scanning) "Scanning…" else "No data — tap scan to start",
                                color = colors.textTertiary,
                                fontSize = 13.sp,
                            )
                        }
                    } else {
                        ChannelOverlapChart(
                            points    = curves,
                            axisColor = colors.stroke,
                            modifier  = Modifier.fillMaxWidth().height(140.dp),
                        )
                        // Channel axis labels (technical mode only)
                        if (!simpleMode) {
                            val band = ui.connected?.band ?: Band.GHZ_5
                            val axisLabels = when (band) {
                                Band.GHZ_24 -> listOf("1", "6", "11")
                                Band.GHZ_5  -> listOf("36", "48", "64", "100", "132", "153")
                                Band.GHZ_6  -> listOf("1", "33", "65", "97", "129", "161")
                                Band.UNKNOWN -> listOf("1", "6", "11")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                axisLabels.forEach { lbl ->
                                    Mono(text = "ch $lbl", color = colors.textTertiary, size = 10)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Networks header + filter controls ────────────────────────────
        item("networks-controls") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (simpleMode) "Networks nearby" else "Networks in range",
                    modifier = Modifier.fillMaxWidth(),
                    trailing = {
                        if (deduped.isNotEmpty()) {
                            Text(
                                text = "${deduped.size} of ${ui.networks.size}",
                                color = colors.textTertiary,
                                fontSize = 12.sp,
                            )
                        }
                    },
                )
                // Filter chips + sort button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FILTERS.forEach { f ->
                        FilterChip(
                            label    = f,
                            selected = filter == f,
                            accent   = colors.accent,
                            colors   = colors,
                            onClick  = { filter = f },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.accent.copy(alpha = 0.12f))
                                .clickable { sortMenuOpen = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Mono(text = sort, color = colors.accent, size = 12, weight = FontWeight.SemiBold)
                        }
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                        ) {
                            SORTS.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = s,
                                            color = if (s == sort) colors.accent else colors.textPrimary,
                                        )
                                    },
                                    onClick = { sort = s; sortMenuOpen = false },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Network list ────────────────────────────────────────────────
        if (deduped.isEmpty()) {
            item("empty") {
                EmptyNetworksState(scanning = ui.scanning, colors = colors, onScan = { vm.triggerScan() })
            }
        } else {
            items(deduped, key = { it.bssid.ifEmpty { it.ssid + it.channel } }) { network ->
                NetworkCard(
                    network    = network,
                    simpleMode = simpleMode,
                    colors     = colors,
                )
            }
        }
    }
}

// ── Network card (expandable) ────────────────────────────────────────────────

@Composable
private fun NetworkCard(
    network: WifiNetwork,
    simpleMode: Boolean,
    colors: BeaconThemeColors,
) {
    var expanded by remember(network.bssid) { mutableStateOf(false) }
    val qualityLabel = network.quality.label
    val signalColor  = rssiColor(network.rssi)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            // ── Collapsed row ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SignalBars(rssi = network.rssi, modifier = Modifier.size(22.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = network.ssid.ifEmpty { "<Hidden>" },
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        if (!network.security.isOpen) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                        if (simpleMode) {
                            Text(
                                text = "${network.band.label} · ${if (network.security.isOpen) "Open" else "Secured"}",
                                color = colors.textTertiary,
                                fontSize = 11.sp,
                            )
                        } else {
                            Mono(
                                text = "ch ${network.channel} · ${network.band.label} · ${network.security.label}",
                                color = colors.textTertiary,
                                size = 11,
                            )
                        }
                    }
                }

                // Right: quality label (simple) or raw dBm (technical)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (simpleMode) {
                        Text(
                            text = qualityLabel,
                            color = signalColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Mono(
                            text = "${network.rssi}",
                            color = signalColor,
                            size = 15,
                            weight = FontWeight.Bold,
                        )
                        Text(text = "dBm", color = colors.textTertiary, fontSize = 10.sp)
                        Mono(text = "${network.channelWidthMhz} MHz", color = colors.textTertiary, size = 10)
                    }
                }
            }

            // ── Expanded details ───────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit  = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HorizontalDivider(color = colors.stroke, thickness = 0.5.dp)
                    Spacer(Modifier.height(2.dp))
                    if (simpleMode) {
                        // Plain-English expanded details
                        DetailRow("Signal",    qualityLabel,                colors)
                        DetailRow("Frequency", network.band.label,          colors)
                        DetailRow("Security",  network.security.label,      colors)
                        DetailRow("Channel",   "ch ${network.channel}",     colors)
                    } else {
                        // Full technical details
                        DetailRow("BSSID",     network.bssid,               colors, mono = true)
                        DetailRow("Vendor",    network.vendor ?: "Unknown", colors)
                        DetailRow("Frequency", "${network.frequencyMhz} MHz", colors)
                        DetailRow("Channel",   "ch ${network.channel} · ${network.channelWidthMhz} MHz wide", colors)
                        DetailRow("Security",  network.security.label,      colors)
                    }
                    // Signal score bar (always shown, labelled differently by mode)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (simpleMode) "Quality" else "Score",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.width(64.dp),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.stroke),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (network.score / 100f).coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(scoreColor(network.score)),
                            )
                        }
                        if (!simpleMode) {
                            Mono(
                                text = "${network.score}",
                                color = scoreColor(network.score),
                                size = 11,
                                weight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, colors: BeaconThemeColors, mono: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(64.dp),
        )
        if (mono) {
            Mono(text = value, color = colors.textPrimary, size = 12)
        } else {
            Text(text = value, color = colors.textPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    colors: BeaconThemeColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.18f) else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) accent else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun EmptyNetworksState(
    scanning: Boolean,
    colors: BeaconThemeColors,
    onScan: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Radar,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = if (scanning) "Scanning for networks…" else "No networks found",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
            )
            Text(
                text = if (scanning) "Discovering nearby WiFi networks"
                       else "Make sure WiFi is on and you're in range of a network.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            if (!scanning) {
                Button(
                    onClick = onScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor   = colors.bg,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = "Scan now", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
