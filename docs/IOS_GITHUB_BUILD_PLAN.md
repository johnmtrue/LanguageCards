# Plan: Getting iOS Builds Working on GitHub

This document outlines the steps to add iOS build support to the LanguageCards GitHub Actions CI.

---

## 1. Current State

| Item | Status |
|------|--------|
| **Shared module** | ✅ Has `iosX64`, `iosArm64`, `iosSimulatorArm64` targets; builds frameworks |
| **iosApp** | ✅ Has Swift sources (`LanguageCardsApp.swift`, `ContentView.swift`), `Config.xcconfig` |
| **Xcode project** | ❓ No `.xcodeproj` or `.xcworkspace` in repo — may need creation/commit |
| **GitHub CI** | ✅ Android workflow exists (`.github/workflows/android.yml`); no iOS job |

---

## 2. Prerequisites for iOS CI

1. **macOS runner** — GitHub-hosted `macos-latest` (or `macos-14`, `macos-15`)
2. **Xcode** — Use `maxim-lobanov/setup-xcode` or rely on default Xcode on runner
3. **JDK** — Same as Android (Java 21 / Temurin)
4. **Gradle** — To build the shared framework before Xcode builds the app

---

## 3. Build Flow

1. **Build shared Kotlin framework** (Gradle):
   ```bash
   ./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64
   ```
   Output: `shared/build/bin/iosSimulatorArm64/releaseFramework/shared.framework`

2. **Build iOS app** (xcodebuild):
   - Uses the pre-built framework
   - Needs an Xcode project (`.xcodeproj`) or workspace (`.xcworkspace`)

---

## 4. Required Steps

### 4.1 Ensure Xcode Project Exists and Is Committed

**Current:** `iosApp/` contains Swift sources and `Config.xcconfig` but no `.xcodeproj` / `.xcworkspace`.

**Actions:**
- [ ] If you have an Xcode project locally, add it to the repo (e.g. `iosApp/iosApp.xcodeproj` or `iosApp/LanguageCards.xcodeproj`)
- [ ] Ensure `.gitignore` does not exclude `*.xcodeproj` or `*.xcworkspace`
- [ ] If no Xcode project exists, create one:
  - Open Xcode → File → New → Project → App (iOS)
  - Product name: `LanguageCards`, Organization: `net.thetrues`
  - Interface: SwiftUI, Language: Swift
  - Save under `iosApp/`
  - Add existing Swift files to the target
  - Configure Framework Search Paths per `Config.xcconfig`
  - Add a Run Script phase (optional) to build the shared framework before compile

### 4.2 Choose CI Strategy

**Option A: Gradle first, then xcodebuild**

- Job 1 (or step 1): `./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64`
- Job 2 (or step 2): `xcodebuild build -project iosApp/<Project>.xcodeproj -scheme <Scheme> -sdk iphonesimulator -derivedDataPath ./build`

**Option B: Xcode Run Script**

- Add a Run Script build phase in Xcode that runs:
  ```bash
  cd "$SRCROOT/../.." && ./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64
  ```
- Then `xcodebuild` will trigger the framework build automatically (xcodebuild → Run Script → Gradle → framework built)

**Recommendation:** Option A — explicit Gradle step in CI for clearer logs and easier debugging.

### 4.3 Create iOS Workflow

Create `.github/workflows/ios.yml` (or extend the existing Android workflow):

```yaml
name: iOS

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build shared framework for iOS Simulator
        run: ./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64 --no-daemon

      - name: Build iOS app
        run: |
          xcodebuild build \
            -project iosApp/<YourProject>.xcodeproj \
            -scheme <YourScheme> \
            -configuration Debug \
            -sdk iphonesimulator \
            -destination 'generic/platform=iOS Simulator' \
            -derivedDataPath ./build

      - name: Upload iOS app
        uses: actions/upload-artifact@v4
        with:
          name: ios-simulator-app
          path: build/Build/Products/Debug-iphonesimulator/*.app
```

Replace `<YourProject>` and `<YourScheme>` with the actual Xcode project and scheme names.

### 4.4 Optional: Unify with Android Workflow

You can merge into a single workflow with multiple jobs:

- `test` — shared / Android unit tests (ubuntu-latest)
- `android` — build APK (ubuntu-latest, `needs: test`)
- `ios` — build iOS simulator app (macos-latest, `needs: test`)

---

## 5. Checklist Summary

| # | Task | Status |
|---|------|--------|
| 1 | Create or add Xcode project (`.xcodeproj`) to repo | ✅ |
| 2 | Verify `.gitignore` allows `*.xcodeproj` | ✅ |
| 3 | Confirm framework search paths in Xcode match `Config.xcconfig` | ✅ |
| 4 | Create `.github/workflows/ios.yml` with Gradle + xcodebuild steps | ✅ |
| 5 | Replace placeholder project/scheme names in workflow | ✅ |
| 6 | Test workflow: push or open PR, verify iOS job runs | |
| 7 | (Optional) Add Xcode version pin with `maxim-lobanov/setup-xcode` | |

---

## 6. Troubleshooting

- **"Framework not found shared"** — Ensure `linkReleaseFrameworkIosSimulatorArm64` runs before xcodebuild and framework path in Xcode matches Gradle output.
- **CocoaPods / SPM** — This project does not use CocoaPods. If you add it later, you may need `pod install` before xcodebuild and use `.xcworkspace`.
- **Xcode version** — macOS runners ship with a default Xcode. Use `xcode-select -s` or `maxim-lobanov/setup-xcode` if a specific version is needed.
- **Code signing** — Simulator builds typically don't require signing. For device/App Store builds, add provisioning and signing configuration.

---

## 7. References

- [Kotlin KMP CI tutorial](https://kotlinlang.org/docs/multiplatform/kmp-ci-tutorial.html)
- [Jetcaster KMP sample CI](https://github.com/kotlin-hands-on/jetcaster-kmp-migration/tree/main/.github)
- [setup-xcode action](https://github.com/maxim-lobanov/setup-xcode)

---

*Plan version: 1.0*
