import { ImageResponse } from "next/og";

export const size        = { width: 1200, height: 630 };
export const contentType = "image/png";
export const alt         = "Beacon: Free WiFi Analyzer for Android";

const BLUE = "#3B82F6";
const BG   = "#05080a";
const TEXT = "#e9f3f0";
const MUTE = "#a6bab8";

async function loadFont() {
  try {
    const css = await fetch(
      "https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@700&display=swap",
      { headers: { "User-Agent": "Mozilla/5.0 (compatible; Googlebot/2.1)" } },
    ).then((r) => r.text());
    const url = css.match(/url\(([^)]+)\)/)?.[1];
    if (!url) return null;
    const data = await fetch(url).then((r) => r.arrayBuffer());
    return { name: "Space Grotesk", data, style: "normal" as const, weight: 700 as const };
  } catch {
    return null;
  }
}

export default async function OgImage() {
  const font = await loadFont();
  const fonts = font ? [font] : [];

  /* ── SVG icon encoded as data-URI ──────────────────────────────────────── */
  const iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" fill="none">
    <path d="M 18,55 A 40,40 0 0 1 90,55" stroke="%233B82F6" stroke-width="7" stroke-linecap="round"/>
    <path d="M 29,60 A 28,28 0 0 1 79,60" stroke="%233B82F6" stroke-width="7" stroke-linecap="round"/>
    <path d="M 40,65 A 16,16 0 0 1 68,65" stroke="%233B82F6" stroke-width="7" stroke-linecap="round"/>
    <path d="M 54,72 L 78,48" stroke="%233B82F6" stroke-width="7" stroke-linecap="round"/>
    <circle cx="54" cy="72" r="6" fill="%233B82F6"/>
  </svg>`;
  const iconSrc = `data:image/svg+xml,${iconSvg}`;

  /* ── large decorative arcs (right panel) ───────────────────────────────── */
  const arcSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400" fill="none">
    <defs>
      <radialGradient id="g" cx="50%" cy="75%" r="65%">
        <stop offset="0%" stop-color="%233B82F6" stop-opacity="0.25"/>
        <stop offset="100%" stop-color="%233B82F6" stop-opacity="0"/>
      </radialGradient>
    </defs>
    <circle cx="200" cy="270" r="190" fill="url(%23g)"/>
    <path d="M 10,200 A 190,190 0 0 1 390,200" stroke="%233B82F6" stroke-width="3.5" stroke-linecap="round" opacity="0.55"/>
    <path d="M 50,220 A 150,150 0 0 1 350,220" stroke="%233B82F6" stroke-width="3.5" stroke-linecap="round" opacity="0.7"/>
    <path d="M 90,240 A 110,110 0 0 1 310,240" stroke="%233B82F6" stroke-width="3.5" stroke-linecap="round" opacity="0.85"/>
    <path d="M 130,260 A 70,70 0 0 1 270,260" stroke="%233B82F6" stroke-width="3.5" stroke-linecap="round" opacity="1"/>
    <path d="M 200,270 L 270,200" stroke="%233B82F6" stroke-width="4" stroke-linecap="round"/>
    <circle cx="200" cy="270" r="8" fill="%233B82F6"/>
    <circle cx="200" cy="270" r="22" fill="%233B82F6" opacity="0.15"/>
  </svg>`;
  const arcSrc = `data:image/svg+xml,${arcSvg}`;

  return new ImageResponse(
    <div
      style={{
        width: 1200, height: 630,
        background: BG,
        display: "flex",
        position: "relative",
        overflow: "hidden",
        fontFamily: font ? "'Space Grotesk'" : "system-ui, sans-serif",
      }}
    >
      {/* background glow, top-left */}
      <div style={{
        position: "absolute", top: -120, left: -80,
        width: 520, height: 520, borderRadius: "50%",
        background: `radial-gradient(circle, rgba(59,130,246,0.18) 0%, transparent 70%)`,
        display: "flex",
      }} />
      {/* background glow, bottom-right */}
      <div style={{
        position: "absolute", bottom: -180, right: 80,
        width: 600, height: 600, borderRadius: "50%",
        background: `radial-gradient(circle, rgba(34,211,238,0.08) 0%, transparent 65%)`,
        display: "flex",
      }} />

      {/* ── left content panel ─────────────────────────────────────────── */}
      <div style={{
        position: "absolute", left: 0, top: 0, bottom: 0, width: 680,
        display: "flex", flexDirection: "column",
        justifyContent: "center", padding: "0 72px",
      }}>
        {/* logo row */}
        <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 36 }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={iconSrc} width={52} height={52} alt="" />
          <span style={{
            fontSize: 26, fontWeight: 700, color: TEXT, letterSpacing: -0.5,
          }}>
            Beacon
          </span>
          <span style={{
            fontSize: 13, fontWeight: 400, color: MUTE,
            background: "rgba(59,130,246,0.12)", border: "1px solid rgba(59,130,246,0.3)",
            borderRadius: 100, padding: "4px 12px", marginLeft: 4,
            letterSpacing: 1.5, textTransform: "uppercase",
          }}>
            Free
          </span>
        </div>

        {/* headline */}
        <div style={{
          fontSize: 72, fontWeight: 700, lineHeight: 1,
          letterSpacing: -2.5, color: TEXT,
          display: "flex", flexDirection: "column",
        }}>
          <span>Your WiFi.</span>
          <span style={{ color: BLUE }}>Finally clear.</span>
        </div>

        {/* sub */}
        <div style={{
          marginTop: 28, fontSize: 22, lineHeight: 1.45,
          color: MUTE, maxWidth: 480,
          display: "flex",
        }}>
          Signal strength, channel congestion, and smart fixes in real time. Free, with no ads and no paywall.
        </div>

        {/* pills */}
        <div style={{ display: "flex", gap: 12, marginTop: 40 }}>
          {["Android 7.0+", "~16 MB", "Open Source"].map((t) => (
            <span
              key={t}
              style={{
                fontSize: 14, color: MUTE,
                border: "1px solid rgba(255,255,255,0.1)",
                borderRadius: 8, padding: "6px 14px",
                background: "rgba(255,255,255,0.04)",
                display: "flex",
              }}
            >
              {t}
            </span>
          ))}
        </div>

        {/* domain */}
        <div style={{
          marginTop: 52, fontSize: 16, color: "rgba(166,186,184,0.5)",
          letterSpacing: 1, display: "flex",
        }}>
          beacon-wifi.app
        </div>
      </div>

      {/* ── right decorative panel ─────────────────────────────────────── */}
      <div style={{
        position: "absolute", right: 0, top: 0, bottom: 0, width: 520,
        display: "flex", alignItems: "center", justifyContent: "center",
        borderLeft: "1px solid rgba(255,255,255,0.05)",
      }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src={arcSrc} width={380} height={380} alt="" />
      </div>

      {/* vertical divider glow */}
      <div style={{
        position: "absolute", left: 680, top: 0, bottom: 0, width: 1,
        background: "linear-gradient(to bottom, transparent, rgba(59,130,246,0.3) 40%, rgba(59,130,246,0.3) 60%, transparent)",
        display: "flex",
      }} />
    </div>,
    {
      ...size,
      fonts,
    },
  );
}
