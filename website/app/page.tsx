"use client";

import { useRef, useEffect, useState } from "react";
import versionData from "../version.json";

const VERSION    = versionData.version;
const APK_URL    = versionData.apkUrl;
const GITHUB_URL = "https://github.com/jaga3421/Beacon";

/* ─── logo icon ─────────────────────────────────────────────────────────────── */
function BeaconIcon({ size = 22 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 108 108" fill="none" aria-hidden="true">
      <path d="M 18,55 A 40,40 0 0 1 90,55" stroke="var(--accent)" strokeWidth="7" strokeLinecap="round"/>
      <path d="M 29,60 A 28,28 0 0 1 79,60" stroke="var(--accent)" strokeWidth="7" strokeLinecap="round"/>
      <path d="M 40,65 A 16,16 0 0 1 68,65" stroke="var(--accent)" strokeWidth="7" strokeLinecap="round"/>
      <line x1="54" y1="72" x2="78" y2="48" stroke="var(--accent)" strokeWidth="7" strokeLinecap="round"/>
      <circle cx="54" cy="72" r="5.5" fill="var(--accent)"/>
    </svg>
  );
}

/* ─── helpers ──────────────────────────────────────────────────────────────── */
type Device = "android" | "ios" | "desktop" | "init";
const ACCENT = { r: 59, g: 130, b: 246 }; // #3B82F6
const TWO    = Math.PI * 2;
const rgba   = (r: number, g: number, b: number, a: number) => `rgba(${r},${g},${b},${a})`;
const acc    = (a: number) => rgba(ACCENT.r, ACCENT.g, ACCENT.b, a);
const rand   = (a: number, b: number) => a + Math.random() * (b - a);
const mod    = (a: number) => ((a % TWO) + TWO) % TWO;

function setupCanvas(canvas: HTMLCanvasElement) {
  const ctx = canvas.getContext("2d")!;
  const DPR = Math.min(window.devicePixelRatio || 1, 2);
  const box = { W: 0, H: 0 };
  const resize = () => {
    const r  = canvas.getBoundingClientRect();
    box.W = r.width; box.H = r.height;
    canvas.width  = Math.round(box.W * DPR);
    canvas.height = Math.round(box.H * DPR);
    ctx.setTransform(DPR, 0, 0, DPR, 0, 0);
  };
  const ro = new ResizeObserver(resize);
  ro.observe(canvas); resize();
  return { ctx, box, ro };
}

/* ─── canvas: hero radar ────────────────────────────────────────────────────── */
function HeroCanvas() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const canvas = ref.current; if (!canvas) return;
    const ctx = canvas.getContext("2d")!;
    const DPR = Math.min(window.devicePixelRatio || 1, 2);
    let W = 0, H = 0, cx = 0, cy = 0, maxR = 0;
    let nodes: { ang:number; rad:number; x:number; y:number; size:number; base:number; lit:number; big:boolean; label:string }[] = [];
    let pings: { r:number }[] = [], sweep = 0, lastPing = -1e9, last = performance.now(), raf = 0;

    const buildNodes = () => {
      nodes = [];
      for (let i = 0; i < 20; i++) {
        const ang = rand(0, TWO), rad = rand(maxR * 0.16, maxR * 0.92);
        nodes.push({ ang, rad, x: cx + Math.cos(ang) * rad, y: cy + Math.sin(ang) * rad,
          size: rand(1.3, 3), base: rand(0.12, 0.32), lit: 0, big: Math.random() < 0.4,
          label: "-" + Math.round(rand(38, 86)) });
      }
    };

    const resize = () => {
      const r = canvas.getBoundingClientRect();
      W = r.width; H = r.height;
      canvas.width  = Math.round(W * DPR);
      canvas.height = Math.round(H * DPR);
      ctx.setTransform(DPR, 0, 0, DPR, 0, 0);
      cx = W * (W > 820 ? 0.66 : 0.5);
      cy = H * (W > 820 ? 0.46 : 0.40);
      maxR = Math.hypot(Math.max(cx, W - cx), Math.max(cy, H - cy)) * 1.02;
      buildNodes();
    };
    const ro = new ResizeObserver(resize); ro.observe(canvas); resize();
    const hasConic = typeof (ctx as any).createConicGradient === "function";

    const frame = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05); last = now;
      ctx.clearRect(0, 0, W, H);

      const bg = ctx.createRadialGradient(cx, cy, 0, cx, cy, maxR);
      bg.addColorStop(0, acc(0.06)); bg.addColorStop(0.5, acc(0.02)); bg.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = bg; ctx.fillRect(0, 0, W, H);

      ctx.lineWidth = 1;
      for (let i = 1; i <= 5; i++) { ctx.beginPath(); ctx.arc(cx, cy, maxR * i / 5.4, 0, TWO); ctx.strokeStyle = acc(0.05); ctx.stroke(); }

      if (now - lastPing > 1950) { pings.push({ r: maxR * 0.04 }); lastPing = now; }
      ctx.lineWidth = 1.5;
      pings = pings.filter(p => p.r < maxR);
      pings.forEach(p => { p.r += dt * maxR * 0.19; const a = Math.max(0, 1 - p.r / maxR); ctx.beginPath(); ctx.arc(cx, cy, p.r, 0, TWO); ctx.strokeStyle = acc(a * 0.5); ctx.stroke(); });

      sweep = mod(sweep + dt * 0.52);
      if (hasConic) {
        const g = (ctx as any).createConicGradient(sweep, cx, cy);
        g.addColorStop(0, acc(0)); g.addColorStop(0.74, acc(0)); g.addColorStop(0.92, acc(0.05)); g.addColorStop(0.995, acc(0.24)); g.addColorStop(1, acc(0));
        ctx.save(); ctx.beginPath(); ctx.arc(cx, cy, maxR, 0, TWO); ctx.clip(); ctx.fillStyle = g; ctx.fillRect(cx - maxR, cy - maxR, maxR * 2, maxR * 2); ctx.restore();
      }
      ctx.save(); ctx.translate(cx, cy); ctx.rotate(sweep); ctx.strokeStyle = acc(0.45); ctx.lineWidth = 1; ctx.beginPath(); ctx.moveTo(0, 0); ctx.lineTo(maxR, 0); ctx.stroke(); ctx.restore();

      ctx.font = "500 10px var(--font-mono,'IBM Plex Mono',monospace)";
      nodes.forEach(n => {
        const diff = mod(sweep - n.ang);
        if (diff < 0.16) n.lit = 1;
        n.lit *= 0.965;
        if (n.lit > 0.12) { ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(n.x, n.y); ctx.strokeStyle = acc(n.lit * 0.22); ctx.lineWidth = 1; ctx.stroke(); }
        const a = Math.min(1, n.base + n.lit * 0.8);
        ctx.beginPath(); ctx.arc(n.x, n.y, n.size + n.lit * 1.4, 0, TWO);
        ctx.fillStyle = acc(a); ctx.shadowColor = acc(n.lit * 0.8); ctx.shadowBlur = n.lit * 12; ctx.fill(); ctx.shadowBlur = 0;
        if (n.big && n.lit > 0.45) { ctx.fillStyle = acc(n.lit * 0.85); ctx.fillText(n.label, n.x + 7, n.y + 3); }
      });

      const pulse = 0.5 + 0.5 * Math.sin(now / 560);
      ctx.beginPath(); ctx.arc(cx, cy, 16 + pulse * 5, 0, TWO); ctx.fillStyle = acc(0.10); ctx.fill();
      ctx.beginPath(); ctx.arc(cx, cy, 4.5 + pulse * 1.6, 0, TWO);
      ctx.fillStyle = acc(0.95); ctx.shadowColor = acc(0.9); ctx.shadowBlur = 22; ctx.fill(); ctx.shadowBlur = 0;

      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, []);
  return <canvas ref={ref} style={{ position: "absolute", inset: 0, width: "100%", height: "100%", zIndex: 0 }} />;
}

