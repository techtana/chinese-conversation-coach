# Chinese Conversation Coach (普通话练习)

An Android app for practicing Mandarin Chinese with an AI conversation partner. The app adapts to your proficiency level, tracks vocabulary, provides interactive dictionary lookups, and supports both voice and text input.

## Features

- **AI Conversation Partner** — Chat with "小明" (Xiao Ming), an adaptive AI tutor powered by your choice of LLM provider
- **4 Proficiency Levels** — Beginner (初级), Intermediate (中级), Advanced (高级), and Fluent (流利)
- **Structured Responses** — Every AI message includes Chinese characters, pinyin romanization, English translation, and grammar tips
- **Interactive Dictionary** — Tap any Chinese character to see its definition, pinyin, HSK level, part of speech, and example sentences (440+ entries)
- **Vocabulary Management** — Tracks learned words and controls how many new words are introduced per conversation
- **Text-to-Speech** — Plays AI responses in Mandarin at adjustable speed (0.6×–1.2×)
- **Speech Recognition** — Voice input in Mandarin (zh-CN) via the Google Speech API
- **Multi-Provider LLM Support** — Works with Claude, OpenAI, DeepSeek, Google Gemini, DeepInfra, Azure AI Foundry, AWS Bedrock, or a custom API endpoint
- **Cost Tracking** — Monitors accumulated API usage cost per provider
- **Encrypted Storage** — API keys and settings stored with Android Security Crypto

## Screenshots

> Add screenshots here once the app is built and running.

## Requirements

- Android Studio (latest stable)
- Android SDK 35
- Java 17+
- An API key for at least one supported LLM provider
- Android device or emulator running Android 8.0 (API 26) or higher

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/techtana/chinese-conversation-coach.git
cd chinese-conversation-coach
```

### 2. Open in Android Studio

Open the project root directory in Android Studio and let Gradle sync complete.

### 3. Build and run

```bash
# Build debug APK
./gradlew assembleDebug

# Install directly to a connected device or running emulator
./gradlew installDebug
```

Or press **Shift+F10** in Android Studio to build and run.

### 4. Add an API key

On first launch:
1. Select your proficiency level on the Home screen.
2. Tap the settings icon to open Settings.
3. Enter an API key for your chosen LLM provider.
4. Return to the conversation and start practicing.

## Supported LLM Providers

| Provider | Notes |
|---|---|
| Claude (Anthropic) | Default model: `claude-haiku-4-5-20251001` |
| OpenAI | GPT models via OpenAI API |
| DeepSeek | OpenAI-compatible endpoint |
| Google Gemini | Gemini models |
| DeepInfra | OpenAI-compatible endpoint |
| Azure AI Foundry | Azure-hosted models |
| AWS Bedrock | AWS-hosted models |
| Custom | Any OpenAI-compatible API |

## Architecture

```
app/src/main/java/com/mandarincoach/app/
├── data/
│   ├── model/          # Data classes (Message, LLMProvider, ProficiencyLevel, etc.)
│   ├── repository/     # LLMRepository, VocabularyRepository, DictionaryRepository
│   └── preferences/    # UserPreferences (DataStore-backed)
├── service/
│   ├── TextToSpeechService.kt
│   └── SpeechRecognitionService.kt
└── ui/
    ├── home/           # Level selection screen
    ├── conversation/   # Main chat screen + ViewModel
    ├── settings/       # Settings screen
    ├── navigation/     # AppNavigation routes
    └── theme/          # Material Design 3 theme
```

**Patterns used:** MVVM, Repository, Kotlin Coroutines + Flow, Jetpack Compose, DataStore.

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material Design 3 |
| State | ViewModel, Kotlin Coroutines, Flow |
| Navigation | Compose Navigation |
| Networking | OkHttp3 |
| Serialization | Kotlin Serialization |
| Storage | Android DataStore + Security Crypto |
| Language | Kotlin 2.0.21 |
| Build | Gradle (Kotlin DSL), AGP 8.13.2 |

## Permissions

- `INTERNET` — Required for LLM API calls
- `RECORD_AUDIO` — Required for speech input

## Running Tests

```bash
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumented tests (requires device/emulator)
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes with a descriptive message
4. Push to your fork and open a pull request

## License

This project does not currently include a license file. All rights reserved unless otherwise stated.
