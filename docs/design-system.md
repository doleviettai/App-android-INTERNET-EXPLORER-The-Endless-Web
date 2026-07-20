# Internet Explorer — Android UI Design Skill
### "Terminal Noir" Visual Design System — Extended Edition

> This is a standalone design skill reference for the Android client, written in English so it travels well regardless of who's building — while all in-app copy the player actually reads (website content, emails, NPC dialogue, puzzle text) stays in Vietnamese. Use this document as the single source of truth when building any new screen or component: if a choice isn't covered here, derive it from Section 1 (Philosophy) and Section 2 (Signature System) rather than defaulting to Material.

---

## 1. Design Philosophy & Mood

**Core feeling:** the quiet, cold hum of a data center at 3AM. A terminal window your uncle really didn't want you finding. System logs nobody meant to be read by a human. Mystery here is produced by **restraint** — large areas of near-black, very little ornament, information presented flatly and without persuasion — not by loud effects stacked on top of each other.

**Grounding in the subject.** The game's actual lore gives the design system a real organizing idea, not just a color scheme: NEXUS generates most of the Internet the player explores, but a small amount of content is *human, verified, original* — old forum posts, personal letters, real access logs. The interface should let a careful player **feel** the difference between machine-generated noise and verified signal, even before they know the story reason why. This single idea (Section 3–4) is what should keep every future screen from drifting into "generic dark app."

**Mood keywords (for calibrating any new screen):** clinical, patient, unhurried, deniable, forensic, nocturnal. **Avoid:** playful, warm, reassuring, decorative, loud.

---

## 2. Signature System (the one thing this app should be remembered for)

Per standard design practice, spend the app's "boldness budget" in one coherent, load-bearing place rather than scattering small flourishes everywhere. Here, that place is a mechanic-driven visual system called **Integrity Static**, plus one strong first-impression moment.

### 2.1 Integrity Static (ties visuals directly to gameplay stakes)

As the player advances deeper into a case and uncovers more corrupted / dangerous / NEXUS-obscured information, a very faint grain and flicker texture gradually increases across the **entire UI** — not just the current screen. Early in a case it's nearly imperceptible (matches the baseline scanline overlay). By the case's climax it should be clearly, uncomfortably present.

- Implementation: a `staticIntensity: Float` (0.0–1.0) driven by `PlayerProgressEntity.completedSteps / totalSteps` for the active case, applied as: scanline opacity 4% → 9%, occasional 1-frame glitch flicker frequency increases, and a barely-audible static swell if sound is enabled.
- Why it matters: this is diegetic feedback — it tells the player "you are getting close to something NEXUS doesn't want found" without a single line of UI copy, and it gives the aesthetic a *reason to exist* beyond decoration.

### 2.2 Boot Sequence (first-impression signature moment)

On cold app start, before the Home screen, show 2–3 seconds of simulated system boot text (monospace, typewriter-paced, skippable by tap):

```
> INIT EXPLORER_SESSION...
> HANDSHAKE nexus.core [OK]
> ACCESS LEVEL: UNVERIFIED
> WARNING: connection not authorized by NEXUS
> proceeding anyway...
```

