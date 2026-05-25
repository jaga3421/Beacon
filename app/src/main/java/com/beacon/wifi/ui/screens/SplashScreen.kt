package com.beacon.wifi.ui.screens

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.Mono
import com.beacon.wifi.ui.components.RadarPulse
import com.beacon.wifi.ui.theme.BeaconTheme
import com.beacon.wifi.ui.theme.MonoFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    vm: BeaconViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BeaconTheme.colors
    val settings = vm.settings

    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "splash_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(durationMillis = 700),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        val delayMs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 600L else 2400L
        delay(delayMs)
        onFinish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Top bar: wordmark
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BEACON",
                fontFamily = MonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 6.sp,
                color = colors.textTertiary
            )
        }

        // Center content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radar + icon stack
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                RadarPulse(
                    color = settings.accent.color,
                    modifier = Modifier.fillMaxSize(),
                    enabled = settings.radarEffects,
                    speed = settings.animationSpeed,
                    rings = 3
                )

                // Soft glow layer
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            settings.accent.color.copy(alpha = 0.12f)
                        )
                )

                // Rounded-square icon container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceElevated)
                        .border(
                            width = 1.5.dp,
                            color = settings.accent.color,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Router,
                        contentDescription = "WiFi",
                        tint = settings.accent.color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Headline
            Text(
                text = "See your WiFi, finally.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Beacon visualizes signal quality in real time — so you can spot dead zones and crowded channels at a glance.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }

        // Bottom page indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(settings.accent.color)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.textTertiary)
            )
        }
    }
}
