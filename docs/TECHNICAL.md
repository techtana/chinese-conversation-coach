# Technical Documentation

Architecture and implementation reference for Mandarin Conversation Coach.

## 1. Stack & build

| | |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 (BOM 2024.12.01), material-icons-extended |
| Architecture | MVVM (single `AndroidViewModel` for conversation; simpler screens read repositories directly) |
| Persistence | DataStore Preferences (two stores: `user_prefs`, `progress_store`) |
| Networking | OkHttp 4.12 (30 s connect / 60 s read timeouts) |
| Serialization | kotlinx-serialization-json 1.7.3 |
| Speech | Android system TTS (zh-CN) and `RecognizerIntent` STT |
| SDK | min 26, target/compile 35 |
| Tests | JUnit 4, pure-JVM unit tests |

No DI framework, no Room, no build-time codegen beyond Compose — deliberate
choices to keep the codebase small. All structured persistence is JSON strings
in DataStore, aggregated on write so nothing grows unbounded.

## 2. Module map

```
data/model/        Pure data types (most are JVM-only, unit-testable)
  CoachResponse.kt   LLM response schema + ChoiceOption, Correction, ScenarioEvent
  Message.kt         Chat message (id, isUser, hanzi, pinyin, english, tip, correction)
  BridgeStage.kt     PASSIVE_INPUT → FRAGMENT_BUILDING → SCAFFOLDED → FREE_FLOW
  Scenario.kt        Mission definition + ScenarioItem + validateEvent()
  ProficiencyLevel.kt BEGINNER / INTERMEDIATE / ADVANCED / FLUENT
  LLMProvider.kt     8 providers with base URLs, default models, pricing
  ProgressModels.kt  CompletionRecord, StreakState, WeekAggregate, HesitationAggregate

data/repository/
  LLMRepository.kt        HTTP calls, request building, cost tracking, translateFragment()
  PromptBuilder.kt        Composable system-prompt blocks (see §4)
  CoachResponseParser.kt  JSON parse + regex fallback + structured-field validation
  VocabularyRepository.kt Level corpora, active-vocabulary filtering, segmentation
  DictionaryRepository.kt ~440 HSK 1–3 entries, lookup / lookupWithContext / reverseLookup
  ScenarioRepository.kt   Hardcoded mission catalog (5 scenarios)

data/preferences/
  UserPreferences.kt    user_prefs store: API keys, profile, level, bridge stage,
                        learned words, speech settings, cost
  ProgressRepository.kt progress_store: completions, earned items, streak,
                        latency & hesitation weekly aggregates, curveball day

domain/              Pure logic, no Android imports — fully unit-tested
  StageProgressionEngine.kt  Qualifying-session counting → stage advancement offers
  WordBankState.kt           Immutable tap-to-order chip state
  StreakManager.kt           Day-streak transitions + witty nudges
  LatencyStats.kt            ISO-week bucketing, outlier discard (> 5 min)
  VocabularyWealthCalculator.kt  HSK-weighted word scoring
  CurveballProvider.kt       Daily surprise-opener selection (day-seeded)
  SignalTracker.kt           Draft-deletion churn + mic-restart counting
  ContentModeSelector.kt     80/20 exploit/explore mission suggestion

service/
  TextToSpeechService.kt        zh-CN TTS, voice selection, rate control
  SpeechRecognitionService.kt   System recognizer intent wrapper

ui/
  navigation/AppNavigation.kt   Routes: home, conversation/{level}?scenario={id},
                                settings, passport
  home/HomeScreen.kt            Level cards, missions row, streak banner, suggestion badge
  conversation/                 ConversationScreen + ConversationViewModel
    input/                      FreeInputBar, ChoiceInputBar, WordBankInputBar, ScaffoldedInputBar
    components/                 ScenarioHud, ScenarioCompleteDialog
  passport/PassportScreen.kt    Stamps, streaks, wealth, latency trend
  settings/SettingsScreen.kt    Profile, bridge stage, provider/API key, speech, display
  theme/                        Color.kt, Theme.kt, Type.kt
```

