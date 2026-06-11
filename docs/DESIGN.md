# Design Documentation

UX principles, screens, interaction patterns, and visual language for
Mandarin Conversation Coach.

## 1. Design principles

1. **Low anxiety above all.** The product exists to remove the fear of looking
   foolish. Nothing interrupts the learner mid-thought; nothing grades them in
   public view; failure inside a mission is a story beat, not a red X.
2. **Adult, not childish.** No mascots, no cartoon badges, no confetti. The
   register is a sharp, warm conversation partner. Progress markers borrow
   from grown-up artifacts: a passport, an inventory, a week-over-week trend.
3. **Scaffolding that disappears.** Support is generous at the start and
   removed deliberately — and visibly — as confidence grows. The learner is
   always offered the next step, never shoved.
4. **The model is a character, the coach is a whisper.** In missions the AI
   never breaks character; teaching happens in the margins (tips,
   corrections) in a visually quieter voice.
5. **Everything audible, everything inspectable.** Every Chinese sentence can
   be played (normal or slow) and every character can be tapped for a
   definition, in every mode.

## 2. Visual language

### Palette (`ui/theme/Color.kt`)

| Token | Value | Use |
|---|---|---|
| `ChineseRed` | `#C0392B` | Brand, primary actions, pinyin accents, section labels |
| `ChineseRedLight` / `ChineseRedDark` | `#E74C3C` / `#96281B` | Accents, listening state |
| `GoldAccent` | `#D4A017` | Passport stamps, earned inventory |
| `BackgroundWarm` | `#FAF9F7` | App background (warm paper, not clinical white) |
| `SurfaceWhite` / `SurfaceGray` | `#FFFFFF` / `#F0EEE9` | Cards, input surfaces, quiet chips |
| `TextPrimary` / `TextSecondary` / `TextHint` | `#1A1A1A` / `#6B6560` / `#ADADA8` | Type hierarchy |
| `PinyinColor` | `#C0392B` | Pinyin lines (italic) |
| `EnglishColor` | `#6B6560` | English translations (italic) |
| `TipColor` | `#2980B9` | Teaching tips |
| `UserPillColor` | `#2C3E50` | The learner's message pill |

### Typography

- **Hanzi is the hero:** serif family, larger sizes (22sp in messages, up to
  56sp in the dictionary sheet and home title) — calligraphic warmth against a
  sans UI.
- **Layered message anatomy** (top to bottom, descending visual weight):
  hanzi → pinyin (italic red) → English (italic gray) → correction (quiet gray
  card) → tip (💡 blue italic) → action buttons.
- Bilingual labels throughout: `English · 中文` ("Settings · 设置",
  "Real-World Missions · 实战任务").

### Iconography & symbols

Material icons for chrome; emoji as content symbols (level plants 🌱🌿🎋🏮,
mission emoji 🏨🍜🚕🍸💼, item emoji 🔑📶, moods 😊🙂😕😒🤩, streak 🔥). Emoji read as
adult shorthand here, not decoration — they carry meaning (mood, inventory).

## 3. Screen inventory & navigation

```
Home ──────────────┬─→ Conversation (level)                ─┐
  │                └─→ Conversation (level + mission)       ├─→ Settings
  ├─→ Fluency Passport                                      │
  └─→ Settings ←────────────────────────────────────────────┘
```

### Home

- App title (普通话 / Pǔtōnghuà) over a warm background.
- **Streak chip** (🔥 N) in the top bar; passport and settings icons.
- **Streak-break nudge**: a dismissible card with a witty line — appears only
  on the day a streak died, never nags again.
- **Level cards**: four cards with an intensity bar (red alpha ramps with
  level), hanzi + pinyin + description.
- **Missions row**: horizontally scrolling mission cards with emoji, title,
  description; a "Suggested today" badge (80/20 engine) floats one card to the
  front; completed missions wear a "🛂 Stamped" badge.

### Conversation

- Top bar: level identity (emoji, hanzi, pinyin), new-conversation, settings.
- Hint strip ("Tap any Chinese character…"), then the **mission HUD** when in
  a scenario: mission title, the character's current mood emoji, and item
  slots that flip from ❔ to "🔑 房卡" as they're earned.
- Message list: coach messages lead with the 明 avatar (red circle); learner
  messages are dark navy pills, right-aligned.
- **The input bar is the stage.** The bottom of the screen changes with the
  learner's bridge stage — see §4.
