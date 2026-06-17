import { ImageResponse } from "next/og";

export const size        = { width: 32, height: 32 };
export const contentType = "image/png";

const BLUE = "#3B82F6";

export default function Icon() {
  return new ImageResponse(
    <div
      style={{
        width: 32, height: 32,
        background: "#05080a",
        borderRadius: 7,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        position: "relative",
      }}
    >
      <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
        <path d="M 4,17 A 12,12 0 0 1 28,17" stroke={BLUE} strokeWidth="2.6" strokeLinecap="round"/>
        <path d="M 7.5,18.5 A 8.5,8.5 0 0 1 24.5,18.5" stroke={BLUE} strokeWidth="2.6" strokeLinecap="round"/>
        <path d="M 11,20 A 5,5 0 0 1 21,20" stroke={BLUE} strokeWidth="2.6" strokeLinecap="round"/>
        <line x1="16" y1="22" x2="23" y2="15" stroke={BLUE} strokeWidth="2.6" strokeLinecap="round"/>
        <circle cx="16" cy="22" r="2.2" fill={BLUE}/>
      </svg>
    </div>,
    { ...size },
  );
}
