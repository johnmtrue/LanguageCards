# LanguageCards App — Game Modes & Audio Expansion Plan

## 1. Overview

This plan describes how to expand LanguageCards with multiple game modes, audio support (TTS for prompts), and configurable prompt presentation. The current "Guess" mode (I know / Show answer) will be preserved as one of three modes.

---

## 2. Proposed Features

### 2.1 Game Modes

| Mode | Description | Answer Mechanism |
|------|-------------|------------------|
| **Guess** | Current flow; self-assessment | "I know" / "Show answer" → then "I was right" / "I was wrong" |
| **Text Answer** | User types the translation | `TextField` + normalized comparison against `sideA`/`sideB` |
| **Audio Answer** | User speaks the translation | Speech-to-text (STT) + same comparison as Text mode |

### 2.2 Prompt Presentation (First Line / Quiz Line)

For all modes, the prompt can be shown in two ways:

| Option | Description | Use Case |
|--------|-------------|----------|
| **Text + Audio** | Prompt shown in text and spoken via TTS | Default; visual + aural learning |
| **Audio Only** | Prompt spoken via TTS; no text shown | Listening comprehension, pronunciation focus |

### 2.3 Text Answer Mode Options

| Option | Description | Example |
|--------|-------------|---------|
| **Strict** | Case-insensitive, trim; accents must match | "café" ≠ "cafe" |
| **Non-strict** | Same as strict, plus ignore diacritics | "café" = "cafe" |

---

## 3. Architecture Changes

### 3.1 New Models & Enums

**Location:** `shared/src/commonMain/kotlin/net/thetrues/languagecards/model/`

```
GameMode        : GUESS | TEXT_ANSWER | AUDIO_ANSWER
PromptDisplay   : TEXT_AND_AUDIO | AUDIO_ONLY
TextAnswerMode  : STRICT | NON_STRICT (ignore accents)
```

**SessionState** should carry session-level settings (or reference a `SessionSettings` object):

- `gameMode: GameMode`
- `promptDisplay: PromptDisplay`
- `textAnswerMode: TextAnswerMode` (used only when `gameMode == TEXT_ANSWER` or `AUDIO_ANSWER`)

### 3.2 Settings Infrastructure

**Location:** `shared/src/commonMain/kotlin/net/thetrues/languagecards/settings/`

- **SettingsStore** (expect/actual or repository pattern):
  - `gameMode`, `promptDisplay`, `textAnswerMode`
  - `ttsEnabled: Boolean` (global TTS on/off)
  - Optional: `defaultSessionSize`, `defaultDirection` (from SPEC.md)

- **Persistence:** DataStore (already in `libs.versions.toml`) or SharedPreferences
- **Platform:** Use `expect`/`actual` if settings differ per platform; otherwise shared KMP logic

### 3.3 Answer Matching (Shared Logic)

**Location:** `shared/src/commonMain/kotlin/net/thetrues/languagecards/answer/`

- **AnswerMatcher** (pure Kotlin):
  - `matches(userInput: String, correctAnswers: List<String>, mode: TextAnswerMode): Boolean`
  - Normalization: trim, lowercase
  - For `NON_STRICT`: strip diacritics (e.g. `Normalizer` or manual mapping)
  - Return true if `userInput` matches any of `correctAnswers`

Use existing `CardLine.sideB: List<String>` (or `sideA` for B→A) for multiple acceptable answers.

---

## 4. Platform-Specific Components

### 4.1 Text-to-Speech (TTS)

**Expect/Actual:**

- **Common:** `expect class TtsEngine` or `expect fun speak(text: String)` in shared
- **Android:** `actual` using `android.speech.tts.TextToSpeech`
- **iOS:** `actual` using `AVSpeechSynthesizer` (if KMP supports iOS)

**Integration:**

- `CardScreen` (or shared `PromptPresenter` composable) receives:
  - `promptText: String`
  - `promptDisplay: PromptDisplay`
  - `ttsEngine: TtsEngine?` (null if TTS disabled or unavailable)
