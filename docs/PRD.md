# Product Requirements Document

**Product:** Mandarin Conversation Coach (普通话)
**Target market:** Adult language learners (21–50+)
**Status:** Core features shipped — see [implementation status](#7-implementation-status)

---

## 1. Product vision & strategy

### 1.1 Core value proposition

A conversational Mandarin learning app for adults that uses an LLM as an
adaptive, low-anxiety sparring partner. The product attacks the single biggest
barrier to adult language acquisition: the social anxiety and fear of looking
foolish when speaking. By replacing human judgment with an intelligent,
responsive simulation, users build immediate, practical speaking confidence.

### 1.2 Target audience

- **The persona:** busy, career-driven professionals, expats, and culturally
  curious adults who need high-utility fluency and have no patience for
  academic drills.
- **The vibe:** energetic, contemporary, culturally sharp — a conversation with
  an interesting stranger at a trendy cocktail bar, a local pub, or a casual
  networking event.
- **Anti-goals:** zero childish themes (no talking animals, no cartoon badges,
  no playground vocabulary). Zero sterile textbook drills (no rote conjugation
  tables).

---

## 2. Core feature specifications

### 2.1 The 4-Step Beginner Bridge ("Noob to Native")

To prevent complete beginners from freezing at an open input box, the UX
implements a conversation ladder that removes scaffolding as confidence grows:

```
[ 🔁 Step 1: Passive Input ] → [ 🏗️ Step 2: Fragments ] → [ 🤝 Step 3: Scaffolded ] → [ 🚀 Step 4: Free-Flow ]
  Tap curated responses         Order word-bank chips       Type with inline help      Open text & voice chat
```

1. **Passive Input & Mimicry** — the LLM initiates dialogue from
   adult scenarios; the user selects a reply from 2–3 smart buttons, each
   showing hanzi, pinyin and English, each playable aloud.
2. **Fragment Building** — the user assembles a reply from a shuffled
   word bank (target words plus 1–2 distractors) by tapping chips into order.
3. **Scaffolded Production** — the user types or speaks freely. If they
   hit a blank, the trailing English word can be translated in place with one
   tap (offline dictionary first, micro LLM call as fallback).
4. **Free-Flowing Conversation** — open-ended text and voice chat with
   the adaptive coach persona.

**Stage policy:** the bridge applies to the Beginner level only. A session
qualifies at 6+ user turns or a completed mission; after 3 qualifying sessions
at a stage the app *offers* the next stage (never forces it — declining pins
the stage). Any stage can be pinned manually in Settings. If the model fails
to supply choices or a word bank for a turn, the UI degrades gracefully to
free input for that turn.

### 2.2 Interactive "Real-World Impact" simulation engine

Language practice is treated like a branching narrative game. The LLM does not
break character to grade the user; it acts out the social consequences of the
user's language choices.

- **Branching narrative outcomes:** wrong vocabulary or an aggressive tone
  changes the character's mood (shown as an avatar mood indicator: 😊 🙂 😕 😒 🤩)
  and alters the story (the waiter brings the wrong dish).
- **Interactive inventory:** successful interactions award visual items —
  completing a hotel check-in earns a 🔑 Room Key and 📶 Wi-Fi Password in the
  on-screen inventory.
- **Mission catalog (launch set):** Hotel Check-In, Ordering Dinner, Taxi
  Ride, Bar Banter, Office Small Talk — each with goals, earnable items, a
  minimum level, and a passport stamp title.
- **Trust boundaries:** the client validates every scenario event from the
  model — hallucinated or duplicate item awards are dropped, and "mission
  complete" is honored only once every item has genuinely been earned.

### 2.3 UX safety net & accessibility

- **Post-turn corrections:** the LLM never interrupts mid-thought. Fixes and
  native phrasing ("You said X, but locals say Y") appear subtly after the
  user's turn, attached to the coach's reply.
- **Slow playback:** every coach message has a permanent slow-speed play
  button alongside normal speed; global speech rate is adjustable 50–150%.
- **Tap-to-define:** any Chinese character in any message opens a dictionary
  sheet (pinyin, HSK level, part of speech, examples).

---

## 3. Adaptive personalization engine

The app avoids a rigid linear curriculum. The engine is deliberately
heuristic (not actual RL) — simple, inspectable rules over aggregated signals.

```
[ 🎲 1. Random Start Event ] → [ ⚡ 2. Brain Shifts Modes ] → [ 📈 3. High-Focus Practice ]
  Surprise daily challenge      Breaks native-language        Routine practice becomes
  ("The Curveball")             autopilot                     more effective
```

### 3.1 Data inputs & signals

- **Response latency:** milliseconds from the coach's message landing to the
  learner's reply, aggregated per ISO week (samples over 5 minutes are
  discarded as walk-aways).
