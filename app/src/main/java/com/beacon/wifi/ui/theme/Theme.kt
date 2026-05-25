package com.beacon.wifi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/** The 4 accent palettes exposed in the Beacon simulation panel. */
enum class BeaconAccent(val color: Color) {
    Blue(AccentBlue),
    Cyan(AccentCyan),
    Neutral(AccentNeutral),
    Magenta(AccentMagenta)
}

/** Extended, Beacon-specific color tokens not covered by Material3's scheme. */
data class BeaconColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val stroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val signalExcellent: Color = SignalExcellent,
    val signalGood: Color = SignalGood,
    val signalFair: Color = SignalFair,
    val signalWeak: Color = SignalWeak,
    val signalDead: Color = SignalDead,
)

/** Alias used by some screens. */
typealias BeaconThemeColors = BeaconColors

val LocalBeaconColors = staticCompositionLocalOf {
    BeaconColors(
        isDark = true,
        bg = BgDark, surface = SurfaceDark, surfaceElevated = SurfaceElevatedDark,
        stroke = StrokeDark, textPrimary = TextPrimaryDark, textSecondary = TextSecondaryDark,
        textTertiary = TextTertiaryDark, accent = AccentBlue, accentSecondary = AccentCyan
    )
}

/** Convenience accessor: `BeaconTheme.colors`. */
object BeaconTheme {
    val colors: BeaconColors
        @Composable get() = LocalBeaconColors.current
}

private val BeaconType = Typography().run {
    val ls = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(lineHeightStyle = ls),
        bodyMedium = bodyMedium.copy(lineHeightStyle = ls),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp)
    )
}

/** Monospace family for technical readouts (dBm, channels, MHz). */
val MonoFamily = FontFamily.Monospace

@Composable
fun BeaconTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: BeaconAccent = BeaconAccent.Blue,
    content: @Composable () -> Unit
) {
    val beaconColors = if (darkTheme) {
        BeaconColors(
            isDark = true,
            bg = BgDark, surface = SurfaceDark, surfaceElevated = SurfaceElevatedDark,
            stroke = StrokeDark, textPrimary = TextPrimaryDark, textSecondary = TextSecondaryDark,
            textTertiary = TextTertiaryDark,
            accent = accent.color,
            accentSecondary = if (accent == BeaconAccent.Cyan) AccentBlue else AccentCyan
        )
    } else {
        BeaconColors(
            isDark = false,
            bg = BgLight, surface = SurfaceLight, surfaceElevated = SurfaceElevatedLight,
            stroke = StrokeLight, textPrimary = TextPrimaryLight, textSecondary = TextSecondaryLight,
            textTertiary = TextTertiaryLight,
            accent = accent.color,
            accentSecondary = if (accent == BeaconAccent.Cyan) AccentBlue else AccentCyan
        )
    }

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = accent.color,
            onPrimary = Color.White,
            background = beaconColors.bg,
            surface = beaconColors.surface,
            onBackground = beaconColors.textPrimary,
            onSurface = beaconColors.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = accent.color,
            background = beaconColors.bg,
            surface = beaconColors.surface,
            onBackground = beaconColors.textPrimary,
            onSurface = beaconColors.textPrimary,
        )
    }

    CompositionLocalProvider(LocalBeaconColors provides beaconColors) {
        MaterialTheme(colorScheme = scheme, typography = BeaconType, content = content)
    }
}
