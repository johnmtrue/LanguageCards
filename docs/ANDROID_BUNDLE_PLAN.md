# Plan: Android App Bundle (AAB)

## 1. Overview

This document describes how Language Cards produces an **Android App Bundle** (`.aab`) for distribution. App Bundles are the recommended format for Google Play and enable optimized delivery (smaller downloads via dynamic delivery).

---

## 2. Why App Bundle

| Format | Use case |
|--------|----------|
| **APK** | Direct install (sideload, debug, some stores). Single artifact per ABI/variant. |
| **AAB** | Google Play upload. Play Store generates optimized APKs per device (split by ABI, language, screen density). |

For Play Store submission, Google requires (or strongly recommends) the App Bundle format. CI should produce both APKs (for testing/artifacts) and a release AAB (for Play or archival).

---

## 3. Gradle Tasks

The Android Application plugin provides:

| Task | Output |
|------|--------|
| `./gradlew :androidApp:assembleRelease` | Release APK(s) |
| `./gradlew :androidApp:bundleRelease` | Release App Bundle (`.aab`) |

**Output paths:**

- Release APK: `androidApp/build/outputs/apk/release/androidApp-release.apk`
- Release AAB: `androidApp/build/outputs/bundle/release/androidApp-release.aab`

Signing uses the same config as the release APK (CI: debug keystore; production: upload key or Play App Signing).

---

## 4. CI: GitHub Actions

The Android workflow (`.github/workflows/android.yml`) should:

1. Run unit tests.
2. Build debug APK.
3. Generate debug keystore (for release signing in CI).
4. Build release APK.
5. **Build release App Bundle** (`bundleRelease`).
6. Upload artifacts: APKs and the AAB.

Artifacts can be downloaded from the Actions run for local testing or Play upload.

---

## 5. Steps to Get a Signed Build Running

### 5.1 CI (GitHub Actions)

The Android workflow already produces a **signed** release APK and AAB by:

1. **Creating a keystore** before release builds  
   The workflow runs “Generate debug keystore for release signing” and writes `$HOME/.android/debug.keystore` with a fixed alias/passwords. The app’s `build.gradle.kts` uses the `ci` signing config when `CI=true` (set by GitHub Actions), which points at this keystore.

2. **Building in the right order**  
   - Run unit tests  
   - Build debug APK (optional, for artifacts)  
   - **Generate the keystore** (must run before any release build)  
   - Build release APK: `./gradlew :androidApp:assembleRelease`  
   - Build release bundle: `./gradlew :androidApp:bundleRelease`  
   - Upload the release APK and AAB as artifacts  

3. **Ensuring the keystore step runs**  
   If you add or reorder steps, keep “Generate debug keystore for release signing” **before** both `assembleRelease` and `bundleRelease`. The `ci` config in `androidApp/build.gradle.kts` expects:
   - `CI` env var set (e.g. `true` in GitHub Actions)
   - Keystore at `$HOME/.android/debug.keystore` (Linux) or `%USERPROFILE%\.android\debug.keystore` (Windows)

4. **Verifying the signed build (optional)**  
   To confirm the release AAB is signed, add a step after the bundle task:
   ```yaml
   - name: Verify release AAB is signed
     run: |
       apksigner verify --verbose androidApp/build/outputs/bundle/release/androidApp-release.aab 2>/dev/null || \
       jarsigner -verify -verbose -certs androidApp/build/outputs/apk/release/androidApp-release.apk
   ```
   (Use `jarsigner` for APK; for AAB, `bundletool` or `apksigner` depending on your runner image.)

### 5.2 Local signed release build

1. **Option A: Use debug signing (no extra setup)**  
   Locally, if `CI` is not set, the release build uses the **debug** signing config (Android’s default debug keystore). So a signed release APK/AAB works out of the box:
   ```bash
   ./gradlew :androidApp:assembleRelease
   ./gradlew :androidApp:bundleRelease
   ```
   Outputs are under `androidApp/build/outputs/`.

2. **Option B: Use a release keystore**  
   - Create a release keystore (one-time):
     ```bash
     keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias \
       -keyalg RSA -keysize 2048 -validity 10000
     ```
   - Add a `release` signing config in `androidApp/build.gradle.kts` that reads from this file (e.g. via `storeFile file(System.getenv("RELEASE_KEYSTORE_PATH") ?: "my-release-key.keystore")` and env vars for passwords), or use a `keystore.properties` file (add to `.gitignore`).  
   - Point the `release` build type at this config when not in CI (e.g. `signingConfig = if (System.getenv("CI") == "true") signingConfigs.getByName("ci") else signingConfigs.getByName("release")`).  
   - Run the same Gradle commands; the APK and AAB will be signed with your release key.

### 5.3 Production / Play Store signing (optional)

- **Play App Signing**: You can use an upload key (keystore) and let Google manage the app signing key. Store the upload keystore as a GitHub secret (e.g. base64), decode it in the workflow into `$HOME/.android/upload.keystore`, and add a signing config that uses it when a secret is present (e.g. `ANDROID_UPLOAD_KEYSTORE_BASE64`).  
- **Secrets**: Do not commit keystore files or passwords. Use GitHub Actions secrets for `storePassword`, `keyPassword`, and the keystore file content.  
- **Order**: In the workflow, create the keystore file from the secret **before** running `assembleRelease` and `bundleRelease`.

---

## 6. Local Commands

```bash
# Release bundle only (signed with debug or release config per section 5)
./gradlew :androidApp:bundleRelease

# Output
# androidApp/build/outputs/bundle/release/androidApp-release.aab
```

---

## 7. Checklist

- [x] Document AAB purpose and Gradle task
- [x] Add `bundleRelease` step to GitHub Android workflow
- [x] Upload AAB as workflow artifact with APKs
- [x] Document steps to get signed build running (CI and local)
- [ ] (Optional) Add signature verification step in CI
- [ ] (Optional) Add GitHub Release step to attach AAB on tag
- [ ] (Optional) Configure Play Store signing (upload keystore in secrets)

---

*Document version: 1.1 — Android Bundle Plan*
