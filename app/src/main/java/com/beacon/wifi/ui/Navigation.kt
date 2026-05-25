package com.beacon.wifi.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val HEATMAP = "heatmap"
    const val TIPS = "tips"
    const val ADVANCED = "advanced"
}

enum class BeaconTab(val route: String, val label: String, val icon: ImageVector) {
    Dashboard(Routes.DASHBOARD, "Home",     Icons.Outlined.GridView),
    Scanner(Routes.SCANNER,    "Scanner",   Icons.Outlined.Radar),
    Heatmap(Routes.HEATMAP,    "Heatmap",   Icons.Outlined.Map),
    Tips(Routes.TIPS,          "Tips",      Icons.Outlined.Lightbulb);

    companion object {
        val routes = entries.map { it.route }
    }
}