/* ─── canvas: scan waveform ─────────────────────────────────────────────────── */
function ScanCanvas() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const canvas = ref.current; if (!canvas) return;
    const { ctx, box, ro } = setupCanvas(canvas);
    const N = 90, data = new Array(N).fill(0.5);
    let phase = 0, last = performance.now(), raf = 0;
    const frame = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05); last = now;
      const { W, H } = box;
      phase += dt * 0.8;
      data.shift();
      const v = 0.5 + 0.26 * Math.sin(phase) + 0.12 * Math.sin(phase * 2.7) + 0.06 * (Math.random() - 0.5);
      data.push(Math.max(0.08, Math.min(0.92, v)));
      ctx.clearRect(0, 0, W, H);
      ctx.strokeStyle = acc(0.06); ctx.lineWidth = 1;
      for (let i = 1; i < 4; i++) { const y = H * i / 4; ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(W, y); ctx.stroke(); }
      const pad = 14, gw = W - pad * 2;
      const xAt = (i: number) => pad + gw * i / (N - 1);
      const yAt = (val: number) => H - 16 - val * (H - 32);
      const grad = ctx.createLinearGradient(0, 0, 0, H);
      grad.addColorStop(0, acc(0.28)); grad.addColorStop(1, acc(0));
      ctx.beginPath(); ctx.moveTo(xAt(0), H);
      for (let i = 0; i < N; i++) ctx.lineTo(xAt(i), yAt(data[i]));
      ctx.lineTo(xAt(N - 1), H); ctx.closePath(); ctx.fillStyle = grad; ctx.fill();
      ctx.beginPath();
      for (let i = 0; i < N; i++) { const x = xAt(i), y = yAt(data[i]); i ? ctx.lineTo(x, y) : ctx.moveTo(x, y); }
      ctx.strokeStyle = acc(0.95); ctx.lineWidth = 2; ctx.shadowColor = acc(0.7); ctx.shadowBlur = 8; ctx.stroke(); ctx.shadowBlur = 0;
      const lx = xAt(N - 1), ly = yAt(data[N - 1]);
      ctx.beginPath(); ctx.arc(lx, ly, 3.5, 0, TWO); ctx.fillStyle = "#fff"; ctx.shadowColor = acc(1); ctx.shadowBlur = 12; ctx.fill(); ctx.shadowBlur = 0;
      const dbm = Math.round(-30 - (1 - data[N - 1]) * 60);
      ctx.font = "500 12px var(--font-mono,'IBM Plex Mono',monospace)"; ctx.fillStyle = acc(0.9); ctx.textAlign = "left";
      ctx.fillText(dbm + " dBm", pad, 20);
      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, []);
  return <canvas ref={ref} style={{ display: "block", width: "100%", height: "140px" }} />;
}

/* ─── canvas: heatmap ───────────────────────────────────────────────────────── */
function HeatCanvas() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const canvas = ref.current; if (!canvas) return;
    const { ctx, box, ro } = setupCanvas(canvas);
    const cols = 16, rows = 7, gap = 2;
    let raf = 0;
    const frame = (now: number) => {
      const t = now / 1000, { W, H } = box;
      ctx.clearRect(0, 0, W, H);
      const rx = (0.5 + 0.34 * Math.sin(t * 0.5)) * W;
      const ry = (0.5 + 0.34 * Math.cos(t * 0.37)) * H;
      const cw = (W - gap * (cols - 1)) / cols, ch = (H - gap * (rows - 1)) / rows;
      const maxD = Math.hypot(W, H) * 0.62;
      for (let i = 0; i < cols; i++) {
        for (let j = 0; j < rows; j++) {
          const x = i * (cw + gap), y = j * (ch + gap);
          const d = Math.hypot((x + cw / 2) - rx, (y + ch / 2) - ry);
          let v = 1 - d / maxD;
          v += 0.06 * Math.sin(t * 1.3 + i * 0.6 + j * 0.9);
          v = Math.max(0, Math.min(1, v));
          let r2, g2, b2;
          if (v > 0.5) { const k = (v - 0.5) * 2; r2 = Math.round(ACCENT.r * k); g2 = Math.round(ACCENT.g * (0.55 + 0.45 * k)); b2 = Math.round(ACCENT.b * (0.5 + 0.5 * k)); }
          else { const k = v * 2; r2 = Math.round(150 * (1 - k) + ACCENT.r * k * 0.3); g2 = Math.round(40 * (1 - k) + ACCENT.g * k * 0.4); b2 = Math.round(50 * (1 - k) + ACCENT.b * k * 0.4); }
          ctx.fillStyle = `rgba(${r2},${g2},${b2},${0.18 + v * 0.7})`; ctx.fillRect(x, y, cw, ch);
        }
      }
      ctx.beginPath(); ctx.arc(rx, ry, 4, 0, TWO);
      ctx.fillStyle = "#fff"; ctx.shadowColor = acc(1); ctx.shadowBlur = 14; ctx.fill(); ctx.shadowBlur = 0;
      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, []);
  return <canvas ref={ref} style={{ display: "block", width: "100%", height: "140px" }} />;
}

/* ─── canvas: channel recommendations ──────────────────────────────────────── */
function RecoCanvas() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const canvas = ref.current; if (!canvas) return;
    const { ctx, box, ro } = setupCanvas(canvas);
    const nets = [{ c:0.16,w:0.10 },{ c:0.24,w:0.12 },{ c:0.31,w:0.09 },{ c:0.40,w:0.11 },{ c:0.20,w:0.08 },{ c:0.50,w:0.10 }];
    const pick = 0.82, pickW = 0.085;
    let t = 0, last = performance.now(), raf = 0;
    const frame = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05); last = now; t += dt;
      const { W, H } = box;
      const base = H - 22, pad = 14, gw = W - pad * 2;
      ctx.clearRect(0, 0, W, H);
      ctx.strokeStyle = acc(0.10); ctx.lineWidth = 1; ctx.beginPath(); ctx.moveTo(pad, base); ctx.lineTo(W - pad, base); ctx.stroke();
      const bell = (cx: number, wid: number, amp: number, fill: string, line: string) => {
        ctx.beginPath();
        for (let x = 0; x <= gw; x += 4) { const xn = x / gw, y = base - amp * Math.exp(-Math.pow((xn - cx) / wid, 2)); const px = pad + x; x ? ctx.lineTo(px, y) : ctx.moveTo(px, y); }
        ctx.lineTo(W - pad, base); ctx.lineTo(pad, base); ctx.closePath(); ctx.fillStyle = fill; ctx.fill(); ctx.strokeStyle = line; ctx.lineWidth = 1.4; ctx.stroke();
      };
      nets.forEach((n, i) => { const amp = (H - 46) * (0.5 + 0.12 * Math.sin(t * 1.3 + i)); bell(n.c, n.w, amp, "rgba(230,120,70,0.10)", "rgba(230,140,90,0.32)"); });
      const pulse = 0.6 + 0.4 * Math.sin(t * 2.4), amp = (H - 46) * 0.72;
      bell(pick, pickW, amp, acc(0.16 + 0.10 * pulse), acc(0.55 + 0.35 * pulse));
      const mx = pad + gw * pick;
      ctx.strokeStyle = acc(0.5); ctx.setLineDash([3, 3]); ctx.lineWidth = 1; ctx.beginPath(); ctx.moveTo(mx, base); ctx.lineTo(mx, base - amp - 4); ctx.stroke(); ctx.setLineDash([]);
      ctx.beginPath(); ctx.arc(mx, base - amp - 8, 3.2 + pulse * 1.2, 0, TWO); ctx.fillStyle = "#fff"; ctx.shadowColor = acc(1); ctx.shadowBlur = 12; ctx.fill(); ctx.shadowBlur = 0;
      ctx.font = "600 12px var(--font-mono,'IBM Plex Mono',monospace)"; ctx.fillStyle = acc(0.95); ctx.textAlign = "right"; ctx.fillText("USE CH 11", W - pad, 20);
      ctx.font = "500 11px var(--font-mono,'IBM Plex Mono',monospace)"; ctx.fillStyle = "rgba(230,140,90,0.7)"; ctx.textAlign = "left"; ctx.fillText("crowded", pad, 20);
      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, []);
  return <canvas ref={ref} style={{ display: "block", width: "100%", height: "140px" }} />;
}

