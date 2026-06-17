import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Page Not Found",
  robots: { index: false },
};

export default function NotFound() {
  return (
    <div style={{
      minHeight: "100svh", background: "#05080a", color: "#e9f3f0",
      display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
      fontFamily: "'IBM Plex Sans', system-ui, sans-serif", padding: "40px 24px", textAlign: "center",
    }}>
      <svg width="60" height="60" viewBox="0 0 108 108" fill="none" aria-hidden="true" style={{ marginBottom: 28 }}>
        <path d="M 18,55 A 40,40 0 0 1 90,55" stroke="#3B82F6" strokeWidth="6.5" strokeLinecap="round" opacity="0.3"/>
        <path d="M 29,60 A 28,28 0 0 1 79,60" stroke="#3B82F6" strokeWidth="6.5" strokeLinecap="round" opacity="0.5"/>
        <path d="M 40,65 A 16,16 0 0 1 68,65" stroke="#3B82F6" strokeWidth="6.5" strokeLinecap="round" opacity="0.8"/>
        <line x1="54" y1="72" x2="78" y2="48" stroke="#3B82F6" strokeWidth="6.5" strokeLinecap="round"/>
        <circle cx="54" cy="72" r="5.5" fill="#3B82F6"/>
      </svg>
      <p style={{ fontFamily: "'IBM Plex Mono', monospace", fontSize: "12px", letterSpacing: "0.16em", textTransform: "uppercase", color: "#3B82F6", margin: "0 0 14px" }}>
        404
      </p>
      <h1 style={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 700, fontSize: "clamp(2rem,5vw,3rem)", letterSpacing: "-0.025em", margin: "0 0 12px" }}>
        No signal here.
      </h1>
      <p style={{ fontSize: "1.1rem", color: "#a6bab8", margin: "0 0 36px", maxWidth: "36ch", lineHeight: 1.6 }}>
        The page you're looking for doesn't exist. Let's get you back to a strong connection.
      </p>
      <Link href="/" style={{
        fontFamily: "'IBM Plex Mono', monospace", fontSize: "14px", fontWeight: 500, letterSpacing: "0.02em",
        color: "#03110d", background: "#3B82F6", textDecoration: "none",
        padding: "13px 24px", borderRadius: "10px", boxShadow: "0 0 28px rgba(59,130,246,0.3)",
      }}>
        Back to home
      </Link>
    </div>
  );
}
