package com.beacon.wifi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.data.SignalQuality
import com.beacon.wifi.data.scoreForRssi
import com.beacon.wifi.ui.theme.BeaconTheme
import com.beacon.wifi.ui.theme.MonoFamily

/** Subtly elevated, rounded card with a hairline border — the base surface unit. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val c = BeaconTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.stroke),
    ) {
        Box(Modifier.padding(padding)) { content() }
    }
}

/** Small status/label chip. */
@Composable
fun Pill(
    text: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Monospace technical readout text (dBm, MHz, channels). */
@Composable
fun Mono(
    text: String,
    color: Color = BeaconTheme.colors.textSecondary,
    size: Int = 12,
    weight: FontWeight = FontWeight.Medium,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontFamily = MonoFamily,
        fontSize = size.sp,
        fontWeight = weight,
        modifier = modifier,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = BeaconTheme.colors
    Row(modifier) {
        Text(
            title,
            color = c.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

fun rssiColor(rssi: Int): Color = SignalQuality.fromRssi(rssi).color

fun scoreColor(score: Int): Color = when {
    score >= 80 -> SignalQuality.EXCELLENT.color
    score >= 65 -> SignalQuality.GOOD.color
    score >= 50 -> SignalQuality.FAIR.color
    score >= 30 -> SignalQuality.WEAK.color
    else -> SignalQuality.DEAD.color
}

fun qualityColorForRssi(rssi: Int): Color = scoreColor(scoreForRssi(rssi))