/* ─── canvas: free reveal ───────────────────────────────────────────────────── */
function FreeCanvas() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const canvas = ref.current; if (!canvas) return;
    const { ctx, box, ro } = setupCanvas(canvas);
    let t = 0, last = performance.now(), raf = 0;
    const CYCLE = 4.2;
    const frame = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05); last = now; t += dt;
      const { W, H } = box, cx2 = W / 2, cy2 = H / 2 + 4, p = (t % CYCLE) / CYCLE;
      ctx.clearRect(0, 0, W, H);
      ctx.textAlign = "center"; ctx.textBaseline = "middle";
      const bg = ctx.createRadialGradient(cx2, cy2, 0, cx2, cy2, W * 0.5);
      bg.addColorStop(0, acc(0.05)); bg.addColorStop(1, acc(0)); ctx.fillStyle = bg; ctx.fillRect(0, 0, W, H);
      const priceAlpha = p < 0.55 ? 1 : Math.max(0, 1 - (p - 0.55) / 0.12);
      const strike = p > 0.42 ? Math.min(1, (p - 0.42) / 0.16) : 0;
      const freeIn = p > 0.6  ? Math.min(1, (p - 0.6)  / 0.16) : 0;
      if (priceAlpha > 0) {
        ctx.font = "600 30px var(--font-space,'Space Grotesk',sans-serif)"; ctx.fillStyle = `rgba(150,160,165,${0.85 * priceAlpha})`; ctx.fillText("₹500 / yr", cx2, cy2);
        const tw = ctx.measureText("₹500 / yr").width;
        if (strike > 0) { ctx.strokeStyle = `rgba(235,90,80,.95)`; ctx.lineWidth = 3; ctx.lineCap = "round"; ctx.beginPath(); ctx.moveTo(cx2 - tw / 2 - 6, cy2); ctx.lineTo(cx2 - tw / 2 - 6 + (tw + 12) * strike, cy2); ctx.stroke(); }
      }
      if (freeIn > 0) {
        const s = 0.8 + 0.2 * freeIn;
        ctx.save(); ctx.translate(cx2, cy2); ctx.scale(s, s);
        ctx.font = "700 32px var(--font-space,'Space Grotesk',sans-serif)"; ctx.fillStyle = acc(0.98 * freeIn); ctx.shadowColor = acc(0.7 * freeIn); ctx.shadowBlur = 18;
        ctx.fillText("FREE. FOREVER.", 0, 0); ctx.shadowBlur = 0; ctx.restore();
      }
      ctx.font = "500 11px var(--font-mono,'IBM Plex Mono',monospace)"; ctx.textAlign = "left"; ctx.textBaseline = "alphabetic"; ctx.fillStyle = acc(0.55); ctx.fillText("NO ADS · NO LOGIN", 14, 22);
      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, []);
  return <canvas ref={ref} style={{ display: "block", width: "100%", height: "140px" }} />;
}

/* ─── phone mockup ──────────────────────────────────────────────────────────── */
function MiniWave() {
  const ref = useRef<HTMLCanvasElement>(null);
  useEffect(() => {
    const c = ref.current; if (!c) return;
    const ctx = c.getContext("2d")!;
    const W = c.width, H = c.height;
    const N = 60; const data = new Array(N).fill(0.5);
    let ph = 0, last = performance.now(), raf = 0;
    const frame = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05); last = now; ph += dt * 0.8;
      data.shift();
      data.push(Math.max(0.1, Math.min(0.9, 0.5 + 0.26 * Math.sin(ph) + 0.1 * Math.sin(ph * 2.7) + 0.04 * (Math.random() - 0.5))));
      ctx.clearRect(0, 0, W, H);
      const xA = (i: number) => i / (N - 1) * W, yA = (v: number) => H - 4 - v * (H - 8);
      const g = ctx.createLinearGradient(0, 0, 0, H);
      g.addColorStop(0, "rgba(59,130,246,.38)"); g.addColorStop(1, "rgba(59,130,246,0)");
      ctx.beginPath(); ctx.moveTo(0, H);
      for (let i = 0; i < N; i++) ctx.lineTo(xA(i), yA(data[i]));
      ctx.lineTo(W, H); ctx.closePath(); ctx.fillStyle = g; ctx.fill();
      ctx.beginPath();
      for (let i = 0; i < N; i++) { i ? ctx.lineTo(xA(i), yA(data[i])) : ctx.moveTo(xA(i), yA(data[i])); }
      ctx.strokeStyle = "rgba(59,130,246,.95)"; ctx.lineWidth = 1.5; ctx.shadowColor = "rgba(59,130,246,.6)"; ctx.shadowBlur = 5; ctx.stroke(); ctx.shadowBlur = 0;
      raf = requestAnimationFrame(frame);
    };
    raf = requestAnimationFrame(frame);
    return () => cancelAnimationFrame(raf);
  }, []);
  return <canvas ref={ref} width={220} height={56} style={{ display: "block", width: "100%", height: "56px" }} />;
}

const NETS = [
  { ssid: "Home_5G",   ch: 36, band: "5 GHz", base: -52, bars: 4 },
  { ssid: "AndroidAP", ch:  6, band: "2.4 GHz", base: -67, bars: 3 },
  { ssid: "Jio_FIBER", ch:  1, band: "2.4 GHz", base: -81, bars: 1 },
];
function qualColor(dbm: number) {
  if (dbm >= -60) return "#22c55e";
  if (dbm >= -70) return "#f59e0b";
  if (dbm >= -80) return "#f97316";
  return "#ef4444";
}
function qualLabel(dbm: number) {
  if (dbm >= -60) return "Excellent";
  if (dbm >= -70) return "Good";
  if (dbm >= -80) return "Fair";
  return "Weak";
}
function SignalBars({ bars, color }: { bars: number; color: string }) {
  const heights = [38, 55, 72, 90];
  return (
    <div style={{ display: "flex", alignItems: "flex-end", gap: "2px", height: "16px" }}>
      {heights.map((h, i) => (
        <div key={i} style={{ width: "3px", height: `${h}%`, borderRadius: "1px", background: i < bars ? color : "rgba(255,255,255,0.12)" }} />
      ))}
    </div>
  );
}

