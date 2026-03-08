# Installation

This page describes how to set up your environment and build Language Cards.

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 21 |
| Android Studio | Latest (for Android development) |
| Xcode | Latest (for iOS development, macOS only) |

---

## Clone the Repository

```bash
git clone https://github.com/johnmtrue/LanguageCards.git
cd LanguageCards
```

---

## Build the App

### Android

```bash
./gradlew :androidApp:assembleDebug
```

The APK will be at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

### iOS

iOS requires Xcode and macOS. See [Building](Building#ios) for detailed iOS build steps.

---

## Next Steps

- [Building](Building) — Full build instructions for Android and iOS
- [Project Structure](Project-Structure) — Understand the codebase layout
