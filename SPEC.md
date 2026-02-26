# Language Cards — Android Application Specification

## 1. Overview

**Language Cards** is an Android application that helps users learn foreign words and phrases through flashcard-style practice. The app shows a card in one language (English or the target foreign language); the user guesses the translation in the other language. The app tracks correct and incorrect answers to prioritize cards that need more practice.

### 1.1 Goals

- **Learn vocabulary**: Expose users to words/phrases in English ↔ target language.
- **Bidirectional practice**: Support both “English → foreign” and “foreign → English” directions.
- **Adaptive practice**: Use hit/miss data to surface difficult cards more often.
- **Simple, focused UX**: Minimal friction so users can practice in short sessions.

### 1.2 Target Platform

- **Platform**: Android (phone and tablet).
- **Minimum SDK**: TBD (e.g. API 24+ recommended).
- **Language**: App UI in English; content in English + one or more foreign languages per deck.

---

## 2. Core Concepts

### 2.1 Card

A **card** represents a single vocabulary item or phrase.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Unique ID | Stable identifier (e.g. UUID). |
| `sideA` | String | Text in the first language (e.g. English). |
| `sideB` | String | Text in the second language (e.g. Spanish). |
| `languageA` | Language code | e.g. `en`. |
| `languageB` | Language code | e.g. `es`. |
| `notes` | String (optional) | Optional hint or context. |

- **Direction**: A session or round can be “show A, guess B” or “show B, guess A”. The spec treats these as two logical directions per card.

### 2.2 Deck

A **deck** is a collection of cards, typically for one language pair and topic.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Unique ID | Stable identifier. |
| `name` | String | Display name (e.g. "Spanish — Food"). |
| `languageA` | Language code | Primary language (e.g. English). |
| `languageB` | Language code | Target language. |
| `cardIds` | List of card IDs | Cards in this deck (order optional). |

### 2.3 Practice Session

A **session** is one continuous period of practice.

- User selects a deck (and optionally direction, count, or “need practice” filter).
- App presents a sequence of cards.
- For each card, the app records:
  - Which card and direction.
  - **Hit** (correct) or **Miss** (incorrect).
  - Timestamp (optional, for future analytics or spacing).

---

## 3. Hit/Miss Tracking and Practice Prioritization

### 3.1 Per-Card, Per-Direction Stats

Tracking is **per card and per direction** (A→B and B→A), so the app can prioritize “hello → hola” separately from “hola → hello”.

**Suggested stored stats (per card + direction):**

| Field | Type | Description |
|-------|------|-------------|
| `hits` | Non-negative integer | Number of correct answers. |
| `misses` | Non-negative integer | Number of incorrect answers. |
| `lastPracticedAt` | Timestamp (optional) | Last time this card+direction was practiced. |

Derived values:

- **Success rate**: `hits / (hits + misses)` when total attempts > 0.
- **Need practice**: Cards (or card+direction) with low success rate or high misses relative to hits.

### 3.2 When to Record

- **Hit**: User’s answer is judged correct (exact match, normalized match, or “marked correct” by user).
- **Miss**: User’s answer is wrong, or user taps “I don’t know” / “Show answer”.

Optional: treat “Show answer” and “Wrong answer” separately (e.g. weight misses differently) in a later version.

### 3.3 How to Prioritize Cards

The app should **prefer showing cards that need more practice**. Possible strategies:

1. **Weak-first**: Sort by success rate (ascending), then by `misses` (descending). Show weaker cards more often in a session.
2. **Spaced repetition (future)**: Use `lastPracticedAt` and success rate to schedule “next review” time; prioritize due cards.
3. **Hybrid**: A portion of session is “need practice” cards, the rest random or round-robin.

**Recommendation for v1**: Implement weak-first (or a simple score like `misses - hits` per card+direction) so high-miss cards appear more often. Keep storage ready for `lastPracticedAt` if you add spacing later.

### 3.4 Persistence

- Stats must **persist** across app restarts (e.g. Room DB, DataStore, or SQLite).
- Stats are **user-local** (no multi-user requirement in this spec).

---

## 4. User Flows

### 4.1 Choose Deck and Start Session