- On prompt display:
  - If `AUDIO_ONLY`: speak prompt, show nothing (or placeholder like "Listen...")
  - If `TEXT_AND_AUDIO`: show text + speak
  - Optional: play button to replay audio

### 4.2 Speech-to-Text (STT) — Audio Answer Mode

**Expect/Actual:**

- **Common:** `expect class SpeechRecognizer` or `expect fun startListening(callback: (String) -> Unit)`
- **Android:** `actual` using `SpeechRecognizer` (Android Speech API)
- **iOS:** `actual` using `SFSpeechRecognizer` (if KMP supports)

**Integration:**

- In `CardScreen`, when `gameMode == AUDIO_ANSWER`:
  - Show prompt (text/audio per `promptDisplay`)
  - "Start speaking" button → start STT
  - On result, run `AnswerMatcher.matches(...)` and record hit/miss
  - Show feedback (correct/incorrect) + correct answer if wrong

---

## 5. UI Changes

### 5.1 StartScreen

Add session options before "Start":

- **Game mode:** Dropdown or segmented control: Guess | Text Answer | Audio Answer
- **Prompt display:** Text + Audio | Audio only (enable only when TTS available)
- **Text answer strictness:** Strict | Non-strict (visible only when game mode is Text Answer or Audio Answer)

Pass these into `SessionFlow.startSession(...)` (new parameters).

### 5.2 CardScreen

Refactor by mode:

| Mode | PROMPT phase | After answer |
|------|--------------|--------------|
| **Guess** | Same as now; add TTS per `promptDisplay` | Same (I know / Show answer → I was right/wrong) |
| **Text Answer** | Show/hide prompt text per `promptDisplay`, play TTS, show `TextField` | Compare input via `AnswerMatcher`, show correct/incorrect |
| **Audio Answer** | Same prompt display, show "Start speaking" button | Same comparison as Text; show result |

**Shared:**

- `PromptPresenter`: composable that shows text and/or speaks based on `promptDisplay` and `ttsEngine`
- Replay-audio button for prompt
- "Show answer" / "I don't know" always available (counts as miss)

### 5.3 Settings Screen (New)

Add "Settings" to the app menu (in `App.kt`). Settings screen contains:

- Default game mode
- Default prompt display
- Default text answer strictness
- TTS enabled (global)
- Optional: default session size, default direction

These become session defaults; `StartScreen` can override per session.

---

## 6. Data Flow Summary

```
StartScreen
  → User selects: deck, direction, gameMode, promptDisplay, textAnswerMode
  → onStart(deck, direction, SessionOptions(gameMode, promptDisplay, textAnswerMode))

SessionFlow.startSession(deck, direction, options, statsRepository)
  → SessionState(cards, direction, results, options)

CardScreen(state, onAnswer)
  → Renders based on state.options.gameMode
  → Uses PromptPresenter(prompt, promptDisplay, ttsEngine)
  → For Text Answer: TextField → AnswerMatcher → onAnswer(cardId, wasHit)
  → For Audio Answer: STT → AnswerMatcher → onAnswer(cardId, wasHit)
  → For Guess: existing flow (with optional TTS for prompt)
```

---

## 7. Implementation Phases

Use `[x]` = done, `[ ]` = not started.

### Phase 1: Foundation

- [x] 1. Add `GameMode`, `PromptDisplay`, `TextAnswerMode` enums.
- [x] 2. Add `SessionOptions` and thread it through `SessionState` and `SessionFlow`.
- [x] 3. Add `AnswerMatcher` with strict/non-strict logic.
- [x] 4. Add Settings infrastructure (DataStore) and Settings screen stub.

### Phase 2: Text Answer Mode

