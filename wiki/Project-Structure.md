# Project Structure

Overview of the Language Cards codebase layout.

---

## Directory Layout

```
LanguageCards/
├── androidApp/          # Android application
│   ├── src/main/        # Kotlin/Swift, resources
│   └── src/test/        # Unit tests
├── iosApp/              # iOS app (SwiftUI host)
│   ├── Configuration/   # Config.xcconfig (framework paths, linker flags)
│   └── iosApp/          # Swift sources, Assets, Info.plist
├── shared/              # Kotlin Multiplatform shared code
│   ├── src/
│   │   ├── commonMain/  # Shared Kotlin + Compose UI
│   │   │   ├── kotlin/  # Models, session logic, UI, data layer
│   │   │   ├── composeResources/  # Deck JSON files
│   │   │   └── sqldelight/        # SQL schema and queries
│   │   ├── androidMain/ # Android-specific implementations
│   │   ├── iosMain/     # iOS-specific implementations (MainViewController)
│   │   └── commonTest/  # Shared unit tests
│   └── build.gradle.kts
├── docs/                # Design and planning documents
├── AppIcon.appiconset/  # App icons (source)
└── wiki/                # Wiki source (version-controlled)
```

---

## Key Components

| Path | Description |
|------|-------------|
| `shared/src/commonMain/kotlin/` | Core logic: models, `SessionFlow`, `CardSelector`, `DeckRepository`, Compose UI (`App`, screens) |
| `shared/src/commonMain/sqldelight/` | SQLDelight schema (`*.sq`) for decks, cards, stats |
| `shared/src/commonMain/composeResources/files/` | Bundled deck JSON files (French, Spanish, German) |
| `shared/src/iosMain/` | `MainViewController` — Kotlin entry point for iOS |
| `androidApp/` | `MainActivity` — Android host, file picker for import |
| `iosApp/` | SwiftUI host, `ContentView` → `ComposeView` → `MainViewControllerKt.MainViewController()` |

---

## Architecture

- **Shared UI**: Compose Multiplatform provides a single UI codebase for Android and iOS.
- **Platform entry points**: Android uses `MainActivity`; iOS uses `MainViewController` invoked from Swift.
- **Persistence**: SQLDelight with SQLite; drivers are platform-specific (Android driver, Native driver for iOS).
- **Deck format**: JSON files (see `shared/.../DeckFile.kt` and sample `.deck.json` files).
