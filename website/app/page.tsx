import Link from "next/link";

const DOWNLOAD_URL = "https://github.com/jaga3421/Beacon/releases/latest";

/* ─── WiFi Meter SVG icon ────────────────────────────────────────────────── */
function WifiMeterIcon({ size = 80, className = "" }: { size?: number; className?: string }) {
  const cx = 54, cy = 72;
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 108 108"
      fill="none"
      className={className}
      aria-hidden="true"
    >
      {/* outer arc */}
      <path d="M 18,55 A 40,40 0 0 1 90,55" stroke="#5B8CFF" strokeWidth="5.5" strokeLinecap="round" />
      {/* middle arc */}
      <path d="M 29,60 A 28,28 0 0 1 79,60" stroke="#5B8CFF" strokeWidth="5.5" strokeLinecap="round" />
      {/* inner arc */}
      <path d="M 40,65 A 16,16 0 0 1 68,65" stroke="#5B8CFF" strokeWidth="5.5" strokeLinecap="round" />
      {/* needle */}
      <path d="M 54,72 L 78,48" stroke="#22d3ee" strokeWidth="5.5" strokeLinecap="round" />
      {/* pivot dot */}
      <circle cx={cx} cy={cy} r="4.5" fill="#5B8CFF" />
    </svg>
  );
}

/* ─── Animated radar rings ───────────────────────────────────────────────── */
function RadarPulse() {
  return (
    <div className="relative flex items-center justify-center w-48 h-48 mx-auto mb-10">
      <span className="absolute inset-0 rounded-full border border-[#5B8CFF]/30 pulse-ring" />
      <span className="absolute inset-0 rounded-full border border-[#5B8CFF]/20 pulse-ring-2" />
      <span className="absolute inset-0 rounded-full border border-[#5B8CFF]/10 pulse-ring-3" />
      <div className="relative z-10 bg-[#0f1117] rounded-full p-6 ring-1 ring-[#5B8CFF]/20">
        <WifiMeterIcon size={80} />
      </div>
    </div>
  );
}

/* ─── Feature card ───────────────────────────────────────────────────────── */
function FeatureCard({
  emoji,
  title,
  body,
}: {
  emoji: string;
  title: string;
  body: string;
}) {
  return (
    <div className="rounded-2xl bg-[#0f1117] border border-white/[0.06] p-6 flex flex-col gap-3">
      <span className="text-3xl" role="img" aria-label={title}>{emoji}</span>
      <h3 className="font-semibold text-lg text-white">{title}</h3>
      <p className="text-[#71717a] text-sm leading-relaxed">{body}</p>
    </div>
  );
}

