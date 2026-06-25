package com.beacon.wifi.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.beacon.wifi.LocalSimpleMode
import com.beacon.wifi.data.ActionType
import com.beacon.wifi.data.Recommendation
import com.beacon.wifi.data.RecSeverity
import com.beacon.wifi.data.WifiState
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.Routes
import com.beacon.wifi.ui.components.GlassCard
import com.beacon.wifi.ui.components.LiveWaveform
import com.beacon.wifi.ui.components.Mono
import com.beacon.wifi.ui.components.Pill
import com.beacon.wifi.ui.components.RadarPulse
import com.beacon.wifi.ui.components.SectionHeader
import com.beacon.wifi.ui.components.SignalGauge
import com.beacon.wifi.ui.components.scoreColor
import com.beacon.wifi.ui.theme.BeaconTheme

@Composable
fun DashboardScreen(
    vm: BeaconViewModel,
    onNavigate: (String) -> Unit,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val ui = vm.ui
    val c = BeaconTheme.colors
    val accent = vm.settings.accent.color
    val simpleMode = LocalSimpleMode.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1) Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Beacon",
                    color = c.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                val ssid = ui.connected?.ssid
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = c.textSecondary, fontSize = 13.sp)) {
                            append("connected to ")
                        }
                        withStyle(SpanStyle(color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)) {
                            append(ssid ?: "—")
                        }
                    },
                )
            }
            IconButton(onClick = { onNavigate("advanced") }) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "Advanced",
                    tint = c.textSecondary,
                )
            }
        }

        // 2) Gauge or edge state
        if (ui.state != WifiState.OK || ui.connected == null) {
            EdgeStateCard(ui = ui, accent = accent, onNavigate = onNavigate)
        } else {
            // Big centered SignalGauge
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (vm.settings.radarEffects) {
                    RadarPulse(
                        color = accent.copy(alpha = 0.25f),
                        modifier = Modifier.size(260.dp),
                        enabled = true,
                        speed = vm.settings.animationSpeed,
                    )
                }
                SignalGauge(
                    score = ui.score,
                    arcColor = accent,
                    arcColorEnd = c.accentSecondary,
                    trackColor = c.stroke,
                    modifier = Modifier.size(240.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (simpleMode) {
                            Text(
                                text = "Signal strength",
                                color = c.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        } else {
                            Mono(
                                text = "SIGNAL · ${ui.connected.rssi} dBm",
                                color = c.textSecondary,
                                size = 11,
                                weight = FontWeight.Medium,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${ui.score}",
                                color = c.textPrimary,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "/100",
                                color = c.textTertiary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            text = ui.connected.quality.label,
                            color = scoreColor(ui.score),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // 3) Row of 3 stat cards — equal height via IntrinsicSize so the
            //    CHANNEL card (which can lack a unit line) matches its siblings.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    label = "HEALTH",
                    value = "${ui.score}",
                    unit = "/100",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatCard(
                    label = "BAND",
                    value = ui.connected.band.label,
                    unit = if (simpleMode && ui.connected.band.label == "5 GHz") "faster" else if (simpleMode && ui.connected.band.label == "2.4 GHz") "longer range" else null,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatCard(
                    label = if (simpleMode) "CHANNEL" else "CHANNEL",
                    value = if (simpleMode) (if (ui.coChannel < 4) "Clear" else "Busy") else "${ui.connected.channel}",
                    unit = if (simpleMode) null else if (ui.coChannel < 4) "clear" else "busy",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            // 4) Live signal card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live signal",
                                color = c.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (simpleMode) "live" else "STABLE",
                                color = c.textTertiary,
                                fontSize = 10.sp,
                                letterSpacing = if (simpleMode) 0.sp else 0.8.sp,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (simpleMode) {
                            Text(
                                text = ui.connected.quality.label,
                                color = accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Mono(
                                text = "${ui.connected.rssi} dBm",
                                color = accent,
                                size = 14,
                                weight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LiveWaveform(
                        samples = ui.rssiHistory,
                        lineColor = accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                    )
                }
            }
        }

        // 5) Section header + recommendation — only when actually connected
        if (ui.connected != null) {
            SectionHeader(
                title = "Top fix for you",
                trailing = {
                    Text(
                        text = "All ${ui.recommendations.size} ›",
                        color = accent,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigate(Routes.TIPS) },
                    )
                },
            )

            // 6) First recommendation card
            ui.recommendations.firstOrNull()?.let { rec ->
                RecommendationCard(
                    rec = rec,
                    accent = accent,
                    onAction = { dashboardHandleAction(context, rec, onRequestPermission) { vm.triggerScan() } },
                )
            }
        }
    }
}

private fun dashboardHandleAction(
    context: android.content.Context,
    rec: Recommendation,
    onRequestPermission: () -> Unit,
    onTriggerScan: () -> Unit,
) {
    when (rec.actionType) {
        ActionType.TRIGGER_SCAN -> onTriggerScan()
        ActionType.OPEN_WIFI_SETTINGS -> {
            val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
            context.startActivity(intent)
        }
        ActionType.OPEN_ROUTER -> {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("http://192.168.1.1")
            )
            context.startActivity(intent)
        }
        ActionType.REQUEST_PERMISSION -> onRequestPermission()
        ActionType.NONE -> { /* no-op */ }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
) {
    val c = BeaconTheme.colors
    GlassCard(
        modifier = modifier,
        padding = PaddingValues(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            Mono(
                text = label,
                color = c.textTertiary,
                size = 10,
                weight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                color = c.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            if (unit != null) {
                Text(
                    text = unit,
                    color = c.textSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    rec: Recommendation,
    accent: Color,
    onAction: () -> Unit,
) {
    val c = BeaconTheme.colors
    val (stripColor, icon, pillLabel) = when (rec.severity) {
        RecSeverity.HEALTHY -> Triple(c.signalExcellent, Icons.Outlined.CheckCircle, "Healthy")
        RecSeverity.INFO -> Triple(accent, Icons.Outlined.Info, "Info")
        RecSeverity.WARNING -> Triple(c.signalFair, Icons.Outlined.WarningAmber, "Fix")
        RecSeverity.CRITICAL -> Triple(c.signalDead, Icons.Outlined.WarningAmber, "Critical")
    }
    val pillBg = stripColor.copy(alpha = 0.15f)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Colored left accent strip — fills card height via IntrinsicSize.Min
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripColor),
            )
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = stripColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = rec.title,
                        color = c.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Pill(text = pillLabel, fg = stripColor, bg = pillBg)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = rec.body,
                    color = c.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (rec.actionLabel != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${rec.actionLabel} ›",
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onAction() },
                    )
                }
            }
        }
    }
}

@Composable
private fun EdgeStateCard(
    ui: com.beacon.wifi.ui.BeaconUiState,
    accent: Color,
    onNavigate: (String) -> Unit,
) {
    val c = BeaconTheme.colors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(52.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Connect to WiFi first",
                color = c.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stateBody(ui.state),
                color = c.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
            )
            Spacer(Modifier.height(20.dp))
            // CTA
            androidx.compose.material3.Button(
                onClick = { onNavigate(Routes.SCANNER) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = c.bg,
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "View all networks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun stateTitle(state: WifiState): String = when (state) {
    WifiState.NO_WIFI -> "No WiFi connected"
    WifiState.PERMISSION_DENIED -> "Location permission needed"
    WifiState.AIRPLANE_MODE -> "Airplane mode is on"
    WifiState.NO_INTERNET -> "No internet access"
    WifiState.SCAN_FAILED -> "Scan failed"
    WifiState.UNSUPPORTED -> "WiFi unsupported"
    WifiState.OK -> "Not connected"
}

private fun stateBody(state: WifiState): String = when (state) {
    WifiState.NO_WIFI -> "Connect to a WiFi network to see signal analysis."
    WifiState.PERMISSION_DENIED -> "Grant location permission to scan nearby networks."
    WifiState.AIRPLANE_MODE -> "Turn off airplane mode to use WiFi."
    WifiState.NO_INTERNET -> "Connected to WiFi but no internet detected."
    WifiState.SCAN_FAILED -> "Unable to scan. Try again in a moment."
    WifiState.UNSUPPORTED -> "This device does not support WiFi scanning."
    WifiState.OK -> "No active connection detected."
}
