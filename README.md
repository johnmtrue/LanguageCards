# Language Cards

A flashcard app for learning vocabulary in English ↔ French, English ↔ Spanish, and English ↔ German. Built with Kotlin Multiplatform and Compose Multiplatform.

## Features

- **Bidirectional practice** — English → Foreign and Foreign → English
- **Hit/miss tracking** — Per card and per direction, to prioritize difficult cards
- **Session summary** — See accuracy and progress after each practice round
- **Overall stats** — Track total hits, misses, and accuracy across all sessions
- **Add, delete, and restore decks** — Add bundled decks, remove unwanted ones, or restore defaults
- **Import decks** — Add custom decks from JSON files (Android)
- **Built-in decks** — French (Basics, Past Tense, Conversation); Spanish (Basics, Conversation); German (Basics, Conversation)

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform (shared UI)
- Android (androidApp) + iOS (iosApp, Xcode)

## Prerequisites

- JDK 21
- Android Studio (for Android)
- Xcode + macOS (for iOS)

## Build

### Android

```bash
./gradlew :androidApp:assembleDebug
```

### iOS

iOS requires Xcode and manual setup. See [iosApp/README.md](iosApp/README.md).

NOTE: iOS build has not been fully tested.

## Project Structure

- `shared/` — shared Kotlin code, Compose UI, models, session logic
- `androidApp/` — Android application
- `iosApp/` — iOS app (SwiftUI host for Compose)
- `docs/` — design and planning documents

## Contributing

Bug reports and feature proposals are welcome via GitHub Issues. See [CONTRIBUTING.md](CONTRIBUTING.md) for code style and pull request guidelines.

## License

© 2026 John True. Licensed under MIT. See [LICENSE](LICENSE) for details.
