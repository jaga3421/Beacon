import type { Metadata } from "next";
import { Space_Grotesk, IBM_Plex_Sans, IBM_Plex_Mono } from "next/font/google";
import "./globals.css";

const spaceGrotesk = Space_Grotesk({ subsets: ["latin"], variable: "--font-space", display: "swap" });
const ibmPlexSans  = IBM_Plex_Sans ({ subsets: ["latin"], weight: ["400","500","600"], variable: "--font-sans",  display: "swap" });
const ibmPlexMono  = IBM_Plex_Mono ({ subsets: ["latin"], weight: ["400","500"],       variable: "--font-mono",  display: "swap" });

const SITE_URL = "https://beacon-wifi.app";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: { default: "Beacon — Free WiFi Analyzer for Android. No Ads, No Paywall.", template: "%s | Beacon WiFi Analyzer" },
  description: "Beacon scans nearby WiFi networks, shows live signal strength, finds dead zones, and gives personalised fixes — completely free, with zero ads and no paid tier. Made by a frustrated traveller in Yercaud.",
  keywords: ["wifi analyzer android","free wifi analyzer no ads","wifi scanner app","wifi signal strength meter","channel congestion analyzer","find wifi dead zones","wifi heatmap android","beacon wifi app","best wifi spot finder","wifi analyser free india","wifi channel scanner","no ad wifi tool"],
  authors: [{ name: "Jagadeesh" }],
  creator: "Jagadeesh",
  openGraph: { type: "website", locale: "en_IN", url: "/", siteName: "Beacon WiFi Analyzer", title: "Beacon — Free WiFi Analyzer. No Ads, No Paywall.", description: "See your WiFi clearly. Find dead zones, crowded channels, and the best spots to sit — completely free.", images: [{ url: "/og.png", width: 1200, height: 630, alt: "Beacon WiFi Analyzer" }] },
  twitter: { card: "summary_large_image", title: "Beacon — Free WiFi Analyzer. No Ads, No Paywall.", description: "See your WiFi clearly. Find dead zones, crowded channels, and the best spots — completely free.", images: ["/og.png"] },
  robots: { index: true, follow: true, googleBot: { index: true, follow: true } },
  alternates: { canonical: "/" },
};

const jsonLd = {
  "@context": "https://schema.org", "@type": "MobileApplication",
  name: "Beacon WiFi Analyzer",
  description: "A free Android app that scans WiFi networks, shows signal strength, detects dead zones, and gives personalised recommendations. No ads, no paywall.",
  operatingSystem: "Android 7.0+", applicationCategory: "UtilitiesApplication",
  offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
  downloadUrl: "https://github.com/jaga3421/Beacon/releases/latest",
  author: { "@type": "Person", name: "Jagadeesh" },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${spaceGrotesk.variable} ${ibmPlexSans.variable} ${ibmPlexMono.variable}`}>
      <head>
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      </head>
      <body style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',system-ui,sans-serif)" }}>
        {children}
      </body>
    </html>
  );
}
