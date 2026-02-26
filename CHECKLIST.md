# Language Cards — Minimum Version Checklist

One deck, French only, self-report right/wrong after "I know". Track hits/misses and show a summary at the end.

---

## Data & models

- [x] Create a `Card` data class: `id`, `sideA` (English), `sideB` (French).
- [x] Create a `Deck` (or single-deck config): holds a list of `Card`s.
- [x] Add 5–10 hardcoded French/English cards (e.g. hello/bonjour, thank you/merci, good morning/bonjour…).
- [x] Create a way to store **hit/miss per card** (in-memory for prototype, or Room from the start): e.g. `CardStats` with `cardId`, `hits`, `misses`.

---

## Navigation & session flow

- [x] Define session flow: **card screen** → (after response) → **next card** or **summary screen**.
- [x] Implement a simple session state: current card index, list of cards for this run, and collected hit/miss counts for the session.
- [x] After last card, navigate to **summary screen** (don’t loop back to first card).

---

## Card screen (main practice UI)

- [x] Show one side of the current card only (e.g. English **or** French — pick one direction for the minimum version, e.g. English → French).
- [x] Two buttons: **"I know"** and **"Show answer"**.
  - **"Show answer"**: count as **miss** for this card, show correct answer, then advance to next card (or summary).
  - **"I know"**: do **not** count yet; show the answer and two follow-up buttons.
- [x] After **"I know"**, show the translation and two buttons: **"I was right"** and **"I was wrong"**.
  - **"I was right"**: count **hit** for this card, then go to next card (or summary).
  - **"I was wrong"**: count **miss** for this card, then go to next card (or summary).
- [x] After each card (whether "Show answer" or "I was right/wrong"), update stored hit/miss for that card and move to next card or summary.

---

## Hit/miss tracking

- [x] When user commits (Show answer = miss, I was right = hit, I was wrong = miss), update **per-card** hit/miss (in memory or DB).
- [x] Ensure each card’s stats are updated exactly once per card shown (no double-counting).

---

## Summary screen

- [x] After the last card, show a **summary** screen with:
  - Total cards in the session.
  - Total **hits** (correct) and **misses** (wrong / show answer).
  - Optional: list each card and whether it was hit or miss.
- [x] Provide a way to **start again** (same deck, new session) or exit.

---

## Polish & runnability

- [x] App launches to the card screen (or a minimal “Start” then card screen) with the single deck.
- [x] Run on emulator or device; complete one full pass: several cards, mix of "I know" → right/wrong and "Show answer", then see summary.
- [x] Confirm hit/miss values persist for the session and (if you added storage) across app restarts.

---

## Optional for minimum version (skip if time‑boxed)

- [ ] Persist hit/miss across app restarts (e.g. Room or DataStore).
- [ ] "Practice again" from summary without restarting the app.
- [ ] Reverse direction (French → English) or second deck.

---

*When all required checkboxes are done, you have a minimum playable version: one French deck, I know / Show answer, I was right / I was wrong, per-card hit/miss, and end summary.*
