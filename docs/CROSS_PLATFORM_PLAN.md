# Cross-Platform Plan: Android + iOS

## 1. Options Overview

| Approach | Pros | Cons |
|----------|------|------|
| **Kotlin Multiplatform (KMP) + Compose Multiplatform** | Reuse Kotlin/Compose; share logic + UI; DataStore has KMP support; iOS Compose stable (1.8+) | New Gradle structure; need Xcode/macOS for iOS; some platform APIs |
| **Flutter** | Single codebase, mature iOS support | Rewrite in Dart; different paradigm |
| **React Native** | Single codebase, large ecosystem | Rewrite in JS/TS; different stack |
| **Separate native** (Kotlin/Android + Swift/SwiftUI) | Best per-platform UX | Two full codebases; duplicated logic |

**Recommendation:** **Kotlin Multiplatform + Compose Multiplatform** — leverages existing Kotlin/Compose investment, shares most code, single language/UI paradigm.

---

## 2. Current App Inventory (Shareable vs Platform-Specific)

| Layer | Current | Shareable in KMP | Platform-Specific |
|-------|---------|------------------|-------------------|
| **Models** | `Card`, `Deck`, `CardStats`, `SessionState`, `CardResult`, `PracticeDirection` | ✅ Pure data, no Android deps | — |
| **Business logic** | `SessionFlow` | ✅ Pure Kotlin | — |
| **Data** | `SampleData` | ✅ | — |
| **Persistence** | `StatsStore` + DataStore | ✅ Logic shareable | Paths & DataStore creation (expectContext on Android, NSFileManager path on iOS) |
| **UI** | Compose in `MainActivity` | ✅ Compose Multiplatform | Theme/resources, `finish()`, `lifecycleScope` |
| **App shell** | `MainActivity` | — | ✅ Separate `MainActivity` (Android) vs `MainApplication` / Swift UI entry (iOS) |

---

## 3. Target Structure (KMP + Compose Multiplatform)

```
LanguageCards/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── libs.versions.toml
├── shared/                          # shared module
│   ├── src/
│   │   ├── commonMain/kotlin/
│   │   │   ├── model/                # Card, Deck, CardStats, SessionState, etc.
│   │   │   ├── session/              # SessionFlow
│   │   │   ├── data/                 # SampleData, StatsRepository (interface)
│   │   │   └── ui/                   # Compose screens, theme, navigation
│   │   ├── androidMain/kotlin/
│   │   │   └── platform/             # Android StatsStore impl, DataStore path
│   │   └── iosMain/kotlin/
│   │       └── platform/             # iOS StatsStore impl, DataStore path
│   └── build.gradle.kts
├── androidApp/                       # Android app
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/.../MainActivity.kt
│   └── build.gradle.kts
└── iosApp/                           # iOS app (Xcode project or KMP iOS target)
    └── ...
```

---

## 4. Migration Steps (Phased)

### Phase 1: Gradle & Module Setup

- Create a KMP project (or convert current one) with:
  - `shared` (commonMain + androidMain + iosMain)
  - `androidApp` (application, depends on `shared`)
  - `iosApp` (iOS framework + app, depends on `shared`)
- Use the same Kotlin and Compose BOM versions; align with [Compose Multiplatform docs](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-create-first-app.html) for `composeMultiplatform` and iOS source sets.
- Move **only** the following into `shared/src/commonMain/kotlin` first (no UI yet):
  - `model/`: `Card`, `Deck`, `CardStats`, `SessionState`, `CardResult`, `PracticeDirection`
  - `session/`: `SessionFlow`
  - `data/`: `SampleData`
- Ensure Android app still builds and calls into `shared` (e.g. `SessionFlow`, models) so that “logic” is already shared.

### Phase 2: Abstract Persistence

- Define in `commonMain` a small interface, e.g. `StatsRepository` (or keep name `StatsStore` if you prefer):
  - `fun record(cardId: String, wasHit: Boolean)`
  - `fun getStats(cardId: String): CardStats?`
  - Optional: `suspend fun load()` / `suspend fun save()` if you want explicit load/save.
- Implement once in `androidMain` using DataStore (current logic + `context.filesDir` path).
- Implement once in `iosMain` using KMP DataStore with iOS path (single DataStore instance per process; use a lazy/singleton as in the KMP DataStore docs).
- Replace current `StatsStore` usage in shared code with this abstraction so that both platforms use the same API and the rest of the app is platform-agnostic.

### Phase 3: Shared UI (Compose Multiplatform)

