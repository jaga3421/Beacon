package com.beacon.wifi.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.Pill
import com.beacon.wifi.ui.theme.BeaconTheme

@Composable
fun HeatmapScreen(vm: BeaconViewModel, modifier: Modifier = Modifier) {
    val c = BeaconTheme.colors
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .alpha(alpha.value)
            .scale(scale.value),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Map,
            contentDescription = null,
            tint = c.textTertiary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Heatmap",
            color = c.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Walk-through signal mapping is coming in a future update. It'll let you scan every corner of your home and build a live coverage map.",
            color = c.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.height(20.dp))
        Pill(
            text = "Coming soon",
            fg = c.accent,
            bg = c.accent.copy(alpha = 0.15f),
        )
    }
}
