# Plan: Preparing LanguageCards for Public GitHub Release

## 1. Overview

This document outlines tasks to complete before making the LanguageCards project public on GitHub. It covers security, documentation, repository hygiene, and community readiness.

---

## 2. Security & Sensitive Data

### 2.1 Verify .gitignore Coverage

**Current state:** ✅ `.gitignore` excludes `local.properties`, build dirs, `.gradle`, `.idea`, plus `*.keystore`, `*.jks`, `secrets/`, `.env`, `.env.*`.

**Actions:**
- [x] Confirm `local.properties` is never committed (run `git status` after a fresh clone; it should not appear)
- [x] Add `*.keystore` and `*.jks` if you ever add signing config (prevents accidental commit of signing keys)
- [x] Add `secrets/` or `.env` if you introduce environment variables or API keys later

### 2.2 Scan for Sensitive Content

**Current state:**
- No API keys, tokens, or credentials found in source code
- Author name "John True" is hardcoded in `MainActivity.kt` and `MainViewController.kt` — this is intentional attribution, not sensitive

**Actions:**
- [x] Decide: keep "John True" as author (fine for public) or make it configurable/build-time
- [x] Run `git log` and ensure no commits contain passwords, keys, or internal URLs
- [x] If any sensitive history exists, consider `git filter-branch` or BFG Repo-Cleaner (only if necessary)

---

## 3. Documentation

### 3.1 Root README.md

**Current state:** ✅ Root `README.md` exists with project overview, features, tech stack, build instructions, and project structure.

**Actions:**
- [x] Create `README.md` at project root with:
  - Project name and one-line description
  - Screenshots or app icon (optional)
  - Features (flashcards, hit/miss tracking, English–French/Spanish, etc.)
  - Tech stack (Kotlin Multiplatform, Compose Multiplatform, Android, iOS)
  - Prerequisites (JDK 21, Android Studio, Xcode for iOS)
  - Build instructions:
    - Android: `./gradlew :androidApp:assembleDebug`
    - iOS: link to `iosApp/README.md` or inline key steps
  - Project structure (`shared/`, `androidApp/`, `iosApp/`)
  - License (e.g. MIT, Apache 2.0)
  - Contributing (link to CONTRIBUTING.md or brief note)

### 3.2 License File

**Current state:** ✅ MIT `LICENSE` file exists; copyright notice added to README.

**Actions:**
- [x] Choose a license (MIT and Apache 2.0 are common for open-source apps)
- [x] Add `LICENSE` file with full license text
- [x] Add license/copyright notice in README (e.g. "© 2026 John True. Licensed under MIT.")

### 3.3 Contributing Guidelines (Optional but Recommended)

**Actions:**
- [x] Create `CONTRIBUTING.md` with:
  - How to report bugs (use GitHub Issues)
  - How to propose features
  - Code style (Kotlin conventions, Compose patterns)
  - Pull request process (branch naming, what to include)

### 3.4 Plan Documents

**Current state:** Planning docs are in `docs/`:
- `docs/SPEC.md` — product spec
- `docs/CROSS_PLATFORM_PLAN.md` — KMP migration plan
- `docs/LANGUAGE_COMBO_PLAN.md` — language combination design
- `docs/SQLITE_MIGRATION_PLAN.md` — SQLite migration plan

**Actions:**
- [x] Link to `docs/` from README for contributors who want design context

---

## 4. Repository Hygiene

### 4.1 Build Artifacts

**Current state:** ✅ `build/` directories are in `.gitignore`. No build artifacts are tracked.

**Actions:**
- [x] Run `git status` and ensure no build artifacts are staged or committed
- [x] If any were committed historically, add to `.gitignore` and remove from tracking:  
  `git rm -r --cached androidApp/build shared/build` (etc.)

### 4.2 IDE and Tooling

**Current state:** `.idea/` is partially ignored (workspace, caches, etc.). Some `.idea` files are tracked (e.g. `gradle.xml`, `runConfigurations.xml`).

**Actions:**
- [x] Review tracked `.idea` files — `gradle.xml` and `misc.xml` use `$PROJECT_DIR$` (portable). Keeping them can help contributors.
- [x] Ensure `.idea/workspace.xml` and user-specific files are ignored (already in `.idea/.gitignore` for workspace)
- [x] Consider adding `.idea/` to root `.gitignore` if you prefer a clean repo (considered; keeping `.idea` for contributor convenience)

### 4.3 Gradle Wrapper

**Current state:** ✅ `gradlew` has executable bit set; Gradle 9.2.1 is stable.

**Actions:**
- [x] Ensure `gradlew` and `gradlew.bat` are executable (`chmod +x gradlew` on Unix)
- [x] Verify `gradle-wrapper.properties` uses a stable Gradle version (currently 9.2.1)