function PhoneMockup() {
  const [screen, setScreen] = useState(0);
  const [fade, setFade]     = useState(false);
  const [rssi, setRssi]     = useState(NETS.map(n => n.base));

  useEffect(() => {
    const t = setInterval(() => {
      setFade(true);
      setTimeout(() => { setScreen(s => (s + 1) % 3); setFade(false); }, 360);
    }, 4000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    const t = setInterval(() => {
      setRssi(prev => prev.map((v, i) => Math.max(-92, Math.min(-38, v + Math.round((Math.random() - 0.5) * 4)))));
    }, 1100);
    return () => clearInterval(t);
  }, []);

  const phoneW = 270, phoneH = 560;
  const padX = 10, padTop = 12, padBot = 14;
  const screenW = phoneW - padX * 2, screenH = phoneH - padTop - padBot;

  /* status bar */
  const StatusBar = () => (
    <div style={{ height: "26px", display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 14px", flexShrink: 0 }}>
      <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "11px", color: "rgba(233,243,240,.9)", fontWeight: 500 }}>9:41</span>
      <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
        <svg width="15" height="11" viewBox="0 0 15 11" fill="none"><path d="M7.5 2 A7,7 0 0 1 15,9" stroke="rgba(233,243,240,.7)" strokeWidth="1.4" strokeLinecap="round"/><path d="M7.5 4 A5,5 0 0 1 13,9" stroke="rgba(233,243,240,.85)" strokeWidth="1.4" strokeLinecap="round"/><path d="M7.5 6 A3,3 0 0 1 11,9" stroke="rgba(233,243,240,.95)" strokeWidth="1.4" strokeLinecap="round"/><circle cx="7.5" cy="9.5" r="1.2" fill="rgba(233,243,240,.95)"/></svg>
        <svg width="22" height="11" viewBox="0 0 22 11" fill="none"><rect x="0.5" y="0.5" width="19" height="10" rx="2" stroke="rgba(233,243,240,.5)" strokeWidth="0.8"/><rect x="2" y="2" width="14" height="7" rx="1" fill="rgba(233,243,240,.85)"/><rect x="20" y="3.5" width="1.5" height="4" rx="0.75" fill="rgba(233,243,240,.4)"/></svg>
      </div>
    </div>
  );

  /* ── screen 0: network list ─────────────────────────────────────────── */
  const Screen0 = () => (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      {/* app bar */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "8px 14px 10px", borderBottom: "1px solid rgba(255,255,255,.06)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <svg width="18" height="18" viewBox="0 0 108 108" fill="none"><path d="M 18,55 A 40,40 0 0 1 90,55" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round"/><path d="M 29,60 A 28,28 0 0 1 79,60" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round"/><path d="M 40,65 A 16,16 0 0 1 68,65" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round"/><line x1="54" y1="72" x2="78" y2="48" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round"/><circle cx="54" cy="72" r="6" fill="#3B82F6"/></svg>
          <span style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 700, fontSize: "16px", color: "#e9f3f0" }}>Beacon</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "5px", fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9.5px", letterSpacing: ".12em", textTransform: "uppercase", color: "#3B82F6" }}>
          <span style={{ width: "5px", height: "5px", borderRadius: "50%", background: "#3B82F6", animation: "bcn-blink 1.6s ease-in-out infinite", display: "inline-block" }} />
          LIVE
        </div>
      </div>
      {/* network rows */}
      <div style={{ flex: 1, overflow: "hidden", padding: "6px 10px", display: "flex", flexDirection: "column", gap: "6px" }}>
        {NETS.map((n, i) => {
          const col = qualColor(rssi[i]);
          const bars = rssi[i] >= -60 ? 4 : rssi[i] >= -70 ? 3 : rssi[i] >= -80 ? 2 : 1;
          return (
            <div key={n.ssid} style={{ background: "rgba(255,255,255,.045)", border: "1px solid rgba(255,255,255,.07)", borderRadius: "10px", padding: "9px 12px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
              <div>
                <div style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "12px", fontWeight: 600, color: "#e9f3f0", marginBottom: "3px" }}>{n.ssid}</div>
                <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9.5px", color: "rgba(166,186,184,.7)", letterSpacing: ".04em" }}>
                  {rssi[i]} dBm · CH {n.ch} · {n.band}
                </div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "4px" }}>
                <SignalBars bars={bars} color={col} />
                <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "8px", color: col, letterSpacing: ".06em" }}>{qualLabel(rssi[i])}</span>
              </div>
            </div>
          );
        })}
        {/* extra dim network */}
        <div style={{ background: "rgba(255,255,255,.025)", border: "1px solid rgba(255,255,255,.05)", borderRadius: "10px", padding: "9px 12px", display: "flex", alignItems: "center", justifyContent: "space-between", opacity: 0.5 }}>
          <div>
            <div style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "12px", fontWeight: 600, color: "#e9f3f0" }}>Cafe_Guest</div>
            <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9.5px", color: "rgba(166,186,184,.7)", letterSpacing: ".04em" }}>-88 dBm · CH 11 · 2.4 GHz</div>
          </div>
          <SignalBars bars={1} color="#ef4444" />
        </div>
      </div>
    </div>
  );

  /* ── screen 1: live signal detail ───────────────────────────────────── */
  const Screen1 = () => {
    const v = rssi[0], col = qualColor(v);
    return (
      <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        {/* app bar */}
        <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "8px 14px 10px", borderBottom: "1px solid rgba(255,255,255,.06)" }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(166,186,184,.7)" strokeWidth="2.2" strokeLinecap="round"><path d="m15 18-6-6 6-6"/></svg>
          <span style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 600, fontSize: "14px", color: "#e9f3f0" }}>Home_5G</span>
        </div>
        {/* big RSSI */}
        <div style={{ textAlign: "center", padding: "16px 14px 10px" }}>
          <div style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 700, fontSize: "46px", color: col, lineHeight: 1, textShadow: `0 0 30px ${col}55` }}>{v}</div>
          <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "10px", letterSpacing: ".14em", textTransform: "uppercase", color: "rgba(166,186,184,.7)", marginTop: "2px" }}>dBm</div>
          <div style={{ display: "inline-flex", alignItems: "center", gap: "5px", marginTop: "8px", background: `${col}18`, border: `1px solid ${col}44`, borderRadius: "100px", padding: "3px 10px" }}>
            <span style={{ width: "5px", height: "5px", borderRadius: "50%", background: col, display: "inline-block" }} />
            <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9px", letterSpacing: ".12em", textTransform: "uppercase", color: col }}>{qualLabel(v)}</span>
          </div>
        </div>
        {/* waveform */}
        <div style={{ margin: "0 12px 8px", background: "rgba(59,130,246,.06)", border: "1px solid rgba(59,130,246,.15)", borderRadius: "8px", overflow: "hidden", padding: "4px 0" }}>
          <MiniWave />
        </div>
        {/* details */}
        <div style={{ padding: "0 14px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: "7px" }}>
          {[["Band", "5 GHz"], ["Channel", "36"], ["Frequency", "5180 MHz"], ["Link Speed", "433 Mbps"]].map(([k, val]) => (
            <div key={k} style={{ background: "rgba(255,255,255,.04)", borderRadius: "8px", padding: "7px 9px" }}>
              <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "8.5px", letterSpacing: ".1em", textTransform: "uppercase", color: "rgba(166,186,184,.55)", marginBottom: "2px" }}>{k}</div>
              <div style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "11.5px", fontWeight: 600, color: "#e9f3f0" }}>{val}</div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  /* ── screen 2: channel analysis ─────────────────────────────────────── */
  const Screen2 = () => (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      {/* app bar */}
      <div style={{ padding: "8px 14px 10px", borderBottom: "1px solid rgba(255,255,255,.06)" }}>
        <span style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 600, fontSize: "14px", color: "#e9f3f0" }}>Channel Analysis</span>
      </div>
      {/* recommendation card */}
      <div style={{ margin: "10px 10px 8px", background: "rgba(59,130,246,.12)", border: "1px solid rgba(59,130,246,.3)", borderRadius: "10px", padding: "10px 12px" }}>
        <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "8.5px", letterSpacing: ".14em", textTransform: "uppercase", color: "rgba(59,130,246,.8)", marginBottom: "4px" }}>Recommendation</div>
        <div style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 700, fontSize: "14px", color: "#3B82F6" }}>Switch to Channel 11</div>
        <div style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "10px", color: "rgba(166,186,184,.8)", marginTop: "2px" }}>Least congested, 0 competing networks</div>
      </div>
      {/* channel bars */}
      <div style={{ padding: "0 12px", display: "flex", flexDirection: "column", gap: "7px" }}>
        <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "8.5px", letterSpacing: ".12em", textTransform: "uppercase", color: "rgba(166,186,184,.6)", marginBottom: "2px" }}>2.4 GHz Channels</div>
        {[
          { ch: 1,  nets: 3, pct: 0.88, col: "#ef4444" },
          { ch: 6,  nets: 2, pct: 0.60, col: "#f97316" },
          { ch: 11, nets: 0, pct: 0.06, col: "#3B82F6"  },
        ].map(({ ch, nets, pct, col }) => (
          <div key={ch}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "3px" }}>
              <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "10px", color: ch === 11 ? "#3B82F6" : "rgba(233,243,240,.7)", fontWeight: ch === 11 ? 700 : 400 }}>
                CH {ch} {ch === 11 ? "← use this" : ""}
              </span>
              <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9px", color: "rgba(166,186,184,.6)" }}>{nets} net{nets !== 1 ? "s" : ""}</span>
            </div>
            <div style={{ height: "6px", background: "rgba(255,255,255,.07)", borderRadius: "3px", overflow: "hidden" }}>
              <div style={{ height: "100%", width: `${pct * 100}%`, background: col, borderRadius: "3px", boxShadow: ch === 11 ? `0 0 6px ${col}` : "none", transition: "width .6s ease" }} />
            </div>
          </div>
        ))}
        {/* 5 GHz section */}
        <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "8.5px", letterSpacing: ".12em", textTransform: "uppercase", color: "rgba(166,186,184,.6)", marginTop: "6px", marginBottom: "2px" }}>5 GHz Channels</div>
        {[{ ch: 36, nets: 1, pct: 0.22, col: "#f59e0b" }, { ch: 40, nets: 0, pct: 0.04, col: "#22c55e" }].map(({ ch, nets, pct, col }) => (
          <div key={ch}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "3px" }}>
              <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "10px", color: "rgba(233,243,240,.7)" }}>CH {ch}</span>
              <span style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "9px", color: "rgba(166,186,184,.6)" }}>{nets} net{nets !== 1 ? "s" : ""}</span>
            </div>
            <div style={{ height: "6px", background: "rgba(255,255,255,.07)", borderRadius: "3px", overflow: "hidden" }}>
              <div style={{ height: "100%", width: `${pct * 100}%`, background: col, borderRadius: "3px" }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  const SCREENS = [Screen0, Screen1, Screen2];
  const ActiveScreen = SCREENS[screen];
  const labels = ["Network Scanner", "Live Signal", "Channel Analysis"];

  return (
    <div style={{ position: "relative", display: "inline-flex", flexDirection: "column", alignItems: "center", gap: "16px" }}>
      {/* phone frame */}
      <div style={{
        width: phoneW, height: phoneH,
        background: "linear-gradient(145deg,#1a1a1a,#0d0d0d)",
        borderRadius: "36px",
        padding: `${padTop}px ${padX}px ${padBot}px`,
        boxShadow: "0 0 0 1px rgba(255,255,255,.08), 0 0 0 2px rgba(0,0,0,.8), 0 30px 80px rgba(0,0,0,.7), 0 0 80px rgba(59,130,246,.12)",
        display: "flex",
        flexDirection: "column",
        position: "relative",
      }}>
        {/* side button marks */}
        <div style={{ position: "absolute", left: "-3px", top: "90px", width: "3px", height: "28px", background: "#2a2a2a", borderRadius: "2px 0 0 2px" }} />
        <div style={{ position: "absolute", left: "-3px", top: "128px", width: "3px", height: "44px", background: "#2a2a2a", borderRadius: "2px 0 0 2px" }} />
        <div style={{ position: "absolute", right: "-3px", top: "108px", width: "3px", height: "56px", background: "#2a2a2a", borderRadius: "0 2px 2px 0" }} />
        {/* screen bezel */}
        <div style={{
          flex: 1,
          background: "#06090d",
          borderRadius: "26px",
          overflow: "hidden",
          display: "flex",
          flexDirection: "column",
          border: "1px solid rgba(255,255,255,.05)",
          boxShadow: "inset 0 0 20px rgba(0,0,0,.5)",
        }}>
          <StatusBar />
          {/* punch-hole camera indicator */}
          <div style={{ display: "flex", justifyContent: "center", marginBottom: "2px" }}>
            <div style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#111" }} />
          </div>
          {/* screen content with fade transition */}
          <div style={{ flex: 1, display: "flex", flexDirection: "column", opacity: fade ? 0 : 1, transition: "opacity 0.36s ease" }}>
            <ActiveScreen />
          </div>
          {/* home pill */}
          <div style={{ display: "flex", justifyContent: "center", padding: "8px 0 6px" }}>
            <div style={{ width: "52px", height: "4px", borderRadius: "2px", background: "rgba(233,243,240,.25)" }} />
          </div>
        </div>
      </div>
      {/* screen indicator dots */}
      <div style={{ display: "flex", gap: "7px" }}>
        {[0, 1, 2].map(i => (
          <div key={i} style={{ width: i === screen ? "18px" : "6px", height: "6px", borderRadius: "3px", background: i === screen ? "#3B82F6" : "rgba(166,186,184,.25)", transition: "all .4s ease", boxShadow: i === screen ? "0 0 8px rgba(59,130,246,.5)" : "none" }} />
        ))}
      </div>
      {/* current screen label */}
      <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "10px", letterSpacing: ".14em", textTransform: "uppercase", color: "rgba(166,186,184,.55)", textAlign: "center", minHeight: "14px" }}>
        {labels[screen]}
      </div>
    </div>
  );
}

