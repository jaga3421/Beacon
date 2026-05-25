package com.beacon.wifi.data

import kotlin.math.abs

/**
 * Translates raw WiFi telemetry into ranked, human-readable advice.
 * "RSSI: -81 dBm" → "Your signal is weak — try moving closer to the router."
 */
object RecommendationEngine {

    fun overallScore(connected: ConnectedInfo?, networks: List<WifiNetwork>): Int {
        connected ?: return 0
        val signal = connected.score
        val congestion = congestionPenalty(connected, networks)
        return (signal - congestion).coerceIn(0, 100)
    }

    /** Number of nearby networks sharing the connected channel (interference). */
    fun coChannelCount(connected: ConnectedInfo?, networks: List<WifiNetwork>): Int {
        connected ?: return 0
        return networks.count { !it.isCurrent && abs(it.channel - connected.channel) <= overlapDistance(connected.band) }
    }

    private fun overlapDistance(band: Band) = if (band == Band.GHZ_24) 4 else 0

    private fun congestionPenalty(connected: ConnectedInfo, networks: List<WifiNetwork>): Int {
        val co = coChannelCount(connected, networks)
        return (co * 2).coerceAtMost(25)
    }

    /** Suggests the least-congested channel for the connected band. */
    fun suggestChannel(connected: ConnectedInfo?, networks: List<WifiNetwork>): Int? {
        connected ?: return null
        val candidates = if (connected.band == Band.GHZ_24) listOf(1, 6, 11)
        else listOf(36, 40, 44, 48, 149, 153, 157, 161)
        val load = candidates.associateWith { ch ->
            networks.count { abs(it.channel - ch) <= overlapDistance(connected.band) }
        }
        val best = load.minByOrNull { it.value }?.key ?: return null
        return if (best != connected.channel) best else null
    }

    fun build(
        connected: ConnectedInfo?,
        networks: List<WifiNetwork>,
        state: WifiState
    ): List<Recommendation> {
        if (state != WifiState.OK || connected == null) {
            return listOf(edgeStateRec(state))
        }

        val recs = mutableListOf<Recommendation>()
        val co = coChannelCount(connected, networks)
        val suggested = suggestChannel(connected, networks)

        // Signal strength
        when (connected.quality) {
            SignalQuality.DEAD, SignalQuality.WEAK -> recs += Recommendation(
                title = "Your WiFi signal is weak here",
                body = "At ${connected.rssi} dBm the router is far or blocked by walls. Moving closer or raising the router can dramatically improve speed.",
                severity = RecSeverity.CRITICAL,
                confidence = 88,
                impact = "+30% signal",
                actionLabel = "How to fix",
                actionType = ActionType.TRIGGER_SCAN,
            )
            SignalQuality.FAIR -> recs += Recommendation(
                title = "Signal could be stronger",
                body = "You're getting a usable but middling signal. A few meters closer to the router, or fewer walls in between, would help.",
                severity = RecSeverity.WARNING,
                confidence = 72,
                impact = "+15% signal",
                actionLabel = "How to fix",
                actionType = ActionType.TRIGGER_SCAN,
            )
            else -> {}
        }

        // Channel congestion
        if (co >= 4 && suggested != null) {
            recs += Recommendation(
                title = "Your channel is crowded",
                body = "$co nearby networks share your channel. Switching to channel $suggested may reduce interference and steady your connection.",
                severity = RecSeverity.WARNING,
                confidence = 80,
                impact = "+20% stability",
                actionLabel = "Open router settings",
                actionType = ActionType.OPEN_ROUTER,
            )
        } else if (co in 1..3 && suggested != null) {
            recs += Recommendation(
                title = "Mild channel overlap",
                body = "A handful of networks touch your channel. Channel $suggested is clearer if you want a little more headroom.",
                severity = RecSeverity.INFO,
                confidence = 64,
                impact = "+8% stability",
                actionType = ActionType.NONE,
            )
        }

        // Band advice
        if (connected.band == Band.GHZ_24) {
            val has5 = networks.any { it.band == Band.GHZ_5 }
            recs += Recommendation(
                title = "You're on the slower 2.4 GHz band",
                body = if (has5)
                    "2.4 GHz reaches further but is slower and busier. If you're near the router, the 5 GHz network will be noticeably faster."
                else
                    "2.4 GHz reaches further but is slower and more crowded. A dual-band router would unlock faster speeds.",
                severity = RecSeverity.INFO,
                confidence = 70,
                impact = "Up to 3× faster",
                actionType = ActionType.NONE,
            )
        }

        if (recs.isEmpty()) {
            recs += Recommendation(
                title = "Your WiFi looks great here",
                body = "Strong ${connected.rssi} dBm signal on a clean channel. Your line is stable — nothing to fix right now.",
                severity = RecSeverity.HEALTHY,
                confidence = 95,
                impact = "Healthy",
                actionLabel = "Run a scan",
                actionType = ActionType.TRIGGER_SCAN,
            )
        }

        return recs.sortedBy { it.severity.ordinal }.reversed()
    }

    private fun edgeStateRec(state: WifiState): Recommendation = when (state) {
        WifiState.NO_WIFI -> Recommendation(
            title = "WiFi is turned off",
            body = "Turn on WiFi to let Beacon analyze your network and nearby signals.",
            severity = RecSeverity.WARNING,
            confidence = 100,
            impact = "—",
            actionLabel = "Open settings",
            actionType = ActionType.OPEN_WIFI_SETTINGS,
        )
        WifiState.PERMISSION_DENIED -> Recommendation(
            title = "Location access needed",
            body = "Android requires location permission to read WiFi scan results. Beacon never tracks or uploads your location.",
            severity = RecSeverity.WARNING,
            confidence = 100,
            impact = "—",
            actionLabel = "Grant access",
            actionType = ActionType.REQUEST_PERMISSION,
        )
        WifiState.AIRPLANE_MODE -> Recommendation(
            title = "Airplane mode is on",
            body = "Turn off airplane mode (or re-enable WiFi) so Beacon can scan.",
            severity = RecSeverity.WARNING,
            confidence = 100,
            impact = "—",
            actionLabel = "Open settings",
            actionType = ActionType.OPEN_WIFI_SETTINGS,
        )
        WifiState.NO_INTERNET -> Recommendation(
            title = "Connected, but no internet",
            body = "You're on WiFi but there's no internet. This usually points to an ISP outage or a router that needs a restart.",
            severity = RecSeverity.CRITICAL,
            confidence = 85,
            impact = "—",
            actionLabel = "Run diagnostics",
            actionType = ActionType.TRIGGER_SCAN,
        )
        WifiState.SCAN_FAILED -> Recommendation(
            title = "Scan didn't return results",
            body = "Android may be throttling scans. Pull to refresh in a moment.",
            severity = RecSeverity.INFO,
            confidence = 100,
            impact = "—",
            actionLabel = "Retry",
            actionType = ActionType.TRIGGER_SCAN,
        )
        WifiState.UNSUPPORTED -> Recommendation(
            title = "WiFi not available",
            body = "This device doesn't expose WiFi scanning to Beacon.",
            severity = RecSeverity.WARNING,
            confidence = 100,
            impact = "—",
            actionLabel = null,
            actionType = ActionType.NONE,
        )
        WifiState.OK -> Recommendation(
            title = "All good",
            body = "No issues detected.",
            severity = RecSeverity.HEALTHY,
            confidence = 100,
            impact = "Healthy",
            actionLabel = null,
            actionType = ActionType.NONE,
        )
    }
}
