import type { Metadata } from "next";
import { Space_Grotesk, IBM_Plex_Sans, IBM_Plex_Mono } from "next/font/google";
import "./globals.css";

const spaceGrotesk = Space_Grotesk({ subsets: ["latin"], variable: "--font-space", display: "swap" });
const ibmPlexSans  = IBM_Plex_Sans ({ subsets: ["latin"], weight: ["400","500","600"], variable: "--font-sans",  display: "swap" });
const ibmPlexMono  = IBM_Plex_Mono ({ subsets: ["latin"], weight: ["400","500"],       variable: "--font-mono",  display: "swap" });

const SITE_URL  = "https://beacon-wifi.app";
const SITE_NAME = "Beacon WiFi Analyzer";
const TITLE     = "Beacon — Free WiFi Analyzer for Android. No Ads, No Paywall.";
const DESC      = "Beacon scans nearby WiFi networks, shows live signal strength, finds dead zones, and gives personalised channel fixes — completely free, forever. No ads, no subscription, no account. Open source.";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),

  title: {
    default: TITLE,
    template: `%s | ${SITE_NAME}`,
  },
  description: DESC,

  keywords: [
    "wifi analyzer android",
    "free wifi analyzer no ads",
    "wifi scanner app",
    "wifi signal strength meter",
    "channel congestion analyzer",
    "find wifi dead zones",
    "wifi heatmap android",
    "beacon wifi app",
    "best wifi spot finder",
    "wifi analyser free india",
    "wifi channel scanner",
    "no paywall wifi tool",
    "open source wifi app",
  ],

  authors:  [{ name: "Jagadeesh", url: "https://jagadee.sh" }],
  creator:  "Jagadeesh",
  publisher: SITE_NAME,

  /* ── icons ────────────────────────────────────────────────────────────── */
  icons: {
    icon: [
      { url: "/favicon.svg", type: "image/svg+xml" },
      { url: "/icon.png",    type: "image/png", sizes: "32x32" },
    ],
    apple:    [{ url: "/apple-icon.png", type: "image/png", sizes: "180x180" }],
    shortcut: "/favicon.svg",
  },

  /* ── Open Graph ───────────────────────────────────────────────────────── */
  openGraph: {
    type:        "website",
    locale:      "en_IN",
    url:         "/",
    siteName:    SITE_NAME,
    title:       "Beacon — Free WiFi Analyzer. No Ads, No Paywall.",
    description: "See your WiFi clearly. Signal strength, dead zones, crowded channels, and smart fixes — completely free. Open source Android app.",
    // OG image is auto-discovered from app/opengraph-image.tsx
  },

  /* ── Twitter / X card ─────────────────────────────────────────────────── */
  twitter: {
    card:        "summary_large_image",
    title:       "Beacon — Free WiFi Analyzer. No Ads, No Paywall.",
    description: "See your WiFi clearly. Find dead zones, crowded channels, and the best spots — completely free. Open source for Android.",
    creator:     "@jagadeesh_jkp",
    // Twitter image is auto-discovered from app/twitter-image.tsx (falls back to OG)
  },

  /* ── crawlers ─────────────────────────────────────────────────────────── */
  robots: {
    index:     true,
    follow:    true,
    googleBot: { index: true, follow: true, "max-image-preview": "large" },
  },

  /* ── canonical ────────────────────────────────────────────────────────── */
  alternates: {
    canonical: "/",
  },

  /* ── theme ────────────────────────────────────────────────────────────── */
  other: {
    "theme-color":           "#3B82F6",
    "color-scheme":          "dark",
    "msapplication-TileColor": "#05080a",
    "format-detection":      "telephone=no",
  },
};

const jsonLd = {
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "MobileApplication",
      "@id": `${SITE_URL}/#app`,
      name: SITE_NAME,
      description: "A free Android app that scans WiFi networks, shows signal strength, detects dead zones, and gives personalised channel recommendations. No ads, no subscription.",
      operatingSystem: "Android 7.0+",
      applicationCategory: "UtilitiesApplication",
      offers: { "@type": "Offer", price: "0", priceCurrency: "USD", availability: "https://schema.org/InStock" },
      downloadUrl: "https://github.com/jaga3421/Beacon/releases/latest",
      url: SITE_URL,
      author: { "@type": "Person", "@id": `${SITE_URL}/#author` },
      isAccessibleForFree: true,
      isFamilyFriendly: true,
    },
    {
      "@type": "Person",
      "@id": `${SITE_URL}/#author`,
      name: "Jagadeesh",
      url: "https://jagadee.sh",
    },
    {
      "@type": "WebSite",
      "@id": `${SITE_URL}/#website`,
      url: SITE_URL,
      name: SITE_NAME,
      description: DESC,
      publisher: { "@id": `${SITE_URL}/#author` },
    },
  ],
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${spaceGrotesk.variable} ${ibmPlexSans.variable} ${ibmPlexMono.variable}`}>
      <head>
        <meta name="theme-color" content="#3B82F6" />
        <meta name="color-scheme" content="dark" />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      </head>
      <body style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',system-ui,sans-serif)" }}>
        {children}
      </body>
    </html>
  );
}
