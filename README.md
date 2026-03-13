# Language Cards

A flashcard app for learning vocabulary in English ↔ French, English ↔ Spanish, and English ↔ German. Built with Kotlin Multiplatform and Compose Multiplatform.

## Features

- **Bidirectional practice** — Practice in both directions (e.g. English → French and French → English) for each language combination.
- **Per-card, per-direction stats** — Track hits and misses separately for each card and direction to prioritize difficult material.
- **Stats screen by language and direction** — See overall accuracy plus a breakdown by practice direction for the currently selected language combination.
- **Session summary** — After each session, review accuracy and which cards were hit or missed.
- **Deck management** — Add, delete, and restore decks; restore the default bundled decks at any time.
- **Robust deck import** — Import custom decks from JSON with validation, overwrite prompts on duplicates, and clear success/error messages.
- **Built-in decks** — French (Basics, Past Tense, Conversation); Spanish (Basics, Conversation); German (Basics, Conversation).

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