- [x] 1. Add Text Answer mode UI to `CardScreen` (TextField, submit button).
- [x] 2. Integrate `AnswerMatcher` with `Card.sideA`/`sideB`.
- [x] 3. Add prompt display options to `StartScreen` (text+audio / audio-only); initially "text only" until TTS is ready.
- [x] 4. Add text strictness toggle to StartScreen and Settings.

### Phase 3: Text-to-Speech

- [ ] 1. Implement `expect`/`actual` TtsEngine.
- [ ] 2. Implement `PromptPresenter` composable.
- [ ] 3. Add "Text + Audio" and "Audio only" behavior to `CardScreen`.
- [ ] 4. Add replay button for prompt.
- [ ] 5. Add TTS on/off to Settings.

### Phase 4: Audio Answer Mode

- [ ] 1. Implement `expect`/`actual` SpeechRecognizer.
- [ ] 2. Add Audio Answer mode UI to `CardScreen` (mic button, result display).
- [ ] 3. Wire STT result to `AnswerMatcher`.
- [ ] 4. Add Audio Answer to StartScreen game mode picker.

### Phase 5: Polish

- [ ] 1. Persist session options as defaults in Settings.
- [ ] 2. Handle edge cases: TTS/STT not available, permissions.
- [ ] 3. Accessibility: labels for TTS/STT buttons, TalkBack support.
- [ ] 4. Unit tests for `AnswerMatcher` (strict/non-strict, multi-answer).

---

## 8. File Structure (New/Modified)

```
shared/src/commonMain/kotlin/net/thetrues/languagecards/
├── model/
│   ├── GameMode.kt          (done)
│   ├── PromptDisplay.kt     (done)
│   ├── TextAnswerMode.kt    (done)
│   ├── SessionOptions.kt    (done)
│   └── SessionState.kt      (modified: options — done)
├── answer/
│   ├── AnswerMatcher.kt     (done)
│   ├── Diacritics.kt        (expect; Android Normalizer, iOS Latin fold map)
├── settings/
│   ├── AppSettings.kt       (done)
│   ├── SettingsStore.kt     (interface — done)
│   ├── SettingsStoreFactory.kt (expect createSettingsStore — done)
│   ├── AndroidSettingsStore.kt (androidMain — DataStore Preferences)
│   └── IosSettingsStore.kt  (iosMain — NSUserDefaults)
├── session/
│   └── SessionFlow.kt       (modified: accept options — done)
├── ui/
│   ├── screens/
│   │   ├── StartScreen.kt   (game mode, prompt display, answer matching, defaults from settings — done)
│   │   ├── CardScreen.kt    (Guess vs typed answer; AnswerMatcher; prompt captions — done)
│   │   └── SettingsScreen.kt (persist defaults + TTS toggle; menu → Settings — done)
│   └── components/
│       └── PromptPresenter.kt (new)
└── audio/                   (optional grouping)
    ├── TtsEngine.kt         (expect)
    └── SpeechRecognizer.kt  (expect)

shared/src/androidMain/.../actual/
├── TtsEngine.android.kt     (actual)
└── SpeechRecognizer.android.kt (actual)

shared/src/iosMain/.../actual/
├── TtsEngine.ios.kt         (actual, if supporting iOS)
└── SpeechRecognizer.ios.kt  (actual)
```

---

## 9. Testing Strategy

- **AnswerMatcher:** Unit tests for trim, case, diacritics, multiple answers.
- **SessionFlow:** Verify options flow through.
- **CardScreen:** Manual/UI tests for each mode and prompt display.
- **TTS/STT:** Platform integration tests or manual verification.

---

## 10. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| TTS/STT not available on device | Graceful fallback: hide audio-only, disable Audio mode if no STT |
| Accent normalization varies by locale | Use `java.text.Normalizer` (JVM) or Kotlin/Native equivalent; document limitations |
| KMP expect/actual for iOS | Implement only Android first; add iOS when ready |
| Session options complexity | Keep `SessionOptions` minimal; expand gradually |

---

*Document version: 1.0 — Expansion Plan for Game Modes & Audio*