/* ─── scroll reveal ─────────────────────────────────────────────────────────── */
function useReveal() {
  useEffect(() => {
    const els = Array.from(document.querySelectorAll<HTMLElement>("[data-reveal]"));
    const reveal = (el: HTMLElement) => {
      if (el.dataset.revealed === "1") return;
      el.dataset.revealed = "1";
      const d = parseFloat(el.dataset.revealDelay || "0");
      el.animate([{ opacity: 0, transform: "translateY(30px)" }, { opacity: 1, transform: "translateY(0)" }],
        { duration: 850, delay: d, easing: "cubic-bezier(.2,.7,.2,1)", fill: "forwards" });
    };
    let ticking = false;
    const sweep = () => {
      ticking = false;
      const vh = window.innerHeight;
      els.forEach(el => { if (el.dataset.revealed !== "1") { const r = el.getBoundingClientRect(); if (r.top < vh * 0.92 && r.bottom > 0) reveal(el); } });
    };
    const onScroll = () => { if (!ticking) { ticking = true; requestAnimationFrame(sweep); } };
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    requestAnimationFrame(sweep); setTimeout(sweep, 120);
    return () => { window.removeEventListener("scroll", onScroll); window.removeEventListener("resize", onScroll); };
  }, []);
}

/* ─── download button with platform detection ───────────────────────────────── */
function DownloadButton({ label, style, className }: { label: string; style?: React.CSSProperties; className?: string }) {
  const [device, setDevice]       = useState<Device>("init");
  const [showModal, setShowModal] = useState(false);
  const [showIos, setShowIos]     = useState(false);

  useEffect(() => {
    const ua = navigator.userAgent;
    if (/android/i.test(ua)) setDevice("android");
    else if (/iPad|iPhone|iPod/.test(ua) && !(window as any).MSStream) setDevice("ios");
    else setDevice("desktop");
  }, []);

  const handleClick = () => {
    if (device === "android") { window.location.href = APK_URL; }
    else if (device === "ios") { setShowIos(true); }
    else { setShowModal(true); }
  };

  return (
    <>
      <button onClick={handleClick} style={style} className={className}>{label}</button>
      {showModal && <QrModal onClose={() => setShowModal(false)} />}
      {showIos   && <IosModal onClose={() => setShowIos(false)} />}
    </>
  );
}

