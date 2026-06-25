#!/usr/bin/env node
/*
 * Copy guardrail.
 *
 * Em-dashes are one of the clearest "written by a language model" tells, so
 * this site does not use them. This check scans the site source and fails the
 * build if an em-dash (or its HTML entity) slips into any copy. Rephrase with a
 * period, comma, colon, or parentheses instead. See AGENTS.md for the full
 * writing rules.
 *
 * Run on its own with `npm run check:copy`; also runs automatically before
 * every `npm run build` via the `prebuild` script.
 */

import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, extname, relative } from "node:path";
import { fileURLToPath } from "node:url";

const here = fileURLToPath(new URL(".", import.meta.url));
const root = join(here, "..");

// Directories that hold user-facing copy.
const SCAN_DIRS = ["app"];
const EXTS = new Set([".ts", ".tsx", ".js", ".jsx", ".mjs", ".css", ".md", ".mdx"]);

// U+2014 em dash, plus the HTML entities that render as one.
const PATTERNS = [
  { label: "em-dash (—)", re: /—/ },
  { label: "&mdash;", re: /&mdash;/i },
  { label: "&#8212;", re: /&#8212;/ },
  { label: "&#x2014;", re: /&#x2014;/i },
];

function walk(dir) {
  const files = [];
  for (const name of readdirSync(dir)) {
    const full = join(dir, name);
    if (statSync(full).isDirectory()) files.push(...walk(full));
    else if (EXTS.has(extname(full))) files.push(full);
  }
  return files;
}

const hits = [];
for (const dir of SCAN_DIRS) {
  for (const file of walk(join(root, dir))) {
    const lines = readFileSync(file, "utf8").split("\n");
    lines.forEach((line, i) => {
      for (const { label, re } of PATTERNS) {
        if (re.test(line)) {
          hits.push({ file: relative(root, file), line: i + 1, label, text: line.trim() });
        }
      }
    });
  }
}

if (hits.length > 0) {
  console.error("✖ Copy check failed: em-dashes are not allowed in website copy.\n");
  for (const h of hits) {
    console.error(`  ${h.file}:${h.line}  [${h.label}]  ${h.text}`);
  }
  console.error(
    `\n${hits.length} occurrence(s) found. Use a period, comma, colon, or parentheses ` +
      `instead, and rephrase so it reads naturally. See website/AGENTS.md.`,
  );
  process.exit(1);
}

console.log("✔ Copy check passed: no em-dashes found.");
