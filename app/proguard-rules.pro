# Beacon — ProGuard / R8 rules

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlin.Metadata { *; }

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
# Prevent R8 from stripping Compose compiler-generated classes
-keep class **ComposableSingletons** { *; }
-keepclassmembers,allowobfuscation class * {
    @androidx.compose.ui.tooling.preview.Preview *;
}

# ── AndroidX Lifecycle / ViewModel ───────────────────────────────────────────
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class androidx.lifecycle.** { *; }

# ── Navigation ────────────────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }

# ── Data classes / sealed classes used as state ───────────────────────────────
-keep class com.beacon.wifi.data.** { *; }
-keep class com.beacon.wifi.ui.BeaconUiState { *; }
-keep class com.beacon.wifi.ui.BeaconSettings { *; }
-keep class com.beacon.wifi.ui.ThemeMode { *; }
-keep enum com.beacon.wifi.ui.theme.BeaconAccent { *; }

# ── Suppress noisy notes ──────────────────────────────────────────────────────
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn androidx.compose.**