- Add Compose Multiplatform to `shared` (commonMain; optionally androidMain/iosMain for platform-specific composables if needed).
- Move Compose UI from `MainActivity` into `shared`:
  - Start screen (deck + direction + Start/Exit)
  - Card screen (prompt/answer, I know / Show answer, right/wrong)
  - Summary screen (counts, missed list, Practice again / Exit)
- Use a shared entry point, e.g. `@Composable fun App()` that sets theme and shows the same when/sessionState logic you have today.
- Keep navigation and state in shared code (e.g. `sessionState: SessionState?`, `onStart`, `onPracticeAgain`, `onExit`).
- For “Exit”:
  - On Android: pass an `onExit: () -> Unit` that calls `activity.finish()` from `androidApp`.
  - On iOS: pass an `onExit` that calls the iOS equivalent (e.g. exit or dismiss) from the iOS app.
- Replace `lifecycleScope` with a shared `CoroutineScope` (e.g. from a `Composable`-scoped `rememberCoroutineScope()` or a ViewModel that’s provided from the app layer).

### Phase 4: Platform App Shells ✅

- **Android:** `MainActivity` sets `setContent { App(statsStore = androidStatsStore, onExit = { finish() }) }` (or injects these from a small Android-specific module).
- **iOS:** Create the iOS app target; from Swift/ObjC call into shared Kotlin and show the Compose UI in the host view; provide the iOS `StatsStore` implementation and `onExit` there.

**Implemented:** Android shell in `androidApp/MainActivity.kt` (already in place). iOS: `shared/iosMain/.../MainViewController.kt` exposes `MainViewController()` using `IosStatsStore` and `onExit = { exitProcess(0) }`; `iosApp/iosApp/` contains SwiftUI host (`ContentView.swift`, `LanguageCardsApp.swift`) that presents it. See `iosApp/README.md` for Xcode setup.

### Phase 5: Polish ✅

- Move strings to `commonMain` resources (Compose Multiplatform resources) so they can be localized later.
- Unify theme (colors, typography) in shared code; use `ComposeTheme` (or your theme name) in both apps.
- Add iOS-specific tweaks (safe area, back swipe, status bar) via Compose Multiplatform’s platform APIs or expect/actual if needed.

---

## 5. Platform-Specific Pieces

| Concern | Android | iOS |
|---------|---------|-----|
| **Stats persistence** | DataStore with `context.filesDir` (or app-specific dir) | DataStore with path from `NSFileManager` (Documents or app support); **single DataStore instance per process** (lazy/singleton). |
| **App lifecycle / scope** | `lifecycleScope` in Activity | Equivalent scope from iOS host (e.g. main run loop or Kotlin’s main dispatcher). |
| **Exit** | `Activity.finish()` | iOS exit/dismiss from host. |
| **Build** | Gradle `androidApp`; run on device/emulator | Gradle + Xcode (or Gradle-only iOS); run on simulator/device; need macOS. |

---

## 6. Build and Tooling

- **Android:** Same as today (Gradle; Android Studio).
- **iOS:** macOS + Xcode; Kotlin/Native produces a framework; iOS app links to it. Use the official KMP + Compose Multiplatform “create first app” and “iOS” guides to get the exact `iosApp` and `iosSimulatorArm64`/`iosArm64` setup.
- **Shared code:** Edit in Android Studio or IntelliJ with the KMP plugin; run `androidApp` on Android and `iosApp` on simulator/device to test both.

---

## 7. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| DataStore on iOS (file locking, singleton) | Use one DataStore instance per app (lazy/singleton); follow KMP DataStore docs and the “singleton Preference DataStore” pattern for iOS. |
| Compose on iOS regressions | Pin Compose Multiplatform to a stable version (e.g. 1.8+); test on real devices and multiple iOS versions. |
| Different UX expectations (back, exit) | Use Compose Multiplatform’s navigation and lifecycle; keep “Exit” and “Practice again” behavior identical, with only the system “back”/“exit app” differing by platform. |
| Team doesn’t have macOS | Use CI (e.g. GitHub Actions with `macos-latest`) to build and test the iOS target; develop Android-first and run iOS periodically. |

---

## 8. Summary Checklist

1. Set up KMP project with `shared` + `androidApp` + `iosApp`.
2. Move models and `SessionFlow` (and `SampleData`) into `shared`; keep Android app building.
3. Introduce `StatsRepository` (or abstract `StatsStore`) and implement with DataStore on Android and iOS; use it from shared code.
4. Move Compose UI into `shared` and use Compose Multiplatform; wire `App()` with `onExit` and stats from each platform.
5. Implement Android and iOS app shells that host shared UI and provide platform-specific dependencies.
6. Centralize strings and theme in shared code; test both platforms.
