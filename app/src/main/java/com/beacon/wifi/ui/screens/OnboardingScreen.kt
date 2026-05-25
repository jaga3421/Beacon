package com.beacon.wifi.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.components.GlassCard
import com.beacon.wifi.ui.components.RadarPulse
import com.beacon.wifi.ui.theme.BeaconTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val showRadar: Boolean = false,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.GraphicEq,
        title = "See your WiFi, finally.",
        body = "Beacon turns signal strength, dead zones and crowded channels into something you can actually see — no jargon.",
        showRadar = true,
    ),
    OnboardingPage(
        icon = Icons.Outlined.Lock,
        title = "Private by design.",
        body = "Everything runs on your device. Beacon never uploads your location or your network data. No ads, ever.",
        showRadar = false,
    ),
    OnboardingPage(
        icon = Icons.Outlined.LocationOn,
        title = "One quick permission.",
        body = "Android requires location access to read WiFi details. That's the only reason we ask — it's never used to track you.\n\nNearby network identifiers (BSSIDs) are read to analyze channel congestion. This data never leaves your device.",
        showRadar = false,
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    vm: BeaconViewModel,
    onRequestPermission: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BeaconTheme.colors
    val accent = vm.settings.accent.color
    val radarEffects = vm.settings.radarEffects
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage = pagerState.currentPage

    // Track whether the user already tapped "Enable" — used to show the denied error
    var permissionRequested by remember { mutableStateOf(false) }
    val permissionGranted = vm.ui.permissionGranted

    // Auto-navigate as soon as permission is granted
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            vm.markOnboardingComplete()
            onFinish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                PageContent(
                    page = pages[pageIndex],
                    accent = accent,
                    radarEffects = radarEffects,
                    textPrimary = colors.textPrimary,
                    textSecondary = colors.textSecondary,
                    surfaceElevated = colors.surfaceElevated,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(pages.size) { index ->
                        val dotColor by animateColorAsState(
                            targetValue = if (index == currentPage) accent else colors.textTertiary,
                            label = "dotColor_$index",
                        )
                        val dotSize = if (index == currentPage) 10.dp else 7.dp
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                    }
                }

                // Primary action button
                val isLastPage = currentPage == pages.size - 1
                val buttonLabel = if (!isLastPage) "Next" else "Enable & continue"
                Button(
                    onClick = {
                        if (!isLastPage) {
                            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                        } else {
                            permissionRequested = true
                            onRequestPermission()
                            // Navigation happens automatically via LaunchedEffect(permissionGranted)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                ) {
                    Text(
                        text = buttonLabel,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Show error only after user tapped "Enable" and permission is still denied
                if (isLastPage && permissionRequested && !permissionGranted) {
                    Text(
                        text = "Location permission is required to scan WiFi networks. " +
                               "Please tap \"Enable & continue\" and allow the permission.",
                        color = colors.signalFair,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp),
                    )
                } else {
                    // Reserve height so the button doesn't jump when the message appears
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    page: OnboardingPage,
    accent: androidx.compose.ui.graphics.Color,
    radarEffects: Boolean,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    surfaceElevated: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Illustration area
        GlassCard(
            modifier = Modifier.size(220.dp),
            padding = PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                if (page.showRadar) {
                    RadarPulse(
                        color = accent.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxSize(),
                        enabled = radarEffects,
                        speed = 0.8f,
                        rings = 3,
                    )
                }
                Icon(
                    imageVector = page.icon,
                    contentDescription = page.title,
                    modifier = Modifier.size(72.dp),
                    tint = accent,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )
    }
}
