# Plan: Language Combination Selection

## Overview

Add support for multiple language combinations (e.g. English–French, English–Spanish). Users select a language combination first, then a deck within that combination. Decks are grouped under their language combination.

---

## Implementation Status: ✅ Complete

| Phase | Status |
|-------|--------|
| Phase 1: Data model | ✅ Done |
| Phase 2: StartScreen UI | ✅ Done |
| Phase 3: App wiring | ✅ Done |
| Phase 4: Stats (card ID uniqueness) | ✅ Verified |

---

## Implemented State

- **Language combinations**: `SampleData.languageCombinations` — English–French (2 decks), English–Spanish (1 deck)
- **Card model**: `sideA` / `sideB` generic; semantics from `LanguageCombination.sideAName` / `sideBName`
- **StartScreen**: Language combination picker → Deck picker (filtered) → Practice direction (dynamic labels)
- **App.kt**: Passes `SampleData.languageCombinations` to `StartScreen`
- **Card IDs**: Globally unique (French: `1`–`72`, `pt-1`–`pt-20`; Spanish: `es-1`–`es-10`)

---

## Target State (Achieved)

### User flow

1. **Language combination** — Dropdown/radio: "English – French" | "English – Spanish" | …
2. **Deck** — Dropdown/radio: decks for the selected combination only
3. **Practice direction** — Labels adapt: "English → French" or "Spanish → English", etc.
4. **Start** — Same as today

### Data structure

```
LanguageCombination (id, name, sideAName, sideBName, decks)
  └── Deck (id, name, cards)
        └── Card (id, sideA, sideB)  // sideA/sideB are generic; combo defines semantics
```

---

## Implementation Plan

### Phase 1: Data model

1. **Add `LanguageCombination`** in `shared/.../model/`

   ```kotlin
   data class LanguageCombination(
       val id: String,
       val name: String,           // e.g. "English – French"
       val sideAName: String,      // e.g. "English"
       val sideBName: String,      // e.g. "French"
       val decks: List<Deck>,
   )
   ```

2. **Keep `Deck` and `Card` as-is** — `Card.sideA` / `sideB` stay generic; semantics come from the combo.

3. **Restructure `SampleData`**

   - `languageCombinations: List<LanguageCombination>`
   - English–French: existing `defaultDeck`, `pastTenseDeck`
   - English–Spanish: add a small sample deck (e.g. 5–10 cards) for testing

4. **Add sample Spanish data** — Minimal set (e.g. Hello→Hola, Thank you→Gracias, …) to validate the flow.

---

### Phase 2: StartScreen UI

1. **Language combination picker**

   - Section label: "Language combination"
   - Radio buttons or `DropdownMenu` for each `LanguageCombination.name`

2. **Deck picker**

   - Section label: "Deck"
   - Show only `selectedCombo.decks`
   - When combo changes, reset `selectedDeck` to first deck of new combo

3. **Practice direction**

   - Labels from combo: `"${combo.sideAName} → ${combo.sideBName}"` and `"${combo.sideBName} → ${combo.sideAName}"`
   - `PracticeDirection` enum unchanged (A_TO_B, B_TO_A)

4. **StartScreen signature**

   ```kotlin
   fun StartScreen(
       languageCombinations: List<LanguageCombination>,
       onStart: (Deck, PracticeDirection) -> Unit,
       onExit: () -> Unit,
       ...
   )
   ```

---

### Phase 3: App wiring ✅ Implemented

1. **App.kt**

   - Replace `SampleData.decks` with `SampleData.languageCombinations` ✅
   - Pass `languageCombinations` to `StartScreen` ✅

2. **SessionFlow / SessionState**

   - No changes — still `(Deck, PracticeDirection)` ✅

3. **CardScreen / SummaryScreen**

   - No changes — they use `state.direction` and `card.sideA` / `card.sideB` generically ✅

---

### Phase 4: Stats ✅ Verified

- **Current**: `StatsRepository` keys by `cardId` only.
- **Consideration**: Card IDs should be unique across all decks if stats are global.
- **Verified**: French cards use `1`–`72`, `pt-1`–`pt-20`; Spanish use `es-1`–`es-10`. No collisions. No API change needed.

---

## File changes summary

| File | Change |
|------|--------|
| `model/LanguageCombination.kt` | **New** — data class |
| `model/Deck.kt` | No change |
| `model/Card.kt` | No change (optionally relax "English/French" comments) |
| `model/PracticeDirection.kt` | No change |
| `data/SampleData.kt` | Restructure to `languageCombinations`; add Spanish sample deck |
| `ui/screens/StartScreen.kt` | Add combo picker; filter decks; dynamic direction labels |
| `ui/App.kt` | Pass `languageCombinations` instead of `decks` |

---

## Sample Spanish deck (for testing)

```kotlin
// Minimal English–Spanish deck
Card("es-1", "Hello", "Hola"),
Card("es-2", "Thank you", "Gracias"),
Card("es-3", "Goodbye", "Adiós"),
Card("es-4", "Yes", "Sí"),
Card("es-5", "No", "No"),
// ... a few more
```

---

## Out of scope (future)

- Adding new language combinations via UI
- Importing decks from file
- Localization of UI strings (e.g. "Language combination" in multiple languages)
