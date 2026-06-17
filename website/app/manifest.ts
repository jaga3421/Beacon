import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Beacon WiFi Analyzer",
    short_name: "Beacon",
    description: "Free WiFi analyzer for Android. No ads, no paywall.",
    start_url: "/",
    display: "standalone",
    background_color: "#05080a",
    theme_color: "#3B82F6",
    icons: [
      { src: "/favicon.svg", sizes: "any", type: "image/svg+xml" },
    ],
  };
}
