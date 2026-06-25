<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# Writing rules (guardrail)

These apply to every piece of user-facing copy in this site: page text,
metadata, the OG and Twitter cards, alt text, and the OG image.

## No em-dashes

Never use an em-dash (—). They are the clearest "written by AI" tell, so they
are banned here, including the HTML entities `&mdash;` and `&#8212;`. Reach for
a period, comma, colon, or parentheses instead, and rephrase so the sentence
reads naturally without the dash. This rule is enforced: `npm run check:copy`
fails the build (via the `prebuild` script) if an em-dash shows up under `app/`.

## Do not write copy that reads like AI

Keep the voice that is already here: first person, specific, and plain. The
Yercaud story in `app/page.tsx` is the reference for tone. Avoid the usual
generated-text tells:

- Stacked short fragments for rhythm ("No ads. No paywall. Always free.").
- "Not X, but Y" or "It's not just X, it's Y" contrast scaffolding.
- Empty intensifiers and filler: "completely", "seamlessly", "effortlessly",
  "in today's world", "whether you're A or B".
- Balanced triplets where one or two concrete points would land harder.
- Vague claims with no detail. Name the real number, place, or behaviour.

Before committing any copy change, run `npm run check:copy`.
