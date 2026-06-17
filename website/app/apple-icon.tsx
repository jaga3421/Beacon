import { ImageResponse } from "next/og";

export const size        = { width: 180, height: 180 };
export const contentType = "image/png";

const BLUE = "#3B82F6";

export default function AppleIcon() {
  return new ImageResponse(
    <div
      style={{
        width: 180, height: 180,
        background: "#05080a",
        borderRadius: 40,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <svg width="140" height="140" viewBox="0 0 108 108" fill="none">
        <path d="M 18,55 A 40,40 0 0 1 90,55" stroke={BLUE} strokeWidth="6.5" strokeLinecap="round"/>
        <path d="M 29,60 A 28,28 0 0 1 79,60" stroke={BLUE} strokeWidth="6.5" strokeLinecap="round"/>
        <path d="M 40,65 A 16,16 0 0 1 68,65" stroke={BLUE} strokeWidth="6.5" strokeLinecap="round"/>
        <line x1="54" y1="72" x2="78" y2="48" stroke={BLUE} strokeWidth="6.5" strokeLinecap="round"/>
        <circle cx="54" cy="72" r="5.5" fill={BLUE}/>
      </svg>
    </div>,
    { ...size },
  );
}
