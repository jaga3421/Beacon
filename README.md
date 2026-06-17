# Beacon — Free WiFi Analyzer for Android

**No ads. No paywall. No account. Open source.**

Beacon scans the WiFi networks around you, shows live signal strength, identifies congested channels, and gives you specific recommendations — all for free, forever.

---

## Features

- **Live signal scan** — RSSI, frequency, channel, link speed, and band, refreshed every 1.5 seconds
- **Network list** — every network visible from your location, not just your own
- **Channel analysis** — see which channels are congested and which to switch to
- **Smart recommendations** — specific actions, not generic tips
- **Dead zone heatmap** *(coming soon)* — walk your space and build a signal map in real time
- Zero ads · Zero paywall · No login required

---

## Download

**Sideload APK** (Android 7.0+, ~16 MB):

```
https://github.com/jaga3421/Beacon/releases/latest
```

Play Store listing coming soon.

---

## Screenshots

> App screenshots coming soon.

---

## Building from source

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17      |
| Android SDK | API 24+ (target 35) |
| Gradle | 8.x (via wrapper) |

### Build

```bash
# Debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install on device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Project structure

```
app/
  src/main/
    java/com/beacon/wifi/
      ui/          — Compose screens and components
      data/        — WiFi scanning, repository layer
      domain/      — Use-cases and models
    res/           — Icons, drawables, themes
website/           — Next.js marketing site (beacon-wifi.app)
```

---

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Android WiFi APIs** — `WifiManager`, `ScanResult`, `WifiInfo`
- **Coroutines + Flow** for reactive scanning
- **Hilt** for dependency injection
- **Room** for local scan history *(future)*
- Website: **Next.js 16**, **Tailwind CSS v4**, **TypeScript**

---

## Contributing

Issues and PRs are welcome. Please open an issue first to discuss major changes.

Branch protection rules are enforced on `main`:
- All changes must go through a pull request
- Commits must be signed (GPG or SSH)
- At least one approval required before merge

---

## License

MIT — see [LICENSE](LICENSE).

---

## Author

Built by [Jagadeesh](https://jagadee.sh) in Chennai, born out of frustration in [Yercaud](https://en.wikipedia.org/wiki/Yercaud).
