# Gradle 10 Readiness

This document tracks the project's compatibility with Gradle 10 (expected 2026).

## Current Status

| Area | Status | Notes |
|------|--------|-------|
| Gradle version | 9.3.1 | Current |
| Kotlin | 2.2.21 | Aligned with Gradle 9.3.1 embedded version |
| Kotlin/Native targets | Configured | `kotlin.native.ignoreDisabledTargets=true` suppresses disabled-target warnings on machines without native toolchain |
| Archives configuration | Plugin limitation | Deprecation warning from transitive plugins; tracked in [gradle/gradle#36296](https://github.com/gradle/gradle/issues/36296) |

## Changes Made for Gradle 10 Support

1. **gradle.properties**
   - `kotlin.native.ignoreDisabledTargets=true` — Hides Kotlin/Native disabled-targets warning when building on machines without iOS toolchain.

2. **libs.versions.toml**
   - Kotlin updated to 2.2.21 (matches Gradle 9.3.1 embedded Kotlin).

3. **gradle.properties**
   - `org.gradle.warning.mode=none` — Suppresses the archives-configuration deprecation from Kotlin/Android KMP plugins. Use `--warning-mode all` when auditing.

## Known Limitations

- **Archives configuration deprecation**: The `archives` configuration is deprecated and will fail in Gradle 10. The warning originates from transitive plugins (Kotlin Multiplatform, Android KMP, or Compose), not from project build scripts. Resolution depends on plugin updates. Tracked in [gradle/gradle#36296](https://github.com/gradle/gradle/issues/36296).

- **androidLibrary block deprecation**: The Android-KMP plugin deprecates `androidLibrary` in favor of `android`. The Kotlin MPP plugin's `android` block (KotlinAndroidTarget) takes precedence and lacks namespace/compileSdk/minSdk. Keeping `androidLibrary` with `@Suppress("DEPRECATION")` until Android-KMP provides a clear migration path that works with Kotlin 2.2+.

## TODO

- **NDK build**: Fix the NDK installation (e.g. reinstall NDK 28.2 via SDK Manager so `llvm-strip` is present) and remove the `afterEvaluate` block in `androidApp/build.gradle.kts` that disables `stripDebugDebugSymbols`.

## Verification

To check for deprecation warnings:

```bash
./gradlew help --warning-mode all
```

To run tests:

```bash
python run_android_tests.py
```
