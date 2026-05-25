package com.beacon.wifi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beacon.wifi.ui.BeaconTab
import com.beacon.wifi.ui.BeaconViewModel
import com.beacon.wifi.ui.Routes
import com.beacon.wifi.ui.ThemeMode
import com.beacon.wifi.ui.screens.AdvancedScreen
import com.beacon.wifi.ui.screens.DashboardScreen
import com.beacon.wifi.ui.screens.HeatmapScreen
import com.beacon.wifi.ui.screens.OnboardingScreen
import com.beacon.wifi.ui.screens.ScannerScreen
import com.beacon.wifi.ui.screens.SplashScreen
import com.beacon.wifi.ui.screens.TipsScreen
import com.beacon.wifi.ui.theme.BeaconTheme

/** CompositionLocal so any screen can read simple-mode without prop-drilling. */
val LocalSimpleMode = staticCompositionLocalOf { false }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: BeaconViewModel = viewModel()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (vm.settings.themeMode) {
                ThemeMode.Device -> systemDark
                ThemeMode.Light  -> false
                ThemeMode.Dark   -> true
            }
            BeaconTheme(darkTheme = isDark, accent = vm.settings.accent) {
                CompositionLocalProvider(LocalSimpleMode provides vm.settings.simpleMode) {
                    BeaconRoot(vm)
                }
            }
        }
    }
}

@Composable
private fun BeaconRoot(vm: BeaconViewModel) {
    val nav = rememberNavController()
    val context = LocalContext.current
    var showSettingsRationale by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        vm.start()
        onDispose { vm.stop() }
    }

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }.toTypedArray()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        vm.onPermissionResult(granted)
        if (!granted) {
            val activity = context as? Activity
            val canAskAgain = activity != null && permissions.any { perm ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
            }
            if (!canAskAgain) {
                showSettingsRationale = true
            }
        }
    }
    val requestPermission: () -> Unit = { launcher.launch(permissions) }

    if (showSettingsRationale) {
        AlertDialog(
            onDismissRequest = { showSettingsRationale = false },
            title = { Text("Permission required") },
            text = {
                Text("WiFi scanning needs Location permission. Please enable it in app settings.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsRationale = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsRationale = false }) {
                    Text("Not now")
                }
            },
            containerColor = BeaconTheme.colors.surface,
            titleContentColor = BeaconTheme.colors.textPrimary,
            textContentColor = BeaconTheme.colors.textSecondary,
        )
    }

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBars = currentRoute in BeaconTab.routes

    // If permission is revoked while the user is on a main tab, send them back to
    // splash so they go through the permission flow again.
    LaunchedEffect(vm.ui.permissionGranted) {
        if (!vm.ui.permissionGranted && currentRoute in BeaconTab.routes) {
            nav.navigate(Routes.SPLASH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = BeaconTheme.colors.bg,
        topBar = {
            if (showBars) {
                BeaconTopBar(vm = vm)
            }
        },
        bottomBar = { if (showBars) BeaconBottomBar(nav, currentRoute) }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .background(BeaconTheme.colors.bg)
                .padding(inner),
            enterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 10 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 10 } },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(280)) { it / 10 } },
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(vm = vm, onFinish = {
                    // Skip onboarding entirely if permission was already granted
                    val dest = if (vm.ui.permissionGranted) Routes.DASHBOARD else Routes.ONBOARDING
                    nav.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    vm = vm,
                    onRequestPermission = requestPermission,
                    onFinish = {
                        nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    }
                )
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    vm = vm,
                    onNavigate = { route -> nav.navigate(route) },
                    onRequestPermission = requestPermission,
                )
            }
            composable(Routes.SCANNER) { ScannerScreen(vm = vm) }
            composable(Routes.HEATMAP)  { HeatmapScreen(vm = vm) }
            composable(Routes.TIPS)     { TipsScreen(vm = vm) }
            composable(Routes.ADVANCED) {
                AdvancedScreen(vm = vm, onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun BeaconTopBar(vm: BeaconViewModel) {
    val c = BeaconTheme.colors
    val simpleMode = vm.settings.simpleMode
    val themeMode = vm.settings.themeMode

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars) // edge-to-edge status bar clearance
            .height(52.dp)
            .background(c.bg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Simple ↔ Technical segmented toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, c.stroke, RoundedCornerShape(999.dp))
                .height(34.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToggleChip(
                label = "Simple",
                selected = simpleMode,
                selectedBg = c.accent,
                selectedFg = c.bg,
                unselectedFg = c.textTertiary,
                onClick = { vm.setSimpleMode(true) },
            )
            ToggleChip(
                label = "Technical",
                selected = !simpleMode,
                selectedBg = c.accent,
                selectedFg = c.bg,
                unselectedFg = c.textTertiary,
                onClick = { vm.setSimpleMode(false) },
            )
        }

        Box(modifier = Modifier.weight(1f))

        // Theme cycle icon
        val (themeIcon, themeDesc) = when (themeMode) {
            ThemeMode.Device -> Pair(Icons.Outlined.SettingsBrightness, "Device theme")
            ThemeMode.Light  -> Pair(Icons.Outlined.BrightnessHigh, "Light theme")
            ThemeMode.Dark   -> Pair(Icons.Outlined.Brightness4, "Dark theme")
        }
        IconButton(
            onClick = {
                val next = when (themeMode) {
                    ThemeMode.Device -> ThemeMode.Light
                    ThemeMode.Light  -> ThemeMode.Dark
                    ThemeMode.Dark   -> ThemeMode.Device
                }
                vm.setThemeMode(next)
            }
        ) {
            Icon(
                imageVector = themeIcon,
                contentDescription = themeDesc,
                tint = c.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ToggleChip(
    label: String,
    selected: Boolean,
    selectedBg: Color,
    selectedFg: Color,
    unselectedFg: Color,
    onClick: () -> Unit,
) {
    val bg = if (selected) selectedBg else Color.Transparent
    val fg = if (selected) selectedFg else unselectedFg
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun BeaconBottomBar(
    nav: androidx.navigation.NavHostController,
    currentRoute: String?
) {
    val c = BeaconTheme.colors
    NavigationBar(containerColor = c.surface, contentColor = c.textSecondary) {
        BeaconTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        nav.navigate(tab.route) {
                            popUpTo(Routes.DASHBOARD) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.accent,
                    selectedTextColor = c.accent,
                    indicatorColor = c.accent.copy(alpha = 0.14f),
                    unselectedIconColor = c.textTertiary,
                    unselectedTextColor = c.textTertiary,
                )
            )
        }
    }
}