- Dialogs: stage-advance offer ("🎉 Ready to level up?" with accept/decline),
  mission completion (stamp, items recap, "Back to missions" / "Keep
  chatting"). Item earns surface as snackbar toasts.

### Fluency Passport

- Passport cover card: ChineseRed with gold serif 护照 / "FLUENCY PASSPORT".
- Stat cards: streak (current/longest), Vocabulary Wealth ("428 wealth from
  310 words" — score before count, with a one-line explanation), response
  speed (this week vs last week with ▼/▲ trend, phrased kindly in both
  directions).
- Stamp grid: two columns; earned stamps show the mission emoji, title, and
  date inside a gold border; unearned show 🔒 on a muted surface — visible
  goals, not hidden achievements.

### Settings

Sectioned cards: Profile · 个人资料, Appearance, Vocabulary (learning speed,
learned-word list), **Beginner Bridge · 入门阶梯** (Auto or pin any stage),
LLM Backend (provider, cost, reset), API Key, Speech Speed (🐢–🐇 slider with
test voice), Display (show/hide English), About.

## 4. Interaction patterns

### The four input bars (Beginner Bridge)

| Stage | Surface | Interaction |
|---|---|---|
| 🔁 Guided Choices | 2–3 stacked cards (hanzi/pinyin/English + 🔊 each) | Tap a card to send it. Listen before committing. |
| 🏗️ Word Building | Assembled row (red chips) over a shuffled bank (gray chips), Clear + Send | Tap bank chips into order; tap assembled chips to return them. Tap-to-order, not drag — proven, accessible, one-handed. |
| 🤝 Assisted Typing | Standard input + an "EN→中" assist chip | When the draft ends in an English word the chip activates; tapping splices the translation (hanzi) in place. |
| 🚀 Free Conversation | Mic + text field + send | Open conversation; mic pulses red while listening. |

Degradation rule: if the model doesn't supply choices or a word bank for a
turn, that turn silently falls back to the free input bar — the learner is
never blocked by a model hiccup.

### Corrections (the safety net)

Rendered as a quiet gray rounded card under the coach's reply:

> ✏️ You said 「我想要房间」 · locals say 「我要一个房间」
> *measure word needed*

Visually subordinate to the message itself — guidance available to those who
look, invisible pressure to those who don't.

### Missions (consequences, not grades)

The character's mood emoji is the "face" of the simulation: confuse the
waiter and the HUD shows 😕 while the story bends (wrong dish arrives).
Earning an item pops a chip into the HUD and a toast. Completion is
celebrated with the passport stamp — the reward vocabulary is *certification*,
not *points*.

### Voice

- Coach replies auto-play on arrival.
- Per-message 🔊 normal and 🐢 slow buttons (slow = 60% of the user's chosen
  rate, no pitch distortion).
- Global rate slider 50–150% in Settings, default 85%.

### Progression moments

- Stage advancement is an *offer* dialog after sustained success; declining
  pins the current stage (respect for autonomy — the app never silently moves
  the learner).
- The daily Curveball opens the first session of the day in-conversation; no
  special UI, the surprise *is* the content.

## 5. Tone of voice

- Coach persona: 小明 — warm, patient, celebrates progress without gushing.
- System copy: bilingual, concise, a little wry where stakes are low
  ("Roleplay it like it's real — earn the room key, not a gold star").
- Streak nudges: adult-to-adult, self-aware humor; never shame ("Your streak
  ghosted you — but unlike most ghosts, this one answers texts.").
- Trend copy never scolds: slower week-over-week reads "probably tackling
  harder ground."

## 6. Accessibility & inclusivity notes

- All interactive elements have content descriptions; choice options are
  audible before selection (supports listening-first learners).
- English translations can be globally hidden (immersion) or shown (support).
- Tap targets ≥ 44dp on primary actions; word-bank chips padded for touch.
- Slow-speech mode is a first-class, always-visible control, not buried in
  settings.

## 7. Design backlog

- Drag-and-drop word bank (tap-to-order shipped first as the proven pattern).
- Avatar artwork for scenario characters (mood currently expressed via emoji).
- Passport visual richness: stamp rotation/ink textures.
- Dark theme polish (theme toggle exists; palette tuning pending).
- Inventory persistence surfaced outside missions (a "things you've earned"
  shelf on the passport).