## 3. The conversation loop

```
User input (tap / chips / text / voice)
        │
        ▼
ConversationViewModel.sendMessage(text)
  ├─ latency sample (time since last AI message; outliers discarded)
  ├─ hesitation snapshot (edit churn, mic restarts) → ProgressRepository
  ▼
LLMRepository.sendMessage(…, stage, scenario, earnedItems, curveball)
  ├─ VocabularyRepository.getActiveVocabulary(level, learnedWords, target)
  ├─ PromptBuilder.build(…)            ← mode-specific system prompt
  ├─ HTTP call (Claude-native or OpenAI-compatible format)
  ├─ cost accounting from usage tokens → UserPreferences.addCost
  ▼
CoachResponseParser.parse(rawText)
  ├─ strict JSON decode (ignoreUnknownKeys, isLenient)
  ├─ fallback: regex field extraction → scalars only, structured fields null
  └─ validation: choices must be 2–4 non-blank; wordBank ≥ 2 entries
  ▼
ViewModel.onSuccess
  ├─ auto-learn words from response.hanzi → UserPreferences
  ├─ lift choices/wordBank into UI state (null ⇒ free-input fallback this turn)
  ├─ handleScenarioEvent: Scenario.validateEvent → award items, mood, completion
  └─ onUserTurnCompleted → StageProgressionEngine → advancement offer dialog
  ▼
ConversationScreen
  ├─ message list (tappable hanzi, pinyin, English, tip, correction)
  ├─ TTS autoplay of the coach reply
  └─ bottomBar dispatch on uiState.inputMode (the 4 input bars)
```

History sent to the model is the last 20 messages, hanzi only — choice taps
and assembled word-bank replies flow through the same `sendMessage(text)`
path, so every input mode is transparent to the model.

## 4. LLM integration

### Response schema

The model must reply with JSON only. All fields beyond the core three are
optional with defaults, so old prompts/responses stay compatible:

```json
{
  "hanzi": "…", "pinyin": "…", "english": "…",
  "tip": "optional teaching aside",
  "correction": {"userSaid": "…", "better": "…", "note": "…"},
  "choices": [{"hanzi": "…", "pinyin": "…", "english": "…"}],
  "wordBank": ["word1", "word2"], "expectedAnswer": "…",
  "scenario": {"itemEarned": "room_key", "mood": "confused", "completed": false}
}
```

### Prompt building

`PromptBuilder.build()` assembles blocks: persona (coach 小明, or the
scenario's in-character role), student profile, active vocabulary (the model
is instructed to strictly limit itself to it), level guidance, stage
instructions, scenario rules, optional curveball brief, JSON contract, and
global rules. **The JSON contract example only includes the fields relevant to
the current mode** — this measurably improves schema adherence and saves
tokens. `max_tokens` scales with mode (600 base, 800 in scenarios, 1000 for
choice/word-bank stages).

### Reliability strategy

The model is treated as untrusted input everywhere:

- Malformed JSON degrades to regex scalar extraction; structured fields become
  null and the UI falls back to free input for that turn.
- `choices` are re-shuffled client-side (in case the model leads with the
  "best" answer despite instructions).
- Scenario events are validated client-side: item ids must exist in the
  mission definition, can't be earned twice, and `completed` is only honored
  when every item is genuinely earned.
- `expectedAnswer` is never displayed.

### Providers

`LLMProvider` enumerates Claude (default, Anthropic-native request format),
OpenAI, DeepSeek, Google Gemini, DeepInfra, Azure, AWS Bedrock, and PRIVATE
(any OpenAI-compatible endpoint, custom base URL/model). Per-provider API keys
live in `user_prefs`; per-message token costs accumulate via provider pricing
metadata.