/* ─── Page ───────────────────────────────────────────────────────────────── */
export default function HomePage() {
  return (
    <main>

      {/* ══════════════════════════════════════════════════════════════════
          FOLD 1 — HERO
          "WiFi. No ads. No paywall."
      ═══════════════════════════════════════════════════════════════════ */}
      <section
        className="grid-bg relative overflow-hidden min-h-screen flex flex-col items-center justify-center text-center px-6 py-24"
        aria-label="Hero"
      >
        {/* Radial glow */}
        <div
          className="pointer-events-none absolute inset-0 flex items-center justify-center"
          aria-hidden="true"
        >
          <div className="w-[600px] h-[600px] rounded-full bg-[#5b8cff]/5 blur-3xl" />
        </div>

        <div className="relative z-10 max-w-2xl mx-auto">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 rounded-full border border-[#5B8CFF]/30 bg-[#5B8CFF]/10 px-4 py-1.5 text-xs font-medium text-[#5B8CFF] mb-8">
            <span className="inline-block w-1.5 h-1.5 rounded-full bg-[#5B8CFF] animate-pulse" />
            Free · Open Source · No Ads
          </div>

          {/* Animated icon */}
          <RadarPulse />

          {/* Headline */}
          <h1 className="text-5xl sm:text-6xl font-bold tracking-tight leading-[1.1] mb-5">
            Your WiFi.
            <br />
            <span className="text-[#5B8CFF]">Finally&nbsp;clear.</span>
          </h1>

          <p className="text-lg text-[#a1a1aa] max-w-md mx-auto mb-10 leading-relaxed">
            Beacon shows signal strength, channel congestion, dead zones, and smart fixes —
            in real time, with <strong className="text-white font-semibold">zero ads</strong> and{" "}
            <strong className="text-white font-semibold">no paywall</strong>.
          </p>

          {/* CTA */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
              href={DOWNLOAD_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-3 rounded-2xl bg-[#5B8CFF] hover:bg-[#4a7aee] active:scale-95 transition-all px-8 py-4 font-semibold text-black text-base shadow-[0_0_32px_rgba(91,140,255,0.3)]"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M17.523 15.341l-.003-.002A5.998 5.998 0 0 0 18 13c0-1.933-.917-3.65-2.34-4.75L17.524 6.4A8.002 8.002 0 0 1 20 13a8.002 8.002 0 0 1-2.477 5.74l-1.84-1.84.84-.56ZM4 12a8.002 8.002 0 0 1 2.477-5.74l1.84 1.84A5.998 5.998 0 0 0 6 13c0 1.933.917 3.65 2.34 4.75L6.476 19.6A8.002 8.002 0 0 1 4 13v-1Zm8 2a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"/>
              </svg>
              Download for Android
            </a>
            <a
              href="https://github.com/jaga3421/Beacon"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-2xl border border-white/10 hover:border-white/20 px-8 py-4 text-sm font-medium text-[#a1a1aa] hover:text-white transition-colors"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844a9.59 9.59 0 0 1 2.504.337c1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.02 10.02 0 0 0 22 12.017C22 6.484 17.522 2 12 2Z"/>
              </svg>
              View source
            </a>
          </div>

          <p className="mt-6 text-xs text-[#52525b]">Android 7.0+ · ~16 MB · No account required</p>
        </div>

        {/* Scroll hint */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2 text-[#52525b] text-xs">
          <span>scroll</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <path d="M12 5v14M5 12l7 7 7-7" />
          </svg>
        </div>
      </section>


      {/* ══════════════════════════════════════════════════════════════════
          FOLD 2 — THE REAL STORY
          "I was in Yercaud…"
      ═══════════════════════════════════════════════════════════════════ */}
      <section
        className="relative px-6 py-28 sm:py-36"
        aria-label="Origin story"
        id="story"
      >
        {/* Left accent line */}
        <div className="pointer-events-none absolute left-0 top-0 h-full w-px bg-gradient-to-b from-transparent via-[#5B8CFF]/20 to-transparent" aria-hidden="true" />

        <div className="max-w-2xl mx-auto">
          <p className="text-xs font-semibold uppercase tracking-widest text-[#5B8CFF] mb-6">
            Why I built this
          </p>

          <h2 className="text-4xl sm:text-5xl font-bold leading-[1.15] mb-10 text-white">
            I was in Yercaud.<br />
            Looking for a good WiFi spot.
          </h2>

          <div className="space-y-6 text-[#a1a1aa] text-lg leading-relaxed">
            <p>
              Every app in the Play Store was the same. Open it up, and you're hit with a{" "}
              <span className="text-white font-medium">full-screen ad</span>. Dismiss the ad,
              and the signal bar is blurred — "upgrade to see real values". Tap upgrade, and
              it's ₹500 a year for what should be a simple number.
            </p>
            <p>
              I just wanted to know{" "}
              <strong className="text-white font-semibold">which channel was least crowded</strong>{" "}
              so I could sit somewhere that actually worked. That's it.
            </p>
            <p>
              So I built Beacon over a weekend. It shows everything — signal strength, frequency,
              channel overlap, co-channel neighbours, dead zones — with no ads and no paywall.
              It always will be.
            </p>
          </div>

          {/* Pull quote */}
          <blockquote className="mt-12 border-l-2 border-[#5B8CFF] pl-6">
            <p className="text-2xl font-semibold text-white leading-snug">
              "The data is yours. You shouldn't have to pay to see it."
            </p>
          </blockquote>

          {/* Mini map/location badge */}
          <div className="mt-10 inline-flex items-center gap-3 rounded-xl bg-[#0f1117] border border-white/[0.06] px-5 py-3 text-sm text-[#71717a]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#5B8CFF" strokeWidth="2" aria-hidden="true">
              <path d="M12 2C8.134 2 5 5.134 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.866-3.134-7-7-7Z"/>
              <circle cx="12" cy="9" r="2.5"/>
            </svg>
            Born in Yercaud, Tamil Nadu · shipped from Bangalore
          </div>
        </div>
      </section>


      {/* ══════════════════════════════════════════════════════════════════
          FOLD 3 — WHAT IT DOES + FINAL CTA
          Four honest features, then download
      ═══════════════════════════════════════════════════════════════════ */}
      <section
        className="px-6 py-28 sm:py-36 border-t border-white/[0.04]"
        aria-label="Features"
        id="features"
      >
        <div className="max-w-4xl mx-auto">
          <p className="text-xs font-semibold uppercase tracking-widest text-[#5B8CFF] mb-4 text-center">
            What you get
          </p>
          <h2 className="text-4xl sm:text-5xl font-bold text-center mb-4">
            Everything. For free.
          </h2>
          <p className="text-[#71717a] text-center mb-14 max-w-md mx-auto">
            No "pro" version. No features held back. No timer on your scan.
          </p>

          {/* Feature grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-20">
            <FeatureCard
              emoji="📡"
              title="Live signal scan"
              body="RSSI, frequency, channel, link speed, and band — updated every 1.5 seconds. See the full picture of every network around you, not just your own."
            />
            <FeatureCard
              emoji="🗺️"
              title="Dead zone heatmap"
              body="Walk around your space and watch the signal heatmap build in real time. Find the exact corner where it drops — then fix it."
            />
            <FeatureCard
              emoji="🔧"
              title="Smart recommendations"
              body="Beacon analyses your channel neighbours and gives you specific actions: switch to channel 11, move the router, or change your band. Not generic tips — your actual situation."
            />
            <FeatureCard
              emoji="🚫"
              title="Zero ads. Zero paywall."
              body="No banners, no interstitials, no blurred values behind a subscription. Download it, use it, that's it. It's open source — you can read every line."
            />
          </div>

          {/* Final CTA */}
          <div className="rounded-3xl bg-[#0f1117] border border-white/[0.06] p-10 sm:p-14 flex flex-col items-center text-center relative overflow-hidden">
            {/* Glow */}
            <div className="pointer-events-none absolute inset-0 flex items-center justify-center" aria-hidden="true">
              <div className="w-80 h-80 rounded-full bg-[#5b8cff]/8 blur-3xl" />
            </div>

            <WifiMeterIcon size={64} className="relative z-10 mb-6" />

            <h2 className="relative z-10 text-3xl sm:text-4xl font-bold mb-4">
              Give it a try.
            </h2>
            <p className="relative z-10 text-[#71717a] mb-8 max-w-sm leading-relaxed">
              Sideload the APK, scan your networks, and tell me what you think.
              It takes 30 seconds.
            </p>

            <a
              href={DOWNLOAD_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="relative z-10 inline-flex items-center gap-3 rounded-2xl bg-[#5B8CFF] hover:bg-[#4a7aee] active:scale-95 transition-all px-8 py-4 font-semibold text-black text-base shadow-[0_0_40px_rgba(91,140,255,0.25)]"
            >
              Download Beacon — it&apos;s free
            </a>

            <p className="relative z-10 mt-5 text-xs text-[#52525b]">
              Android 7.0 · ~16 MB · No account, no tracking, no nonsense
            </p>
          </div>
        </div>
      </section>


      {/* ── Footer ──────────────────────────────────────────────────────── */}
      <footer className="border-t border-white/[0.04] px-6 py-10 text-center text-xs text-[#52525b]">
        <div className="flex items-center justify-center gap-1.5 mb-3">
          <WifiMeterIcon size={18} />
          <span className="font-semibold text-[#71717a]">Beacon</span>
        </div>
        <p>
          Built by Jagadeesh ·{" "}
          <a
            href="https://github.com/jaga3421/Beacon"
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-white transition-colors underline underline-offset-2"
          >
            Open source on GitHub
          </a>
        </p>
        <p className="mt-2">Free forever. No ads. No account. No excuses.</p>
      </footer>

    </main>
  );
}