/* ─── QR modal (desktop) ────────────────────────────────────────────────────── */
function QrModal({ onClose }: { onClose: () => void }) {
  const qrSrc = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(APK_URL)}&color=e9f3f0&bgcolor=0e171c&margin=12&format=svg`;
  return (
    <div onClick={onClose} style={{ position: "fixed", inset: 0, zIndex: 9999, isolation: "isolate", background: "rgba(5,8,10,.88)", backdropFilter: "blur(14px)", WebkitBackdropFilter: "blur(14px)", display: "flex", alignItems: "center", justifyContent: "center", padding: "20px" }}>
      <div onClick={e => e.stopPropagation()} style={{ background: "rgba(14,23,28,0.95)", border: "1px solid rgba(59,130,246,.25)", borderRadius: "22px", padding: "36px", maxWidth: "340px", width: "100%", textAlign: "center", boxShadow: `0 0 0 1px rgba(255,255,255,.06), 0 40px 100px rgba(0,0,0,.7), 0 0 80px rgba(${ACCENT.r},${ACCENT.g},${ACCENT.b},.2)` }}>
        <div style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "11px", letterSpacing: ".16em", textTransform: "uppercase", color: "var(--accent)", marginBottom: "20px" }}>Scan to download APK</div>
        <div style={{ background: "#0e171c", borderRadius: "12px", padding: "16px", display: "inline-block" }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={qrSrc} alt="QR code to download Beacon APK" width={176} height={176} style={{ display: "block" }} />
        </div>
        <p style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "14px", color: "var(--muted)", margin: "18px 0 0", lineHeight: 1.5 }}>
          Scan with your Android phone to download Beacon v{VERSION} directly.
        </p>
        <p style={{ fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "11px", color: "var(--muted)", marginTop: "8px" }}>
          Android 7.0+ · ~16 MB · No account
        </p>
        <button onClick={onClose} style={{ marginTop: "22px", fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "12px", letterSpacing: ".06em", color: "var(--muted)", background: "none", border: "1px solid var(--line)", borderRadius: "8px", padding: "8px 20px", cursor: "pointer", transition: "color .2s,border-color .2s" }}
          onMouseEnter={e => { (e.target as HTMLButtonElement).style.color = "var(--text)"; (e.target as HTMLButtonElement).style.borderColor = "rgba(255,255,255,.22)"; }}
          onMouseLeave={e => { (e.target as HTMLButtonElement).style.color = "var(--muted)"; (e.target as HTMLButtonElement).style.borderColor = "var(--line)"; }}>
          Close
        </button>
      </div>
    </div>
  );
}

/* ─── iOS modal ─────────────────────────────────────────────────────────────── */
function IosModal({ onClose }: { onClose: () => void }) {
  return (
    <div onClick={onClose} style={{ position: "fixed", inset: 0, zIndex: 9999, isolation: "isolate", background: "rgba(5,8,10,.88)", backdropFilter: "blur(14px)", WebkitBackdropFilter: "blur(14px)", display: "flex", alignItems: "center", justifyContent: "center", padding: "20px" }}>
      <div onClick={e => e.stopPropagation()} style={{ background: "rgba(14,23,28,0.95)", border: "1px solid rgba(255,255,255,.08)", borderRadius: "22px", padding: "36px", maxWidth: "320px", width: "100%", textAlign: "center", boxShadow: "0 40px 100px rgba(0,0,0,.7)" }}>
        <div style={{ fontSize: "32px", marginBottom: "14px" }}></div>
        <h3 style={{ fontFamily: "var(--font-space,'Space Grotesk',sans-serif)", fontWeight: 700, fontSize: "1.3rem", color: "var(--text)", margin: "0 0 10px" }}>Android only, for now</h3>
        <p style={{ fontFamily: "var(--font-sans,'IBM Plex Sans',sans-serif)", fontSize: "14px", color: "var(--muted)", lineHeight: 1.6, margin: 0 }}>
          An iOS version is on the way. For now, Beacon runs on Android.
        </p>
        <button onClick={onClose} style={{ marginTop: "22px", fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "12px", letterSpacing: ".06em", color: "var(--muted)", background: "none", border: "1px solid var(--line)", borderRadius: "8px", padding: "8px 20px", cursor: "pointer" }}>Got it</button>
      </div>
    </div>
  );
}

/* ─── inline icon svgs ──────────────────────────────────────────────────────── */
const IconDownload = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 3v12"/><path d="m7 11 5 5 5-5"/><path d="M5 21h14"/>
  </svg>
);
const IconGitHub = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 .5C5.7.5.5 5.7.5 12c0 5.1 3.3 9.4 7.9 10.9.6.1.8-.2.8-.6v-2c-3.2.7-3.9-1.4-3.9-1.4-.5-1.3-1.3-1.7-1.3-1.7-1.1-.7.1-.7.1-.7 1.2.1 1.8 1.2 1.8 1.2 1 1.8 2.7 1.3 3.4 1 .1-.8.4-1.3.7-1.6-2.6-.3-5.3-1.3-5.3-5.7 0-1.3.5-2.3 1.2-3.1-.1-.3-.5-1.5.1-3.1 0 0 1-.3 3.3 1.2a11.5 11.5 0 0 1 6 0C17 5.3 18 5.6 18 5.6c.6 1.6.2 2.8.1 3.1.8.8 1.2 1.8 1.2 3.1 0 4.4-2.7 5.4-5.3 5.7.4.4.8 1.1.8 2.2v3.3c0 .4.2.7.8.6 4.6-1.5 7.9-5.8 7.9-10.9C23.5 5.7 18.3.5 12 .5Z"/>
  </svg>
);
const IconWifi = () => (
  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
    <path d="M2 8.6a15 15 0 0 1 20 0"/><path d="M5 12a10 10 0 0 1 14 0"/><path d="M8.5 15.4a5 5 0 0 1 7 0"/><circle cx="12" cy="19" r="1.2" fill="currentColor" stroke="none"/>
  </svg>
);
const IconMap = () => (
  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 21s-7-5.5-7-11a7 7 0 0 1 14 0c0 5.5-7 11-7 11Z"/><circle cx="12" cy="10" r="2.4"/>
  </svg>
);
const IconSliders = () => (
  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
    <line x1="4" y1="6" x2="20" y2="6"/><circle cx="9" cy="6" r="2.3"/><line x1="4" y1="12" x2="20" y2="12"/><circle cx="15.5" cy="12" r="2.3"/><line x1="4" y1="18" x2="20" y2="18"/><circle cx="8" cy="18" r="2.3"/>
  </svg>
);
const IconNo = () => (
  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round">
    <circle cx="12" cy="12" r="9"/><line x1="5.6" y1="5.6" x2="18.4" y2="18.4"/>
  </svg>
);

/* ─── shared button styles ──────────────────────────────────────────────────── */
const btnPrimary: React.CSSProperties = {
  display: "inline-flex", alignItems: "center", gap: "10px", whiteSpace: "nowrap",
  fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "14px", fontWeight: 500, letterSpacing: ".02em",
  color: "#03110d", background: "var(--accent)", textDecoration: "none",
  padding: "15px 26px", borderRadius: "12px", border: "none", cursor: "pointer",
  boxShadow: "0 0 34px rgba(59,130,246,.32)", transition: "transform .2s,box-shadow .2s",
};
const btnGhost: React.CSSProperties = {
  display: "inline-flex", alignItems: "center", gap: "10px", whiteSpace: "nowrap",
  fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)", fontSize: "14px", letterSpacing: ".02em",
  color: "var(--text)", background: "none", textDecoration: "none",
  padding: "15px 24px", borderRadius: "12px", border: "1px solid rgba(255,255,255,.16)", cursor: "pointer",
  transition: "border-color .2s,background .2s",
};
const mono: React.CSSProperties = { fontFamily: "var(--font-mono,'IBM Plex Mono',monospace)" };
const heading: React.CSSProperties = { fontFamily: "var(--font-space,'Space Grotesk',sans-serif)" };

/* ─── page ───────────────────────────────────────────────────────────────────── */
export default function HomePage() {
  useReveal();

  return (
    <div style={{ background: "var(--bg)", color: "var(--text)", fontFamily: "var(--font-sans,'IBM Plex Sans',system-ui,sans-serif)", position: "relative", overflowX: "hidden", WebkitFontSmoothing: "antialiased" }}>

      {/* ── global ambient background blobs ─────────────────────────────── */}
      <div aria-hidden="true" style={{ position: "fixed", inset: 0, zIndex: 0, overflow: "hidden", pointerEvents: "none" }}>
        <div className="bcn-blob-1" style={{ position: "absolute", top: "-8%", left: "-6%", width: "52vw", height: "52vw", borderRadius: "50%", background: "radial-gradient(circle,rgba(59,130,246,.13) 0%,transparent 68%)", filter: "blur(55px)" }} />
        <div className="bcn-blob-2" style={{ position: "absolute", top: "35%", right: "-12%", width: "46vw", height: "46vw", borderRadius: "50%", background: "radial-gradient(circle,rgba(34,211,238,.08) 0%,transparent 65%)", filter: "blur(60px)" }} />
        <div className="bcn-blob-3" style={{ position: "absolute", bottom: "-5%", left: "22%", width: "44vw", height: "44vw", borderRadius: "50%", background: "radial-gradient(circle,rgba(99,102,241,.07) 0%,transparent 65%)", filter: "blur(60px)" }} />
      </div>

      {/* ── NAV ──────────────────────────────────────────────────────────── */}
      <nav style={{ position: "fixed", top: 0, left: 0, right: 0, zIndex: 60, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "16px clamp(20px,5vw,56px)", backdropFilter: "blur(14px)", WebkitBackdropFilter: "blur(14px)", background: "rgba(5,8,10,.55)", borderBottom: "1px solid var(--line)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "10px", ...heading, fontWeight: 600, fontSize: "18px", letterSpacing: "-.01em" }}>
          <BeaconIcon size={26} />
          Beacon
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" style={{ ...mono, fontSize: "12.5px", letterSpacing: ".04em", color: "var(--muted)", textDecoration: "none", padding: "9px 14px", borderRadius: "9px", border: "1px solid var(--line)", transition: "color .2s,border-color .2s" }}>GitHub</a>
          <DownloadButton label="Download" style={{ ...mono, fontSize: "12.5px", fontWeight: 500, letterSpacing: ".03em", color: "#03110d", background: "var(--accent)", padding: "9px 16px", borderRadius: "9px", border: "none", cursor: "pointer", boxShadow: "0 0 22px rgba(59,130,246,.28)", transition: "transform .2s,box-shadow .2s" }} />
        </div>
      </nav>

      {/* ══════════════════════════════════════════════════════════════════
          FOLD 1 · HERO
      ═══════════════════════════════════════════════════════════════════ */}
      <section style={{ position: "relative", zIndex: 1, minHeight: "100svh", display: "flex", alignItems: "center", overflow: "hidden" }}>
        <HeroCanvas />
        {/* gradient overlay */}
        <div style={{ position: "absolute", inset: 0, zIndex: 1, pointerEvents: "none", background: "linear-gradient(95deg,var(--bg) 4%,rgba(5,8,10,.72) 34%,rgba(5,8,10,0) 66%),linear-gradient(0deg,var(--bg) 1%,rgba(5,8,10,0) 26%)" }} />
        <div style={{ position: "relative", zIndex: 2, width: "100%", maxWidth: "1200px", margin: "0 auto", padding: "120px clamp(20px,5vw,56px) 80px" }}>
          <div style={{ maxWidth: "680px" }}>
            <div data-reveal data-reveal-delay="0" style={{ display: "inline-flex", alignItems: "center", gap: "10px", whiteSpace: "nowrap", ...mono, fontSize: "12px", letterSpacing: ".16em", textTransform: "uppercase", color: "var(--accent)", border: "1px solid rgba(59,130,246,.3)", background: "rgba(59,130,246,.06)", padding: "8px 16px", borderRadius: "100px" }}>
              <span className="bcn-blink" style={{ width: "7px", height: "7px", borderRadius: "50%", background: "var(--accent)" }} />
              Free · Open Source · No Ads
            </div>
            <h1 data-reveal data-reveal-delay="90" style={{ ...heading, fontWeight: 700, fontSize: "clamp(2.9rem,7.5vw,6rem)", lineHeight: 0.97, letterSpacing: "-.03em", margin: "26px 0 0", color: "var(--text)" }}>
              See your WiFi.<br />Fix your <span style={{ color: "var(--accent)" }}>signal.</span>
            </h1>
            <p data-reveal data-reveal-delay="170" style={{ fontSize: "clamp(1.05rem,1.7vw,1.3rem)", lineHeight: 1.55, color: "var(--muted)", maxWidth: "520px", margin: "24px 0 0" }}>
              Beacon shows your real signal strength, finds the least-crowded channel, and tells you how to fix weak spots in real time, with{" "}
              <span style={{ color: "var(--text)", fontWeight: 500 }}>no ads</span> and{" "}
              <span style={{ color: "var(--text)", fontWeight: 500 }}>no paywall.</span>
            </p>
            <div data-reveal data-reveal-delay="250" style={{ display: "flex", flexWrap: "wrap", gap: "13px", margin: "36px 0 0" }}>
              <DownloadButton label="Download for Android" style={{ ...btnPrimary }} />
              <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" style={{ ...btnGhost }}>
                <IconGitHub /> View source
              </a>
            </div>
            <p data-reveal data-reveal-delay="330" style={{ ...mono, fontSize: "12.5px", letterSpacing: ".05em", color: "var(--muted)", margin: "24px 0 0" }}>
              Android 7.0+&nbsp;&nbsp;·&nbsp;&nbsp;~16 MB&nbsp;&nbsp;·&nbsp;&nbsp;Sideload APK&nbsp;&nbsp;·&nbsp;&nbsp;Play Store coming soon
            </p>
          </div>
        </div>
        {/* scroll cue */}
        <div style={{ position: "absolute", bottom: "26px", left: "50%", transform: "translateX(-50%)", zIndex: 2, display: "flex", flexDirection: "column", alignItems: "center", gap: "8px", ...mono, fontSize: "11px", letterSpacing: ".22em", textTransform: "uppercase", color: "var(--muted)" }}>
          scroll
          <svg className="bcn-cue" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m6 9 6 6 6-6"/></svg>
        </div>
      </section>

      {/* ══════════════════════════════════════════════════════════════════
          FOLD 2 · STORY
      ═══════════════════════════════════════════════════════════════════ */}
      <section style={{ position: "relative", zIndex: 1, overflow: "hidden", padding: "clamp(90px,13vw,170px) clamp(20px,5vw,56px)" }}>
        <div style={{ position: "relative", maxWidth: "1200px", margin: "0 auto" }}>
          <div className="story-grid">
          <div> {/* left column */}
          <div data-reveal style={{ ...mono, fontSize: "14px", fontWeight: 500, letterSpacing: ".16em", textTransform: "uppercase", color: "var(--accent)", marginBottom: "22px" }}>Why I built this</div>
          <h2 data-reveal data-reveal-delay="60" style={{ ...heading, fontWeight: 700, fontSize: "clamp(2rem,4.6vw,3.5rem)", lineHeight: 1.05, letterSpacing: "-.025em", margin: 0, maxWidth: "18ch" }}>
            I was in{" "}
            <a href="https://en.wikipedia.org/wiki/Yercaud" target="_blank" rel="noopener noreferrer" style={{ color: "var(--accent)", textDecoration: "underline", textDecorationColor: "rgba(59,130,246,.4)", textUnderlineOffset: "5px" }}>Yercaud</a>
            , looking for a good <span style={{ color: "var(--accent)" }}>WiFi spot.</span>
          </h2>
          <div style={{ display: "grid", gap: "22px", marginTop: "34px", maxWidth: "620px" }}>
            <p data-reveal data-reveal-delay="40" style={{ fontSize: "1.12rem", lineHeight: 1.65, color: "var(--muted)", margin: 0 }}>
              Yercaud is a hill station in Tamil Nadu. It is a beautiful place to live and work from, but the hilly terrain leaves the signal weak and patchy. I kept dragging my desk around the house chasing a better connection. Coffee shops in Chennai were hit or miss too: some had great WiFi, some were unusable.
            </p>
            <p data-reveal data-reveal-delay="40" style={{ fontSize: "1.12rem", lineHeight: 1.65, color: "var(--muted)", margin: 0 }}>
              A WiFi analyser is the right tool for this. It shows you where the signal drops and which channels are jammed. But every free one I tried played the same game: open it, sit through a full-screen ad, then find the real numbers blurred behind <span style={{ color: "var(--text)" }}>&ldquo;upgrade to see real numbers.&rdquo;</span> The upgrade ran ₹500 a year, to read a value that was already mine.
            </p>
            <p data-reveal data-reveal-delay="40" style={{ fontSize: "1.12rem", lineHeight: 1.65, color: "var(--muted)", margin: 0 }}>
              So I built Beacon over a weekend. Signal strength, frequency, channel overlap, co-channel neighbours. All of it.{" "}
              <span style={{ color: "var(--text)", fontWeight: 500 }}>No ads, no paywall, and it always will be.</span>
            </p>
          </div>

          <blockquote data-reveal style={{ position: "relative", margin: "56px 0 0", maxWidth: "24ch" }}>
            <span aria-hidden="true" style={{ ...heading, display: "block", fontSize: "clamp(3rem,6vw,4.4rem)", lineHeight: 0.7, color: "var(--accent)", opacity: 0.5 }}>&ldquo;</span>
            <p style={{ ...heading, fontWeight: 500, fontSize: "clamp(1.55rem,3.4vw,2.5rem)", lineHeight: 1.28, letterSpacing: "-.02em", color: "var(--text)", margin: "6px 0 0" }}>
              The signal data is yours. You should not have to pay to see it.
            </p>
            <div style={{ ...mono, fontSize: "11.5px", letterSpacing: ".14em", textTransform: "uppercase", color: "var(--muted)", marginTop: "20px", display: "flex", alignItems: "center", gap: "10px" }}>
              <span style={{ width: "26px", height: "1px", background: "var(--accent)", display: "inline-block" }} />
              Why Beacon is free
            </div>
          </blockquote>
          </div> {/* end left column */}
          {/* right column: phone mockup */}
          <div className="phone-col">
            <PhoneMockup />
          </div>
          </div> {/* end story-grid */}
        </div>
      </section>

      {/* ══════════════════════════════════════════════════════════════════
          FOLD 3 · FEATURES
      ═══════════════════════════════════════════════════════════════════ */}
      <section style={{ position: "relative", zIndex: 1, overflow: "hidden", background: "rgba(10,18,22,0.75)", backdropFilter: "blur(2px)", borderTop: "1px solid var(--line)", borderBottom: "1px solid var(--line)", padding: "clamp(90px,13vw,170px) clamp(20px,5vw,56px)" }}>
        <div style={{ position: "relative", maxWidth: "1080px", margin: "0 auto" }}>
          <div data-reveal style={{ ...mono, fontSize: "14px", fontWeight: 500, letterSpacing: ".16em", textTransform: "uppercase", color: "var(--accent)", marginBottom: "20px" }}>What you get</div>
          <h2 data-reveal data-reveal-delay="60" style={{ ...heading, fontWeight: 700, fontSize: "clamp(2rem,4.6vw,3.5rem)", lineHeight: 1.04, letterSpacing: "-.025em", margin: 0 }}>
            Everything. <span style={{ color: "var(--accent)" }}>For free.</span>
          </h2>
          <p data-reveal data-reveal-delay="120" style={{ fontSize: "1.12rem", lineHeight: 1.6, color: "var(--muted)", maxWidth: "46ch", margin: "18px 0 0" }}>No "pro" version. No features held back. No timer on your scan.</p>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(420px,1fr))", gap: "18px", marginTop: "48px" }}>
            {/* Live scan */}
            <div data-reveal data-reveal-delay="0" style={{ border: "1px solid rgba(255,255,255,.09)", borderRadius: "18px", background: "rgba(14,23,28,0.65)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)", overflow: "hidden" }}>
              <ScanCanvas />
              <div style={{ padding: "24px 28px 30px" }}>
                <div style={{ color: "var(--accent)" }}><IconWifi /></div>
                <h3 style={{ ...heading, fontWeight: 600, fontSize: "1.3rem", letterSpacing: "-.01em", margin: "18px 0 0" }}>Live signal scan</h3>
                <p style={{ fontSize: "1rem", lineHeight: 1.6, color: "var(--muted)", margin: "12px 0 0" }}>RSSI, frequency, channel, link speed, and band, refreshed every 1.5 seconds. The full picture of every network around you, not just your own.</p>
              </div>
            </div>
            {/* Heatmap */}
            <div data-reveal data-reveal-delay="90" style={{ border: "1px solid rgba(255,255,255,.09)", borderRadius: "18px", background: "rgba(14,23,28,0.65)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)", overflow: "hidden" }}>
              <HeatCanvas />
              <div style={{ padding: "24px 28px 30px" }}>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <div style={{ color: "var(--accent)" }}><IconMap /></div>
                  <span style={{ ...mono, fontSize: "10px", letterSpacing: ".14em", textTransform: "uppercase", color: "var(--accent)", border: "1px solid rgba(59,130,246,.3)", borderRadius: "100px", padding: "4px 10px" }}>Coming soon</span>
                </div>
                <h3 style={{ ...heading, fontWeight: 600, fontSize: "1.3rem", letterSpacing: "-.01em", margin: "18px 0 0" }}>Dead zone heatmap</h3>
                <p style={{ fontSize: "1rem", lineHeight: 1.6, color: "var(--muted)", margin: "12px 0 0" }}>Walk your space and watch the heatmap build in real time. Find the exact corner where signal drops, then fix it.</p>
              </div>
            </div>
            {/* Recommendations */}
            <div data-reveal data-reveal-delay="0" style={{ border: "1px solid rgba(255,255,255,.09)", borderRadius: "18px", background: "rgba(14,23,28,0.65)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)", overflow: "hidden" }}>
              <RecoCanvas />
              <div style={{ padding: "24px 28px 30px" }}>
                <div style={{ color: "var(--accent)" }}><IconSliders /></div>
                <h3 style={{ ...heading, fontWeight: 600, fontSize: "1.3rem", letterSpacing: "-.01em", margin: "18px 0 0" }}>Smart recommendations</h3>
                <p style={{ fontSize: "1rem", lineHeight: 1.6, color: "var(--muted)", margin: "12px 0 0" }}>Beacon reads your channel neighbours and gives specific actions: switch to channel 11, move the router, change band. Your actual situation, not generic tips.</p>
              </div>
            </div>
            {/* Free */}
            <div data-reveal data-reveal-delay="90" style={{ border: "1px solid rgba(59,130,246,.28)", borderRadius: "18px", background: "linear-gradient(160deg,rgba(59,130,246,.12),rgba(14,23,28,0.6) 55%)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)", overflow: "hidden" }}>
              <FreeCanvas />
              <div style={{ padding: "24px 28px 30px" }}>
                <div style={{ color: "var(--accent)" }}><IconNo /></div>
                <h3 style={{ ...heading, fontWeight: 600, fontSize: "1.3rem", letterSpacing: "-.01em", margin: "18px 0 0" }}>Zero ads. Zero paywall.</h3>
                <p style={{ fontSize: "1rem", lineHeight: 1.6, color: "var(--muted)", margin: "12px 0 0" }}>No banners, no interstitials, and nothing blurred behind a subscription. You download it and you use it. It is open source, so you can read every line.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════════════════════════════════════════════════════
          FOLD 4 · CTA
      ═══════════════════════════════════════════════════════════════════ */}
      <section style={{ position: "relative", zIndex: 1, overflow: "hidden", padding: "clamp(100px,15vw,190px) clamp(20px,5vw,56px) clamp(96px,13vw,150px)" }}>
        <div style={{ position: "absolute", top: "50%", left: "50%", transform: "translate(-50%,-60%)", width: "760px", height: "760px", maxWidth: "130vw", borderRadius: "50%", background: "radial-gradient(circle,var(--accent),transparent 62%)", opacity: 0.1, filter: "blur(80px)", pointerEvents: "none" }} />
        <div style={{ position: "relative", maxWidth: "760px", margin: "0 auto", textAlign: "center" }}>
          <h2 data-reveal style={{ ...heading, fontWeight: 700, fontSize: "clamp(2.6rem,7vw,5rem)", lineHeight: 1, letterSpacing: "-.03em", margin: 0 }}>
            Give it a <span style={{ color: "var(--accent)" }}>try.</span>
          </h2>
          <p data-reveal data-reveal-delay="80" style={{ fontSize: "clamp(1.05rem,1.7vw,1.25rem)", lineHeight: 1.6, color: "var(--muted)", maxWidth: "44ch", margin: "24px auto 0" }}>
            Sideload the APK on your Android phone, scan your networks, and tell me what you think. It takes about 30 seconds.
          </p>
          <div data-reveal data-reveal-delay="160" style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", gap: "13px", marginTop: "38px" }}>
            <DownloadButton label="Download Beacon, it's free" style={{ ...btnPrimary, padding: "16px 30px", boxShadow: "0 0 40px rgba(59,130,246,.35)" }} />
            <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" style={{ ...btnGhost, padding: "16px 26px" }}>View source</a>
          </div>
          <p data-reveal data-reveal-delay="240" style={{ ...mono, fontSize: "12.5px", letterSpacing: ".05em", color: "var(--muted)", margin: "20px 0 4px" }}>
            Android 7.0+ · ~16 MB · No account, no tracking
          </p>
          <p data-reveal style={{ ...mono, fontSize: "11px", letterSpacing: ".04em", color: "rgba(166,186,184,.5)", margin: 0 }}>
            v{VERSION} · Play Store listing coming soon
          </p>
        </div>

      </section>

      {/* ══════════════════════════════════════════════════════════════════
          FOOTER · full-width
      ═══════════════════════════════════════════════════════════════════ */}
      <footer style={{ position: "relative", zIndex: 1, width: "100%", borderTop: "1px solid var(--line)", background: "rgba(5,8,10,.55)", padding: "34px clamp(20px,5vw,56px)" }}>
        <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", justifyContent: "space-between", gap: "16px 28px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "11px", ...heading, fontWeight: 600, fontSize: "16px" }}>
            <span style={{ width: "9px", height: "9px", borderRadius: "50%", background: "var(--accent)", boxShadow: "0 0 12px var(--accent)" }} />
            Beacon
            <span style={{ ...mono, fontWeight: 400, fontSize: "12.5px", color: "var(--muted)", marginLeft: "2px" }}>
              Built by{" "}
              <a href="https://jagadee.sh" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", textDecoration: "underline", textDecorationColor: "rgba(166,186,184,.35)", textUnderlineOffset: "3px" }}>Jagadee.sh</a>
            </span>
          </div>
          <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: "6px 14px", ...mono, fontSize: "12px", letterSpacing: ".04em", color: "var(--muted)" }}>
            <span>
              Born in{" "}
              <a href="https://en.wikipedia.org/wiki/Yercaud" target="_blank" rel="noopener noreferrer" style={{ color: "var(--accent)", textDecoration: "none" }}>Yercaud</a>
              {" "}· Shipped from Chennai
            </span>
            <span aria-hidden="true" style={{ opacity: 0.35 }}>·</span>
            <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" style={{ color: "var(--accent)", textDecoration: "none" }}>Open source on GitHub</a>
          </div>
        </div>
      </footer>

    </div>
  );
}
