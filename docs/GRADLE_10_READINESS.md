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

## Known Limitations

- **Archives configuration deprecation**: The `archives` configuration is deprecated and will fail in Gradle 10. The warning originates from transitive plugins (Kotlin Multiplatform, Android KMP, or Compose), not from project build scripts. Resolution depends on plugin updates. Tracked in [gradle/gradle#36296](https://github.com/gradle/gradle/issues/36296).

- **androidLibrary block deprecation**: The Kotlin Multiplatform DSL reports `androidLibrary` as deprecated in favor of `android`. The `android` block has a different API (KotlinAndroidTarget) and does not support the Android KMP library configuration (namespace, compileSdk, etc.). Retain `androidLibrary` until the Android KMP plugin provides a migration path.

## Verification

To check for deprecation warnings:

```bash
./gradlew help --warning-mode all
```

To run tests:

```bash
python run_android_tests.py
```