This costs very little to build (it's the same typewriter primitive used everywhere else) but does a lot of work establishing tone in the first three seconds — the exact kind of "one real risk, executed with restraint" the philosophy calls for.

### 2.3 Signal vs. Noise (color + type differentiation)

Two content categories get consistently different treatment everywhere in the app:

| Category | Meaning | Visual treatment |
|---|---|---|
| **Signal** | Verified, human-authored, primary-source (personal letters, direct quotes, confirmed log lines) | `text.primary` (phosphor green), no distortion, typewriter reveal always completes cleanly |
| **Noise** | NEXUS-generated / unverified / background filler | `text.noise` (desaturated cool gray, see 3.2), occasional single-character flicker mid-reveal, small `[UNVERIFIED]` tag |

This turns "which website is trustworthy?" from a narrative-only question into something the player can start to *feel* visually — a direct, mechanic-grounded payoff for the whole aesthetic.

---

## 3. Color System

### 3.1 Core tokens

| Token | Hex | Use |
|---|---|---|
| `bg.primary` | `#080A09` | Base background |
| `bg.surface` | `#0F1412` | Cards / panels, one step up |
| `bg.surface.raised` | `#161C19` | Rare third elevation step — modals, active input focus |
| `text.primary` | `#C8FFC0` | Primary / "signal" text |
| `text.noise` | `#8FA69C` | Desaturated — NEXUS-generated / unverified content (see 2.3) |
| `text.muted` | `#5C6B63` | Decorative metadata only — see contrast note below |
| `text.muted.readable` | `#7C8D84` | Muted text the player must actually read |
| `accent.terminal` | `#39FF88` | Cursor, active borders, "authenticated" state |
| `accent.amber` | `#FFB000` | Mild warning, unread indicator, "unverified" tag |
| `accent.glitch` | `#FF3B4E` | Anomalies, danger, corrupted data — budget: at most one glitch-colored element visible per screen |
| `border.ascii` | `#2A332E` | Default borders |
| `border.active` | `#39FF88` | Focused / selected borders |

### 3.2 Elevation without shadows

Box-shadow and Material elevation are disallowed by the anti-pattern list (Section 14). Depth is instead conveyed purely through **background luminance steps** and **border brightness**:

| Level | Background | Border | Use |
|---|---|---|---|
| Surface 0 | `bg.primary` | — | App background |
| Surface 1 | `bg.surface` | `border.ascii` | Bracket-box cards, list rows |
| Surface 2 | `bg.surface.raised` | `border.active` | Modal sheets, focused input, active puzzle line |

### 3.3 Accessibility — contrast

Measured against `bg.primary` (#080A09):

- `text.primary` (#C8FFC0): **~17.5:1** — comfortably exceeds WCAG AAA for body text.
- `text.muted` (#5C6B63): **~3.5:1** — below AA (4.5:1) for normal-size text. Reserve strictly for small decorative metadata (timestamps, ref IDs) the player doesn't need to read to progress.
- `text.muted.readable` (#7C8D84): **~5.7:1** — use this instead whenever muted-styled text is something the player actually needs to read.
- `accent.terminal` / `accent.glitch`: both bright against near-black, comfortably pass AA — no adjustment needed.

### 3.4 Color budget rule

No more than one `accent.glitch` element on screen at a time. If everything is flagged as dangerous, nothing reads as dangerous — this color's entire value depends on scarcity.

---

## 4. Typography

**One family, used for everything:** JetBrains Mono / Space Mono / IBM Plex Mono (pick one, don't mix). This is a deliberate constraint, not a limitation — every string in the app, including long-form article content, should read as "system data," which is what makes the eventual Signal/Noise distinction (2.3) legible without needing a second typeface.

### 4.1 Type scale

| Role | Size | Weight | Tracking | Use |
|---|---|---|---|---|
| Display | 28sp | Bold | 1.5sp | Boot sequence, case-closed moments only |
| Title | 20sp | Semibold | 1.2sp | Screen headers, entity names |
| Body | 15sp | Regular | 0.2sp | Primary reading content |
| Body — noise | 15sp | Regular | 0.2sp | Same size as body; distinguished only by `text.noise` color + `[UNVERIFIED]` tag, never by size, so it doesn't read as "less important," only "less certain" |
| Label | 11sp | Medium | 1.5sp, uppercase | Eyebrows, nav items, tags |
| Caption | 9sp | Regular | 1sp | Timestamps, ref IDs |
| Code / log | 11sp | Regular, tabular figures | 0 | Puzzle log lines — figures must align in columns |

### 4.2 Rhythm

Line height 1.5–1.7× for body text (monospace needs more breathing room than proportional fonts to stay readable at length). Paragraph spacing = one full line height, never half.

---

## 5. Spacing & Layout Grid

4dp base unit. Scale: **4 / 8 / 12 / 16 / 24 / 32 / 48dp**. Don't invent intermediate values.

- Screen horizontal margin: 16dp
- Card (`bracket-box`) internal padding: 16dp top/sides, 14dp bottom
- Gap between stacked cards: 14dp
- List row vertical padding: 10dp
- Bottom nav height: 56dp + safe-area inset

---

## 6. Iconography

24dp grid, 1.25dp stroke, no fill, no rounded caps — wireframe/schematic character, not default Material rounded icons.

**Required icon set (minimum for MVP):**

| Icon | Used for |
|---|---|
| Connection status (dot + radiating lines) | Address bar `[CONNECTED]` state |
| Lock (open / closed) | Access level indicators |
| Chain-link | Hyperlinks within content |
| Prompt caret `>` | Terminal-style inputs |
| Envelope | Inbox |
| Notebook / ruled page | Notebook |
| Scan / crosshair | Puzzle / Log Analysis |
| Warning triangle | Amber-level warnings |
| Flag | Anomaly marker in logs |
| Clock | Timestamps |
| Person outline | NPC identity |
| Corrupted-file (page with static lines) | Virus/corrupted content, Phase 2 |
| Checkmark in box | Verified / signal content |
| Gear | Settings |

---

## 7. Motion System

All motion is **decisive and linear** — no spring/bounce easing anywhere in the app. Motion should read as mechanical, not playful.

| Interaction | Duration | Curve | Notes |
|---|---|---|---|
| Typewriter character reveal | 12–16ms / character | linear | Cursor blink independent, 500ms on/off |
| Screen transition | 180ms | ease-out, cut/fade only | Never slide — this isn't a "swipe between cards" app |
| Boot sequence lines | 250ms / line, 400ms hold after each | linear | Skippable by tap |
| Glitch flicker (anomaly reveal) | 80ms, single invert-filter pulse | steps(2) | Rare — see color budget rule, 3.4 |
| Button press feedback | 80ms opacity 1→0.7→1 | linear | No scale-bounce |
| Integrity Static ramp (2.1) | continuous, tied to progress | — | Changes are gradual across a whole session, never a sudden jump |
| Loading | ASCII bar increment, ~1 block / 400ms | — | Never a spinner |

**Reduced motion:** every row above must degrade gracefully when the system "Remove animations" accessibility setting is on — typewriter reveals full text instantly, glitch flicker becomes a static color change with no filter animation, screen transitions become instant cuts.

---

## 8. Component Specifications

- **Address bar** — 1dp `border.ascii`, 8dp/10dp padding, command-style content left, status tag right (`[CONNECTED]` in `accent.terminal`, `[UNVERIFIED]` in `accent.amber`).
- **Bracket-box card** — Surface 1, 1dp `border.ascii`, four corner glyphs (`┌┐└┘`) as absolutely-positioned 12sp elements at each corner. Focused/expanded state promotes to Surface 2 with `border.active`.
- **Log line (puzzle)** — default: `text.muted.readable` body, `border.ascii` bottom divider. Hover/press: 5% `accent.terminal` background wash. Selected: `accent.amber` text + background wash — **never rely on color alone**; also underline the timestamp so the state is visible to colorblind players (see 9.3).
- **List row (inbox/notebook)** — unread: `text.primary` subject + small `accent.amber` dot; read: `text.muted.readable` subject, no dot.
- **Bottom nav** — inactive `text.muted`, active `accent.terminal` + 1dp top border in `accent.terminal`. No icons-only nav — always pair with the terminal-style bracket labels already established (`[BROWSER]` etc. or plain caps label, pick one convention and hold it everywhere).
- **Button (primary)** — outlined only, never filled: 1dp `accent.terminal` border, transparent background, `accent.terminal` label. Press state: 8% background wash, no scale change.
- **Status tag / badge** — 1dp border in the relevant semantic color, 2dp/6dp padding, 9sp uppercase label, never filled background.
- **Empty state** — reads as a system message, not an illustration: `NO ENTRIES FOUND.` in `text.muted.readable`, optionally one line of flavor text in `text.noise`. No mascot, no friendly copy.
- **Error state** — `CONNECTION LOST. RETRY.` pattern — states what happened and what to do, in the interface's voice (Section 11), never apologetic.

---

## 9. Accessibility & Quality Floor

1. **Contrast** — see Section 3.3. Default to `text.primary` or `text.muted.readable` for anything the player must read; `text.muted` is decoration-only.
2. **Touch targets** — minimum 48dp, even where the visual element (e.g. a log line) is visually smaller — pad the tap target invisibly.
3. **Colorblind-safe signaling** — roughly 1 in 12 men have red-green color vision deficiency, which directly collides with this palette's green/red accent pair. Every state that currently relies on `accent.glitch` vs `accent.terminal` alone must also carry a non-color cue: an icon, an underline, or a text tag (`[ANOMALY]` / `[OK]`), never color alone.
4. **Focus states** — since the whole system avoids Material's default focus ring, define one explicitly: 2dp `accent.terminal` outline, 2dp offset, visible for keyboard/switch-access navigation.
5. **Reduced motion** — see Section 7's table; every animated pattern needs a defined static fallback, not just "turn off animation" as an afterthought.
6. **Font scaling** — layouts must not clip or overlap at 200% system font scale; test bracket-box cards and the bottom nav labels specifically, since they're the tightest-fitting components.

---

## 10. Voice & Writing System

Words are part of the design system, not an afterthought layered in later.

- **System-chrome text** (buttons, labels, empty/error states) is cold, precise, sentence case unless it's a fixed terminal-style token: *"CONNECTION LOST. RETRY."* not *"Oops! We lost connection, sorry about that."* No exclamation marks anywhere in system chrome.
- **Terminal tokens stay in English by convention** (`[CONNECTED]`, `> `, `YOU>` / `LENA_T>`) even though the app's narrative language is Vietnamese — this mirrors how real systems mix machine-English with a human's own language, and should be treated as an intentional, permanent convention rather than an inconsistency to "fix."
- **Narrative/lore content** (website articles, NPC dialogue, forum posts) can be more literary and atmospheric, written in Vietnamese — this is where the game's actual voice lives.
- **Every control keeps its name through the whole flow** — a button that says `SUBMIT` should never be followed by a confirmation that calls the action something else.

---

## 11. Sound Design (optional, later phase)

- Keystroke tick during typewriter reveal: very low level (~‑24dB), high-pass filtered so it reads as texture, not a notification sound.
- Static burst on screen transition: ~150ms, tied to Integrity Static intensity (2.1) — louder and grittier later in a case.
- Optional ambient low hum loop, off by default.
- All sound fully toggleable in Settings and must respect system silent/DND mode.

---

## 12. Screen-by-Screen Deep Dive

| Screen | Layout notes | States to design |
|---|---|---|
| **Boot / Splash** (2.2) | Full-bleed black, centered left-aligned boot text, skip-on-tap | first-launch only vs. every cold start (decide before Phase 1) |
| **Onboarding** | Not a colorful carousel — a "briefing file": single bracket-box "dossier" with typewriter-revealed paragraphs (who NEXUS is, who the player is), ASCII progress (`[██░░░] 2/4`) instead of dot indicators, page transitions use boot-sequence line timing rather than horizontal swipe | first line, mid-briefing, final page with `ACCEPT ACCESS` terminal button → writes completion flag |
| **Home** | Session dashboard, lands here after Onboarding or on returning cold starts: current case status (mirrors Notebook %), a short "directory listing" of shortcuts styled like `> ls active_case/` into Browser / Inbox / Notebook / Puzzle, unread count | no active case yet (empty state), case in progress, case complete |
| **Browser** | Address bar → stacked bracket-box cards → meta row | loading (ASCII bar), signal content, noise content (2.3), anomaly (glitch flicker once) |
| **Inbox** | Address bar (`mailbox --inbox`) → list rows | empty, unread badge, read |
| **Notebook** | Address bar with % complete → hypothesis bracket-box → checklist rows | in-progress, case-complete (celebratory but still restrained — no confetti, just a clean `[CASE CLOSED]` tag) |
| **Puzzle (Log Analysis)** | Address bar → log lines → submit row | default, selected, correct (`accent.terminal` feedback), incorrect (glitch flicker once, `accent.glitch` feedback) |
| **NPC Chat** † | Address-bar-style header with NPC name → message list with `YOU>` / `<NPC_ID>>` prefixes → input | typing indicator as `...` typewriter dots, not a bubble animation |

† Not yet scaffolded as its own `feature/` package in the current project structure — spec kept here for when it's built; until then, NPC replies can render inline inside Browser or Notebook using the same `YOU>` / `<NPC_ID>>` prefix convention.

---

## 13. Quick Reference Checklist

Before shipping any new screen, confirm:

- [ ] Uses only tokens from Section 3 — no new hex values invented ad hoc
- [ ] No box-shadow / Material elevation — depth via Surface 0/1/2 only
- [ ] No more than one `accent.glitch` element visible at once
- [ ] Signal vs. Noise content is distinguishable without relying on the player having read this document
- [ ] Every color-coded state also has a non-color cue (icon/text/underline)
- [ ] Motion durations pulled from Section 7's table, not improvised
- [ ] System-chrome copy follows Section 11's voice (no exclamation marks, states what happened + what to do)
- [ ] Reduced-motion fallback defined
- [ ] Touch targets ≥ 48dp

---

## 14. Anti-Patterns to Avoid

- Default Material 3 rounded cards, visible drop shadows / elevation shadows
- Pastel colors, multi-color gradients, 3D or filled icons
- Loud, multi-color commercial "cyberpunk poster" neon — turns *mysterious and dangerous* into *fun and flashy*, the opposite of this game's tone
- Bounce/spring easing anywhere — motion should read as mechanical, not playful
- Color-only signaling for any state (accessibility failure + thematically weak — a real system wouldn't rely on color alone either)
- Mixing multiple font families "for variety" — the monospace-everywhere constraint is what makes Signal vs. Noise legible
- Friendly/apologetic copy in system chrome ("Oops!", "Great job!") — breaks the cold, clinical voice this whole system depends on

---

## 15. Project File Mapping (Your Codebase)

Matched against the actual `com.example.internet_explorer.app` structure. Keep this table current — it's the fastest way for anyone touching the project to know where a design decision is supposed to live.

| Design system content | Real file |
|---|---|
| Section 3 color tokens (`TerminalNoirColors`) | `app/ui/theme/Color.kt` |
| Section 4 type scale (`TerminalTypography`) | `app/ui/theme/Type.kt` |
| Theme wrapper (`InternetExplorerTheme`) | `app/ui/theme/Theme.kt` |
| Section 8 reusable composables — `AddressBar`, `BracketBox`, `LogLine`, `StatusTag`, `TerminalButton`, `AsciiProgressBar`, `ScanlineOverlay`, `TypewriterText`, `IntegrityStaticOverlay` | `app/ui/components/TerminalComponents.kt` |
| Section 2.1 Integrity Static intensity — should be computed from case progress | `app/data/repository/GameStateRepository.kt`, sourced from `app/data/local/CaseModel.kt` via `CaseAssetLoader` |
| Section 2.3 Signal vs. Noise — needs a field per content item so components know which treatment to render | add e.g. `isVerified: Boolean` (or a small `ContentOrigin { SIGNAL, NOISE }` enum) to `CaseModel.kt` |
| Section 12 screens | `app/feature/{browser, home, inbox, notebook, onboarding, puzzle}`, wired together in `app/navigation/Navgraph.kt` |

**One thing worth cleaning up:** the project currently has two theme locations — `app/ui/theme/{Color,Theme,Type}.kt` (the real one, referenced above) and a second `ui.theme` package at the top level with the same three filenames, which looks like a leftover from the default "Empty Compose Activity" template. Having two divergent theme sources is exactly the kind of drift the Section 13 checklist exists to prevent — worth deleting the unused one so there's only one place `TerminalNoirColors` can possibly live.

---

*This is the English design skill reference. All actual in-app copy remains in Vietnamese — only this specification document is in English. If a future screen needs a decision not covered here, derive it from Sections 1–2 before improvising.*