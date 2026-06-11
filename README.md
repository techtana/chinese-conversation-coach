# 普通话 · Mandarin Conversation Coach

An Android app for learning Mandarin Chinese through real conversation with an
LLM-powered coach — built for adults who want practical fluency, not cartoon
badges and grammar drills.

The coach (小明 Xiǎo Míng) adapts to your proficiency level, constrains itself
to vocabulary you're ready for, speaks every reply aloud, and corrects you
*after* your turn — never mid-thought. Beginners climb a four-step scaffolding
ladder from tappable replies to free voice chat, and roleplay missions let you
earn the room key instead of a gold star.

## Features

- **Conversational practice at 4 levels** — Beginner (初级), Intermediate
  (中级), Advanced (高级), Fluent (流利), each with a curated vocabulary corpus
  the coach strictly sticks to, introducing a configurable number of new words
  per conversation.
- **The 4-Step Beginner Bridge** — scaffolding that fades as confidence grows:
  1. 🔁 *Guided Choices* — tap one of 2–3 curated replies (hanzi/pinyin/English, each playable aloud)
  2. 🏗️ *Word Building* — assemble your reply from a shuffled word bank
  3. 🤝 *Assisted Typing* — type freely; stuck on a word? An inline EN→中 chip translates just that piece
  4. 🚀 *Free Conversation* — open text and voice chat

  Advancement is offered automatically after qualifying sessions, or pin any
  stage manually in Settings.
- **Real-World Missions** — five branching roleplay scenarios (hotel check-in,
  ordering dinner, taxi, bar banter, office small talk). The AI stays in
  character and reacts to your language in-story — confuse the waiter and the
  wrong dish arrives. Success earns inventory items (🔑 房卡, 📶 无线网密码) and a
  passport stamp.
- **Post-turn corrections** — "You said 「X」 · locals say 「Y」" appears subtly
  under the coach's reply, with the reason.
- **Fluency Passport** — scenario stamps with dates, current/longest streak,
  week-over-week response-speed trend, and Vocabulary Wealth (words weighted by
  difficulty, not raw counts).
- **Streaks with personality** — miss a day and you get a witty adult-to-adult
  nudge, not a guilt-trip.
- **The daily Curveball** — your first session each day opens with a surprise
  challenge ("the bouncer says the club is full — talk your way in") to break
  the native-language autopilot.
- **Voice in and out** — Android TTS reads every coach message (per-message
  normal and slow playback, adjustable 50–150% speed); system speech
  recognition handles voice input.
- **Tap-to-define** — tap any Chinese character in any message for a dictionary
  sheet with pinyin, HSK level, part of speech, and example sentences (~440
  bundled HSK 1–3 entries with multi-character word detection).
- **Bring your own LLM** — Claude (default), OpenAI, DeepSeek, Google Gemini,
  DeepInfra, Azure, AWS Bedrock, or any OpenAI-compatible private endpoint.
  Per-provider API keys and cumulative cost tracking.

## Getting started

### Requirements

- Android Studio (Ladybug or newer recommended)
- Android SDK 35 (min SDK 26 / Android 8.0)
- JDK 17+
- An API key for at least one supported LLM provider

### Build & run

```bash
git clone https://github.com/techtana/chinese-conversation-coach.git
cd chinese-conversation-coach
./gradlew assembleDebug
```

Install on a device/emulator, then open **Settings → LLM Backend** to pick a
provider and paste your API key. Keys are stored locally in DataStore and sent
only to the provider you selected.

### Tests

```bash
./gradlew test
```

Unit tests cover the response parser, prompt builder, stage progression, word
bank, scenario validation, streaks, latency bucketing, vocabulary wealth, and
the adaptive heuristics.

## Documentation

| Document | Contents |
|---|---|
| [docs/PRD.md](docs/PRD.md) | Product requirements: vision, audience, feature specs, success metrics |
| [docs/TECHNICAL.md](docs/TECHNICAL.md) | Architecture, data flow, LLM integration, persistence, testing |
| [docs/DESIGN.md](docs/DESIGN.md) | UX principles, screen inventory, interaction patterns, visual language |

## Project structure

```
app/src/main/java/com/mandarincoach/app/
├── data/
│   ├── model/          # CoachResponse, Message, Scenario, BridgeStage, progress models
│   ├── preferences/    # UserPreferences (settings), ProgressRepository (learner progress)
│   └── repository/     # LLMRepository, PromptBuilder, CoachResponseParser,
│                       # VocabularyRepository, DictionaryRepository, ScenarioRepository
├── domain/             # Pure logic: stage progression, word bank, streaks,
│                       # latency stats, vocabulary wealth, curveballs, signals
├── service/            # TextToSpeechService, SpeechRecognitionService
└── ui/
    ├── home/           # Level cards, missions row, streak banner
    ├── conversation/   # Chat screen, ViewModel, staged input bars, scenario HUD
    ├── passport/       # Fluency Passport
    ├── settings/       # Provider, profile, bridge stage, speech settings
    ├── navigation/     # NavHost + routes
    └── theme/          # Colors, typography
```

## Tech stack

Kotlin 2.0 · Jetpack Compose (Material 3) · MVVM · DataStore Preferences ·
OkHttp · kotlinx-serialization · Android TTS/STT · JUnit 4
