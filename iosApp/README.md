# iOS App (Language Cards)

This folder contains the iOS app that hosts the shared Compose Multiplatform UI.

## Prerequisites

- **macOS** with **Xcode** installed
- The shared framework must be built first (see below)

## Build the shared framework

From the **project root** (LanguageCards/), run:

```bash
# For iOS Simulator (Apple Silicon)
./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64

# For physical device
./gradlew :shared:linkReleaseFrameworkIosArm64
```

The framework will be at:
- Simulator: `shared/build/bin/iosSimulatorArm64/releaseFramework/shared.framework`
- Device: `shared/build/bin/iosArm64/releaseFramework/shared.framework`

## Xcode project setup

The Xcode project is at `iosApp/LanguageCards.xcodeproj`. Open it in Xcode to build and run.

1. **If you need to recreate the Xcode project** (project file was removed):
   - Open Xcode → File → New → Project
   - Choose **App** (iOS)
   - Product Name: **LanguageCards**, Organization Identifier: e.g. `net.thetrues`
   - Interface: **SwiftUI**, Language: **Swift**
   - Save inside `iosApp/` (e.g. so you have `iosApp/iosApp/` with the app source)

2. **Add the Swift source files** from `iosApp/iosApp/`:
   - `LanguageCardsApp.swift` (with `@main`)
   - `ContentView.swift` (with `ComposeView` and `MainViewController_iosKt`)

3. **Link the shared framework**:
   - Select the app target → **General** → **Frameworks, Libraries, and Embedded Content**
   - Click **+** → **Add Other** → **Add Files**
   - Navigate to `shared/build/bin/iosSimulatorArm64/releaseFramework/shared.framework` (after building)
   - Set it to **Embed & Sign** (or **Embed Without Signing** for simulator)

4. **Framework search path** (if needed):
   - Build Settings → **Framework Search Paths**: add  
     `$(PROJECT_DIR)/../../shared/build/bin/iosSimulatorArm64/releaseFramework`  
     (and the same for `iosArm64` when building for device)

   **Alternatively**, use a **Run Script** build phase (before "Embed Frameworks") so Xcode builds the framework automatically:
   ```bash
   cd "$SRCROOT/../.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
   Then set **Framework Search Paths** to:
   `$(SRCROOT)/../../shared/build/xcode-frameworks/$(CONFIGURATION)-$(SDK_NAME)`
   (if that path is used by the Gradle task on your setup).

5. **Calling Kotlin from Swift**  
   The shared module exposes `MainViewController()` from Kotlin. The exact Swift name depends on the Kotlin/Native version:
   - Often: `MainViewController_iosKt.MainViewController()`
   - Or: `MainViewControllerKt.MainViewController()`

   If you get a compile error, build the framework and inspect the generated headers in `shared.framework/Headers/` or use Xcode’s autocomplete after adding the framework.

6. **Run**  
   Select the simulator or a connected device and run the app. The Compose UI (start screen, cards, summary) is shown inside the SwiftUI host.

## Phase 4 summary

- **Android:** `MainActivity` already uses `App(statsStore = androidStatsStore, onExit = { finish() })` (see `androidApp/`).
- **iOS:** This app provides the iOS shell: it builds the `shared` framework, links it in Xcode, and presents the Compose UI via `MainViewController()`, which uses `IosStatsStore` and `onExit = { exitProcess(0) }`.
