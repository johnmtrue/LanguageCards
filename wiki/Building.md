# Building

Detailed build instructions for Android and iOS.

---

## Android

### Debug build

```bash
./gradlew :androidApp:assembleDebug
```

Output: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Run unit tests

```bash
./gradlew :androidApp:testDebugUnitTest
```

### Release build

```bash
./gradlew :androidApp:assembleRelease
```

Release APK: `androidApp/build/outputs/apk/release/`

---

## iOS

### Prerequisites

- macOS with Xcode installed
- JDK 21 (for Gradle)

### Build the shared framework

From the project root:

```bash
# For iOS Simulator (Apple Silicon)
./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64

# For physical device
./gradlew :shared:linkReleaseFrameworkIosArm64
```

The framework output:
- Simulator: `shared/build/bin/iosSimulatorArm64/releaseFramework/shared.framework`
- Device: `shared/build/bin/iosArm64/releaseFramework/shared.framework`

### Build the iOS app

1. Open `iosApp/LanguageCards.xcodeproj` in Xcode
2. Select the simulator or a connected device
3. Build and run (⌘R)

The Xcode project uses a Run Script phase that invokes `embedAndSignAppleFrameworkForXcode` to build the shared framework automatically when building from Xcode. For CI (GitHub Actions), the framework is pre-built with Gradle before running xcodebuild.

### Run iOS unit tests

```bash
./gradlew :shared:iosSimulatorArm64Test
```

---

## Shared module (common code)

Build the shared Kotlin/Compose module and run common tests:

```bash
./gradlew :shared:build
./gradlew :shared:allTests
```
