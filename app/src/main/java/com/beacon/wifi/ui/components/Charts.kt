package com.beacon.wifi.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.beacon.wifi.ui.theme.BeaconTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Large circular signal gauge: a ~270° gradient arc that fills to [score],
 * with a center readout. The centerpiece of the dashboard.
 */
@Composable
fun SignalGauge(
    score: Int,
    arcColor: Color,
    arcColorEnd: Color,
    trackColor: Color,
    modifier: Modifier = Modifier.size(220.dp),
    center: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(900),
        label = "gauge"
    )
    val arcBrush = remember(arcColor, arcColorEnd) {
        Brush.sweepGradient(listOf(arcColor, arcColorEnd, arcColor))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sweep = 270f
            val start = 135f
            val stroke = Stroke(width = 22f, cap = StrokeCap.Round)
            val pad = 14f
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            val topLeft = Offset(pad, pad)
            // track
            drawArc(
                color = trackColor,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            // value arc with gradient
            drawArc(
                brush = arcBrush,
                startAngle = start,
                sweepAngle = sweep * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        center()
    }
}

/** Concentric radar rings that expand and fade outward — premium "alive" backdrop. */
@Composable
fun RadarPulse(
    color: Color,
    modifier: Modifier = Modifier.size(220.dp),
    enabled: Boolean = true,
    speed: Float = 1f,
    rings: Int = 3,
) {
    if (!enabled) {
        Canvas(modifier) { /* disabled — no draw */ }
        return
    }
    val transition = rememberInfiniteTransition(label = "radar")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((3200 / speed).toInt()),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    Canvas(modifier) {
        val maxR = min(size.width, size.height) / 2f
        for (i in 0 until rings) {
            val phase = (progress + i.toFloat() / rings) % 1f
            val r = maxR * phase
            val alpha = (1f - phase) * 0.5f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = r,
                center = center,
                style = Stroke(width = 2f)
            )
        }
        drawCircle(color = color.copy(alpha = 0.10f), radius = maxR * 0.9f, center = center, style = Stroke(width = 1f))
    }
}

/**
 * Flowing live waveform from a series of RSSI samples. Smoothed cubic curve
 * with a soft gradient fill beneath — "the air around me being visualized."
 */
@Composable
fun LiveWaveform(
    samples: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    minDbm: Int = -95,
    maxDbm: Int = -30,
) {
    val path = remember { Path() }
    val fill = remember { Path() }
    val fillBrush = remember(lineColor) {
        Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), Color.Transparent))
    }
    Canvas(modifier) {
        val data = if (samples.size < 2) listOf(samples.firstOrNull() ?: -60, samples.firstOrNull() ?: -60) else samples
        val n = data.size
        val w = size.width
        val h = size.height
        fun y(v: Int): Float {
            val t = ((v - minDbm).toFloat() / (maxDbm - minDbm)).coerceIn(0f, 1f)
            return h - t * h * 0.9f - h * 0.05f
        }
        val dx = w / (n - 1).coerceAtLeast(1)
        path.reset()
        fill.reset()
        for (i in data.indices) {
            val px = i * dx
            val py = y(data[i])
            if (i == 0) { path.moveTo(px, py); fill.moveTo(px, h); fill.lineTo(px, py) }
            else {
                val prevX = (i - 1) * dx
                val midX = (prevX + px) / 2f
                val prevY = y(data[i - 1])
                path.cubicTo(midX, prevY, midX, py, px, py)
                fill.cubicTo(midX, prevY, midX, py, px, py)
            }
        }
        fill.lineTo(w, h)
        fill.close()
        drawPath(path = fill, brush = fillBrush)
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

/** Four-bar WiFi strength glyph, colored by signal. */
@Composable
fun SignalBars(
    rssi: Int,
    modifier: Modifier = Modifier.size(22.dp),
) {
    val color = rssiColor(rssi)
    val active = when {
        rssi >= -55 -> 4
        rssi >= -67 -> 3
        rssi >= -78 -> 2
        else -> 1
    }
    Canvas(modifier) {
        val gap = size.width * 0.12f
        val bw = (size.width - gap * 3) / 4f
        for (i in 0 until 4) {
            val barH = size.height * (0.35f + 0.65f * (i / 3f))
            val x = i * (bw + gap)
            drawRoundRectCompat(
                color = if (i < active) color else color.copy(alpha = 0.22f),
                left = x, top = size.height - barH, width = bw, height = barH
            )
        }
    }
}

private fun DrawScope.drawRoundRectCompat(color: Color, left: Float, top: Float, width: Float, height: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 3f, width / 3f)
    )
}

/**
 * Channel-overlap chart: each network is a bell curve placed at its channel,
 * width scaled by channel width. The connected network is emphasized.
 */
@Composable
fun ChannelOverlapChart(
    points: List<ChannelCurve>,
    axisColor: Color,
    modifier: Modifier = Modifier,
    minChannel: Float = 1f,
    maxChannel: Float = 165f,
) {
    val sorted = remember(points) { points.sortedBy { it.emphasized } }
    val curvePath = remember { Path() }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun x(ch: Float) = ((ch - minChannel) / (maxChannel - minChannel)).coerceIn(0f, 1f) * w
        // baseline
        drawLine(axisColor, Offset(0f, h - 1), Offset(w, h - 1), strokeWidth = 1f)
        sorted.forEach { c ->
            val cx = x(c.channel.toFloat())
            val halfW = max(18f, (c.widthMhz / 20f) * 26f)
            val peak = h * (0.25f + 0.6f * c.strength)
            curvePath.reset()
            curvePath.moveTo(cx - halfW, h)
            curvePath.cubicTo(cx - halfW * 0.4f, h, cx - halfW * 0.4f, h - peak, cx, h - peak)
            curvePath.cubicTo(cx + halfW * 0.4f, h - peak, cx + halfW * 0.4f, h, cx + halfW, h)
            curvePath.close()
            val a = if (c.emphasized) 0.42f else 0.20f
            drawPath(curvePath, brush = Brush.verticalGradient(listOf(c.color.copy(alpha = a), Color.Transparent)))
            drawPath(curvePath, color = c.color.copy(alpha = if (c.emphasized) 0.95f else 0.55f), style = Stroke(width = if (c.emphasized) 2.5f else 1.2f))
        }
    }
}

data class ChannelCurve(
    val channel: Int,
    val widthMhz: Int,
    val strength: Float,   // 0..1
    val color: Color,
    val emphasized: Boolean = false,
)

/** Compact animated radar sweep line (rotating) for the scanner header. */
@Composable
fun RadarSweep(color: Color, modifier: Modifier, speed: Float = 1f) {
    val t = rememberInfiniteTransition(label = "sweep")
    val angle by t.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween((2600 / speed).toInt()), RepeatMode.Restart),
        label = "angle"
    )
    Canvas(modifier) {
        val r = min(size.width, size.height) / 2f
        val rad = Math.toRadians(angle.toDouble())
        val end = Offset(center.x + (r * cos(rad)).toFloat(), center.y + (r * sin(rad)).toFloat())
        drawCircle(color.copy(alpha = 0.15f), r, center, style = Stroke(1f))
        drawLine(color.copy(alpha = 0.6f), center, end, strokeWidth = 2f)
    }
}
