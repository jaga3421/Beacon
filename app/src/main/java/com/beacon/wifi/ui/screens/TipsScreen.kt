package com.beacon.wifi.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.data.ActionType
import com.beacon.wifi.data.Recommendation
import com.beacon.wifi.data.RecSeverity
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.GlassCard
import com.beacon.wifi.ui.components.Pill
import com.beacon.wifi.ui.theme.BeaconTheme

@Composable
fun TipsScreen(
    vm: com.beacon.wifi.ui.BeaconViewModel,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {},
) {
    val c = BeaconTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val ui = vm.ui
    val recs = ui.recommendations
    val fixCount = recs.count { it.severity != com.beacon.wifi.data.RecSeverity.HEALTHY }
    val subtitle = when {
        fixCount == 0 -> "Your WiFi looks good · ranked by impact"
        fixCount == 1 -> "1 fix · ranked by impact"
        else -> "$fixCount fixes · ranked by impact"
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .animateContentSize(tween(300))
    ) {
        // — Header —
        Text(
            text = "Fixes for you",
            color = c.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = c.textSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(20.dp))

        // — Recommendation cards —
        recs.forEach { rec ->
            RecommendationCard(
                rec = rec,
                onAction = { handleAction(context, rec, onRequestPermission, { vm.triggerScan() }) },
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

private fun handleAction(
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
private fun RecommendationCard(
    rec: Recommendation,
    onAction: () -> Unit,
) {
    val c = BeaconTheme.colors
    val (stripColor, icon, pillLabel) = severityStyle(rec.severity)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(0.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Colored left strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripColor),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tinted icon circle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(stripColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = stripColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = rec.title,
                        color = c.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(8.dp))
                    Pill(
                        text = if (rec.severity == RecSeverity.HEALTHY) "Healthy" else pillLabel,
                        fg = stripColor,
                        bg = stripColor.copy(alpha = 0.15f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rec.body,
                    color = c.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = c.textTertiary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "${rec.confidence}% sure",
                            color = c.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = c.accent.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = rec.impact,
                            color = c.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
                if (rec.actionLabel != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${rec.actionLabel} ›",
                        color = c.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onAction),
                    )
                }
            }
        }
    }
}

private data class SeverityStyle(
    val color: Color,
    val icon: ImageVector,
    val label: String,
)

@Composable
private fun severityStyle(severity: RecSeverity): SeverityStyle {
    val c = BeaconTheme.colors
    return when (severity) {
        RecSeverity.HEALTHY -> SeverityStyle(c.signalExcellent, Icons.Outlined.CheckCircle, "Healthy")
        RecSeverity.INFO -> SeverityStyle(c.accent, Icons.Outlined.Info, "Info")
        RecSeverity.WARNING -> SeverityStyle(c.signalFair, Icons.Outlined.WarningAmber, "Fix")
        RecSeverity.CRITICAL -> SeverityStyle(c.signalDead, Icons.Outlined.ErrorOutline, "Critical")
    }
}