- **Active self-correction rate:** how often the learner deletes while
  drafting, plus speech-input restarts (mic sessions that never produce a
  message) — aggregated weekly for future drill targeting.

### 3.2 The Spotify model: exploration vs. exploitation

- **80% exploitation:** the home screen suggests the next sensible mission for
  the learner's level and history.
- **20% exploration ("The Curveball"):** the first session of each day opens
  with a surprise, high-stakes challenge — *"the bouncer says the club is
  full; talk your way in"* — as a psychological primer to drop the native-
  language bias. The pick is stable within a day; missions keep their own
  opening beat instead.

---

## 4. Adult-centric progression & engagement

Patronizing gamification is replaced with professional tracking metrics:

- **Response latency reduction:** week-over-week reply-speed trend showing
  automatic fluency forming.
- **Fluency Passport:** a travel-passport-styled screen stamped with practical
  milestones — *Hotel Check-In Certified*, *Bar Banter Fluent*, *Boardroom
  Survivor* — each with its completion date.
- **Vocabulary Wealth:** the utility-weighted worth of mastered words
  (weighted by HSK difficulty; rare words count most), not a raw word count.
- **Streak protection with personality:** a missed day earns a witty,
  adult-to-adult nudge — *"I get it, you had a long night out. A quick
  2-minute hangover-recovery chat brings the streak back."* — never a
  guilt-trip. Current and longest streaks are tracked.

---

## 5. Functional feature matrix

| Learning stage | Primary UX component | Content type | Primary metric |
|---|---|---|---|
| Beginner | Curated button choices, word bank, tap-to-translate, speed controls | High-cognition, low-vocabulary travel & daily-life scenarios | Bridge stage completion |
| Intermediate | Free typing with assists, missions (bar banter, office small talk) | Work, travel, opinions, casual banter | Response latency reduction |
| Advanced / Fluent | Free voice simulation, full branching consequences | Culture, news, idioms, slang, professional scenarios | Passport milestones unlocked |

---

## 6. Non-goals (current scope)

- Multi-language support (the product is Mandarin-only; abstractions are not
  speculatively generalized).
- User accounts / cloud sync (all data is on-device).
- True reinforcement learning (the adaptive engine is heuristic by design).
- Pronunciation scoring (speech recognition is used for input, not grading).
- Push notifications (streak nudges are in-app only).

---

## 7. Implementation status

| PRD section | Status |
|---|---|
| 2.1 Beginner Bridge (4 stages, advancement, manual override) | ✅ Shipped |
| 2.2 Simulation engine (5 missions, moods, inventory, validation) | ✅ Shipped |
| 2.3 Post-turn corrections, slow playback, tap-to-define | ✅ Shipped |
| 3.1 Latency + self-correction signal collection | ✅ Shipped (collection; drill targeting is future work) |
| 3.2 Curveball + 80/20 mission suggestion | ✅ Shipped |
| 4 Passport, streaks, latency trend, vocabulary wealth | ✅ Shipped |
| Drag-and-drop word bank | ⏩ Shipped as tap-to-order; drag is polish backlog |
| Voice hesitation latency (mid-utterance) | ⏩ Backlog — requires custom ASR pipeline |
| Adaptive drill generation from signals | ⏩ Backlog |