1. User opens the app.
2. Home (or deck list) shows available decks.
3. User selects a deck.
4. Optional: choose direction (A→B, B→A, or random), and session length (e.g. 10 cards, 20 cards, “all”).
5. Optional: toggle “Practice weak cards only”.
6. User taps **Start** (or equivalent). App builds an ordered list of cards (prioritizing weak card+directions) and starts the session.

### 4.2 Answering a Card

1. App shows one side of the card (e.g. “hello” in English).
2. User thinks of the translation (e.g. “hola”).
3. User either:
   - **Submit answer**: Types (or speaks, if supported) the translation. App compares to `sideB` (normalized: trim, case-insensitive, optional diacritics).
   - **Reveal answer**: Taps “Show answer” or “I don’t know”. Count as **miss**.
4. App shows correct answer and feedback (correct ✓ / incorrect ✗).
5. App records **hit** or **miss** for this card + direction and updates stored stats.
6. App advances to next card (or ends session if no more cards).

### 4.3 Session End

1. After the last card, show a short **session summary**:
   - Number of hits and misses.
   - Optional: success rate, “cards that need more practice” list.
2. Actions: **Practice again** (same or different options), **Back to decks**, **Home**.

### 4.4 Viewing Progress (Optional)

- **Deck or global stats**: List cards (or card+direction) with lowest success rate or most misses.
- **History**: Optional list of recent sessions (count, hits, misses, date).

---

## 5. Direction of Play

- **Configurable per session**: User can choose “English → Foreign”, “Foreign → English”, or “Random” (each card’s direction chosen at random).
- **Consistent with tracking**: Stored stats are always per (card, direction), so both directions can be practiced and prioritized independently.

---

## 6. Answer Checking

- **Normalization**: Trim whitespace; optional case-insensitive comparison; optional ignore diacritics (e.g. “café” = “cafe”).
- **Exact vs. flexible**: v1 can be “single correct answer” (exact/normalized). Future: multiple acceptable answers, synonyms, or “mark as correct” by user.
- **Feedback**: Clear “Correct” / “Incorrect” and display of the correct answer after each attempt.

---

## 7. Data Model Summary

```
Deck
  id, name, languageA, languageB
  → list of Card IDs

Card
  id, sideA, sideB, languageA, languageB, notes?

CardStats (per card + direction)
  cardId, direction (A→B | B→A), hits, misses, lastPracticedAt?

Session (optional, for history)
  id, deckId, startedAt, endedAt, totalCards, hits, misses
```

---

## 8. UI/UX Guidelines

- **Cards**: One card per screen (or one card visible at a time). Large, readable text for the prompt; clear input or “Show answer” CTA.
- **Feedback**: Immediate visual (and optional haptic) feedback on hit/miss.
- **Accessibility**: Support TalkBack, sufficient contrast, and scalable text.
- **Offline-first**: Decks and progress stored locally; no login required for core flow (optional account/sync can be added later).

---

## 9. Technical Considerations (Android)

- **Storage**: Room (SQLite) recommended for cards, decks, and CardStats; DataStore or SharedPreferences for simple settings (e.g. default direction, session length).
- **Architecture**: MVVM or MVI; single source of truth for session state (current card index, current deck, pending hits/misses).
- **Testing**: Unit tests for answer normalization, prioritization logic, and hit/miss aggregation; UI tests for main flows (start session, answer card, see summary).
- **Localization**: App strings in `strings.xml` (and optionally more locales later); deck content is data (language codes on cards/decks).

---

## 10. Out of Scope (for Initial Version)

- Account login / cloud sync.
- Audio pronunciation (TTS or pre-recorded).
- Images on cards.
- Spaced repetition with due dates (only “weak first” prioritization).
- Multiple correct answers or synonym support (can be added later with same hit/miss model).

---

## 11. Success Criteria

- User can select a deck and complete a session (see card, answer or reveal, get feedback).
- Every answer is recorded as hit or miss per card and direction.
- Cards (or card+directions) with more misses and lower success rate are shown more often in subsequent sessions.
- Progress persists across app restarts.

---

## 12. Future Enhancements

- Spaced repetition (e.g. next review date per card+direction).
- Multiple acceptable answers and “mark as correct”.
- TTS for pronunciation.
- Import/export decks (e.g. CSV, JSON).
- Themes (light/dark/system).
- Widget or shortcut for “quick practice” with weak cards.

---

*Document version: 1.0 — Language Cards Android Spec*