### 4.4 iOS App in Settings

**Current state:** `settings.gradle.kts` includes only `:shared` and `:androidApp`. The `iosApp/` folder exists but is not a Gradle module (it's an Xcode project).

**Actions:**
- [x] Document in README that iOS requires Xcode and manual setup (see `iosApp/README.md`)
- [x] No change needed to `settings.gradle.kts` — iOS is built via Xcode, not Gradle

---

## 5. Code and Content

### 5.1 Author Attribution

**Current state:** ✅ `authorName = "John True"` kept; About dialog now says "Licensed under MIT" (aligned with LICENSE).

**Actions:**
- [x] Keep as-is for attribution, or move to a shared constant/build config if you want it configurable
- [x] Ensure "All rights reserved" in About dialog aligns with chosen license (e.g. MIT says "without restriction")

### 5.2 Package and Application ID

**Current state:** `net.thetrues.languagecards` — fine for public use.

**Actions:**
- [x] No change needed unless you want a different domain/namespace

### 5.3 Sample Data

**Current state:** `SampleData.kt` contains hardcoded French and Spanish cards. Content is educational, not sensitive.

**Actions:**
- [x] No change needed for public release
- [x] Consider adding a short comment that content is sample/example data

---

## 6. GitHub Repository Setup

*These steps are done on **github.com** (repo → Settings / About / main branch), not in the codebase.*

### 6.1 Project check (in-repo, verified)

| Item | Status |
|------|--------|
| GitHub Actions workflows | ✅ `.github/workflows/android.yml`, `.github/workflows/ios.yml` present |
| README with description, build, structure, license | ✅ Root `README.md` |
| LICENSE file | ✅ MIT in repo root |
| CONTRIBUTING.md | ✅ Present |
| .gitignore (secrets, build, local.properties) | ✅ Covers `local.properties`, `build/`, `*.keystore`, `secrets/`, `.env` |

### 6.2 Repository settings (do on GitHub)

**Where:** Repo → **About** (pencil icon) or **Settings**.

| Action | Where | Copy-paste / notes | Status |
|--------|--------|---------------------|--------|
| **Add description** | About → Description | `Flashcard app for learning vocabulary (English ↔ French/Spanish/German). Kotlin Multiplatform + Compose.` | ✅ Done |
| **Add topics** | About → Topics | `kotlin-multiplatform` `compose-multiplatform` `flashcards` `language-learning` `android` `ios` | ✅ Done |
| **Enable Issues** | Settings → General → Features | Check **Issues** | ✅ Done |
| **Branch protection (optional)** | Settings → Branches → Add rule | Branch name: `main`; require PR, status checks, etc. | ✅ Done |

### 6.3 Before / after first push

| Action | Notes |
|--------|--------|
| Working tree clean | `git status` / `git diff` — commit all changes before push |
| Final build | `./gradlew :androidApp:assembleDebug` (and iOS on macOS if desired) |
| Initial release tag (optional) | After first push: `git tag v0.1.0 && git push origin v0.1.0` |

---

## 7. Checklist Summary

| Category | Task | Priority | Status |
|----------|------|----------|--------|
| **Security** | Verify .gitignore; no secrets in history | High | ✅ |
| **Documentation** | Create root README.md | High | ✅ |
| **Legal** | Add LICENSE file | High | ✅ |
| **Documentation** | Create CONTRIBUTING.md | Medium | ✅ |
| **Hygiene** | Ensure no build artifacts committed | High | (verify with `git status`) |
| **Documentation** | Organize or document plan files (SPEC, etc.) | Low | ✅ |
| **CI** | Add GitHub Actions build (optional) | Low | ✅ |
| **GitHub (on github.com)** | Add repo description, topics; enable Issues; branch protection | Medium | ✅ Done |

---

## 8. Suggested README Structure (Draft)

```markdown
# Language Cards

A flashcard app for learning vocabulary in English ↔ French and English ↔ Spanish. Built with Kotlin Multiplatform and Compose Multiplatform.

## Features

- Bidirectional practice (English → Foreign and Foreign → English)
- Hit/miss tracking to prioritize difficult cards
- Sample decks: French Basics, Past Tense, Conversation; Spanish Basics

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
./gradlew :androidApp:assembleDebug

### iOS
See [iosApp/README.md](iosApp/README.md).

## Project Structure

- `shared/` — shared Kotlin code, Compose UI, models, session logic
- `androidApp/` — Android application
- `iosApp/` — iOS app (SwiftUI host for Compose)

## License

[Your chosen license, e.g. MIT]

© 2026 [Your Name]. All rights reserved.
```

---

*Document version: 1.0 — GitHub Public Release Plan*