`translateFragment()` is a second, tiny entry point (max_tokens 100) used by
the Stage-3 inline assist when the offline dictionary has no match.

## 5. Persistence

Two DataStore Preferences files:

**`user_prefs`** (settings & learner config): per-provider API keys, profile
(name/goals/interests), proficiency level, bridge stage + manual pin +
qualifying-session counts, learned-words set (comma-joined), new-words target,
speech speed, show-English, theme, cumulative cost.

**`progress_store`** (learner progress, JSON-string values):

| Key | Shape | Growth bound |
|---|---|---|
| `scenario_completions` | `{scenarioId: {epochMillis, stage}}` | ≤ mission count |
| `earned_items` | `{scenarioId: [itemId]}` | ≤ total items |
| `streak_state` | `{currentStreak, longestStreak, lastActiveEpochDay}` | constant |
| `latency_weekly` | `{"2026-W24": {sumMs, count}}` | ~52/year |
| `hesitation_weekly` | `{"2026-W24": {editChurn, sttRestarts, turns}}` | ~52/year |
| `curveball_epoch_day` | long | constant |

Aggregate-on-write keeps storage constant-bounded; if per-sample granularity
is ever needed, Room can be introduced behind `ProgressRepository` without
touching callers.

## 6. Domain logic reference

- **Stage advancement** (`StageProgressionEngine`): a session qualifies at
  ≥ 6 user turns or a completed mission; 3 qualifying sessions at a stage
  trigger an *offer* to advance (blocked by the manual pin). The ViewModel
  shows an accept/decline dialog; declining sets the pin.
- **Word bank** (`WordBankState`): immutable; chips tracked by index so
  duplicate words work; seeded shuffle for testability.
- **Streaks** (`StreakManager`): same-day no-op, +1 on consecutive days,
  reset-with-nudge on gaps, longest retained, backwards clock ignored.
- **Latency** (`LatencyStats`): ISO-week keys (`2026-W24`), samples ≤ 0 or
  > 5 min discarded.
- **Wealth** (`VocabularyWealthCalculator`): HSK 1–3 words score 1–3; words
  beyond the bundled dictionary score 4 (rarer ⇒ worth more).
- **Curveball** (`CurveballProvider`): fires once per day (epoch-day guard),
  pick seeded by the date so re-opens are stable; skipped inside missions.
- **Suggestion** (`ContentModeSelector`): 20% uniform exploration, else first
  uncompleted mission at the user's level; seeded by epoch day so the home
  badge is stable within a day.

## 7. Testing

All `domain/` classes and the parser/prompt/scenario logic are pure JVM and
covered by JUnit 4 tests in `app/src/test/`:

```
CoachResponseParserTest    fenced/malformed JSON, fallback nulls, validation
PromptBuilderTest          per-mode contract fields, scenario persona, delegation parity
VocabularyRepositoryTest   segmentation, active-vocab filtering, prompt content
ScenarioRepositoryTest     catalog invariants + validateEvent matrix
StageProgressionEngineTest advancement/hold/pin matrix
WordBankStateTest          pick/unpick/clear/duplicates/seeded shuffle
StreakManagerTest          day-transition matrix
LatencyStatsTest           week bucketing, outlier discard
VocabularyWealthCalculatorTest
CurveballProviderTest      once-per-day, stable pick
SignalTrackerTest          churn counting, restart floor, snapshot reset
ContentModeSelectorTest    exploit/explore branches, fallbacks, ~20% rate
```

Run with `./gradlew test`. UI composables and Android services are not
unit-tested; verify on device (see README).

## 8. Conventions

- New LLM response fields must be optional with defaults (the parser's
  `ignoreUnknownKeys` handles the reverse direction).
- Anything decision-making belongs in `domain/` as a pure function/class with
  tests; ViewModels orchestrate, they don't decide.
- Repositories are `object`s or plain classes constructed where needed — don't
  introduce DI for its own sake.
- Persisted structures must be aggregate-on-write or naturally bounded.
