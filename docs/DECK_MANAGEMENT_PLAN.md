# Plan: Add/Delete Decks and Deck File Import

## 1. Current State

| Component | Current Behavior |
|-----------|------------------|
| **Data source** | `SampleData.kt` → `DatabaseSeeder` seeds SQLite on first run |
| **Schema** | `language_combo` → `deck` → `deck_card` + `card` + `card_line` |
| **DeckRepository** | `getLanguageCombinations()`, `getDeck(id)`, `addDeck()`, `addDeckFromJson()`, `deleteDeck()` |
| **Default decks** | English–French (3), English–Spanish (1), plus bundled German; "Restore default decks" available |
| **Import** | Android: file picker via `ActivityResultContracts.GetContent`; iOS: `UIDocumentPickerViewController` via `DocumentPicker.ios.kt` |
| **Delete** | Menu → "Delete deck" → dialog with deck selection |
| **Add from bundled** | Menu → "Add a deck" → pick from bundled `.deck.json` files |

---

## 2. Goals

1. **Add decks** — User can add a new deck (from file or UI).
2. **Delete decks** — User can delete a deck. When the last deck in a language set is deleted, the language set is removed.
3. **New language set via deck** — Adding a deck for a new language pair creates the language set automatically.
4. **Deck file format** — Decks can be stored in files for import.
5. **Deck files for current decks** — Create one file per existing deck.
6. **Defaults** — Current decks remain the default seed; app ships with them.

---

## 3. Deck File Format

### 3.1 Structure (JSON)

```json
{
  "languageCombo": {
    "id": "en-fr",
    "name": "English – French",
    "sideAName": "English",
    "sideBName": "French"
  },
  "deck": {
    "id": "french-1",
    "name": "French — Basics"
  },
  "cards": [
    {
      "id": "1",
      "lines": [
        { "sideA": "Hello", "sideB": ["Bonjour", "Salut"] }
      ]
    },
    {
      "id": "2",
      "lines": [
        { "sideA": "Thank you", "sideB": ["Merci"] }
      ]
    }
  ]
}
```

**Multi-line card example:**
```json
{
  "id": "fc-d1",
  "lines": [
    { "sideA": "Hi, Paul. Is everything going well?", "sideB": ["Salut, Paul, Ça va bien?"] },
    { "sideA": "Very well / Great, thanks", "sideB": ["Très bien, merci"] }
  ]
}
```

### 3.2 Rules

- **languageCombo**: If `id` exists in DB, deck is added to that combo. If not, a new language combo is created.
- **deck.id**: Must be unique. Use UUID or `{combo-id}-{slug}` for imports.
- **cards**: Array of cards; each card has `id` and `lines` (array of `{sideA, sideB}`).

### 3.3 File Location

- **Shipped defaults**: `shared/src/commonMain/composeResources/files/decks/` (or `resources/`) — bundled with app.
- **User imports**: Platform-specific (e.g. Android: `getExternalStorage` or share intent; iOS: document picker). File extension: `.deck.json` or `.json`.

---

## 4. Schema and Queries

### 4.1 New SQLDelight Queries

**LanguageCombo:**
- `deleteById(id)` — Remove language combo (after last deck deleted).

**Deck:**
- `deleteById(id)` — Remove deck.
- `countByLanguageComboId(comboId)` — Count decks in a combo.

**CardStats:**
- `deleteByCardId(cardId)` or `deleteForCardIds(cardIds)` — Remove stats before deleting cards.

**DeckCard, CardLine, Card:**
- Cascade deletes: when deck is deleted, remove `deck_card` rows; delete `card_line` and `card` for cards in the deck.
- **Recommendation**: Delete `deck_card` first; then delete `card_line` and `card` for cards that were only in this deck. Simpler: delete all cards in the deck (delete card_lines for those cards, then cards, then deck_cards, then deck).

**Delete order:** `card_stats` (for cards in deck) → `deck_card` → `card_line` (for cards in deck) → `card` (for cards in deck) → `deck` → `language_combo` (if last deck).

**Note:** `card_stats` references `card(id)`; delete stats for deck's cards before deleting cards.

### 4.2 Migration

Add new `.sq` queries; no schema change required. Add migration if needed for any new columns (e.g. `is_default` — optional).

---

## 5. Repository Layer

### 5.1 Extend DeckRepository

```kotlin
interface DeckRepository {
    fun getLanguageCombinations(): List<LanguageCombination>
    fun getDeck(id: String): Deck?

    // New
    fun addDeck(deck: Deck, languageCombo: LanguageCombination)
    fun addDeckFromJson(json: String): Result<Unit>  // or throw
    fun deleteDeck(deckId: String): Boolean  // true if deleted; removes combo if last deck
}
```

### 5.2 addDeck Logic

1. If `languageCombo.id` not in DB → insert `language_combo`.
2. Insert `deck`.
3. For each card: insert `card`, `deck_card`, `card_line` (reuse `insertCard` from DatabaseSeeder).

### 5.3 deleteDeck Logic

1. Get deck; if not found, return false.
2. Get `language_combo_id`.
3. In transaction:
   - Delete `deck_card` for this deck.
   - Get card IDs from deck.
   - For each card: delete `card_line`, then `card`.
   - Delete `deck`.
   - If `countByLanguageComboId(comboId) == 0`: delete `language_combo`.
4. Return true.

### 5.4 addDeckFromJson Logic

1. Parse JSON to `DeckFile` (or similar DTO).
2. Map to `Deck` + `LanguageCombination` + `List<Card>`.
3. Call `addDeck(deck, languageCombo)` with cards embedded in deck.
4. Handle duplicate deck ID (replace or error).

---

## 6. Deck File Generation

Create one `.deck.json` file per current deck:

| File | Content |
|------|---------|
| `decks/en-fr-french-basics.deck.json` | French — Basics |
| `decks/en-fr-french-past-tense.deck.json` | French - Past Tense |
| `decks/en-fr-french-conversation.deck.json` | French - Conversation |
| `decks/en-es-spanish-basics.deck.json` | Spanish — Basics |

Place in `shared/src/commonMain/composeResources/files/decks/` (or equivalent) so they ship with the app and can be used as templates or for "restore defaults."

---

## 7. UI Flow

### 7.1 Add Deck

- **From file**: "Import deck" → file picker → parse and add.
- **From bundled file**: Optional "Add sample deck" that loads from bundled `decks/` (e.g. for demo).

### 7.2 Delete Deck

- In StartScreen or a "Manage decks" screen: long-press or menu on a deck → "Delete deck" → confirm.
- When last deck in a combo is deleted, the combo disappears from the list.

### 7.3 New Language Set

- User imports a deck file whose `languageCombo.id` is new (e.g. `en-de` for German).
- `addDeck` creates the combo and deck; next time `getLanguageCombinations()` includes it.

---

## 8. Default Decks

- **Seed on first run**: `DatabaseSeeder` continues to seed from `SampleData` when DB is empty.
- **Restore defaults** (optional): Add "Restore default decks" that clears user-added data and re-seeds. Or: only seed if empty; no "restore" — user can re-import from bundled files.

---

## 9. Implementation Phases

### Phase 1: Schema and Repository

1. Add SQLDelight queries: `deleteDeck`, `deleteLanguageCombo`, `countDecksByComboId`, and any cascade deletes.
2. Implement `addDeck`, `deleteDeck`, `addDeckFromJson` in `SqlDelightDeckRepository`.
3. Add JSON parsing (kotlinx.serialization or org.json) for deck files.

### Phase 2: Deck Files

1. Define `DeckFile` (or similar) data class for JSON.
2. Create `decks/` directory and one `.deck.json` per current deck.
3. Add logic to load bundled deck files (for restore or import).

### Phase 3: UI

1. Add "Import deck" (file picker) and "Delete deck" (with confirmation).
2. Wire to repository.
3. Refresh `languageCombinations` after add/delete.

### Phase 4: Polish

1. Handle duplicate deck IDs (replace vs error).
2. Validate JSON structure.
3. Error handling and user feedback.

---

## 10. File Structure (Implemented)

```
shared/
├── src/commonMain/
│   ├── kotlin/.../
│   │   ├── data/
│   │   │   ├── DeckRepository.kt          # extended
│   │   │   ├── SqlDelightDeckRepository.kt
│   │   │   ├── DeckFileParser.kt         # JSON → Deck + LanguageCombo
│   │   │   ├── DeckFile.kt               # DTOs for JSON
│   │   │   └── ...
│   │   └── ...
│   ├── composeResources/files/
│   │   ├── en-fr-french-basics.deck.json
│   │   ├── en-fr-french-past-tense.deck.json
│   │   ├── en-fr-french-conversation.deck.json
│   │   ├── en-es-spanish-basics.deck.json
│   │   ├── en-es-spanish-conversation.deck.json
│   │   ├── en-de-german-basics.deck.json
│   │   └── en-de-german-conversation.deck.json
│   └── sqldelight/.../db/
│       ├── LanguageCombo.sq              # deleteById
│       ├── Deck.sq                       # deleteById, countByLanguageComboId
│       └── ...
├── src/iosMain/kotlin/.../
│   └── platform/
│       └── DocumentPicker.ios.kt         # UIDocumentPickerViewController for import
├── src/commonTest/kotlin/.../
│   ├── data/
│   │   └── DeckRepositoryTest.kt         # Add/delete/addFromJson tests (shared)
│   └── platform/
│       └── TestDriver.kt                 # expect createTestDriver()
├── src/androidUnitTest/kotlin/.../
│   └── platform/
│       └── TestDriver.android.kt         # actual: JdbcSqliteDriver
└── src/iosSimulatorArm64Test/kotlin/.../
    └── platform/
        └── TestDriver.ios.kt             # actual: NativeSqliteDriver
```

---

## 11. Dependencies

- **JSON**: `kotlinx.serialization` (already common in KMP) or `org.json` for simple parsing.
- **File access**: Platform-specific (Android: `Context.contentResolver`, `InputStream`; iOS: `NSFileManager`, URL).

---

## 12. Checklist

- [x] Add delete queries to SQLDelight
- [x] Implement `addDeck`, `deleteDeck`, `addDeckFromJson` in SqlDelightDeckRepository
- [x] Add DeckFileParser (JSON parsing)
- [x] Create deck JSON files for French Basics, Past Tense, Conversation, Spanish Basics
- [x] Add "Import deck" UI (file picker) — Android and iOS
- [x] Add "Delete deck" UI with confirmation
- [x] Refresh language combinations after add/delete
- [x] Test add/delete flows on Android and iOS
- [x] Overwrite prompt when importing deck with existing ID
- [x] JSON format validation and "Import failed" error dialog
- [x] DeckFileParserTest for invalid/missing JSON

---

## 13. Implementation Status (as of implementation)

| Phase | Status | Notes |
|-------|--------|-------|
| Phase 1: Schema and Repository | ✅ Done | All SQLDelight queries, repository methods, DeckFileParser |
| Phase 2: Deck Files | ✅ Done | 7 bundled `.deck.json` files in `composeResources/files/` |
| Phase 3: UI | ✅ Done | Import deck, Delete deck, Add deck from bundled, Restore defaults |
| Phase 4: Polish | ✅ Done | Overwrite prompt on duplicate deck ID; JSON validation + "Import failed" dialog; DeckFileParserTest for format errors; success Snackbar after import and add-from-bundled |

**Additional implemented:** "Add a deck" dialog (adds from bundled files), "Restore default decks", iOS `DocumentPicker.ios.kt` with `UIDocumentPickerViewController`, `HostViewControllerHolder` + Swift `setHostViewControllerForImport`, success Snackbar ("Deck imported" / "Deck added") after import and add-from-bundled.

### Testing

- **Android**: `./gradlew :androidApp:testDebugUnitTest` — `DeckRepositoryTest` (JVM) covers add/delete/addFromJson flows.
- **iOS**: `./gradlew :shared:iosSimulatorArm64Test` — shared `DeckRepositoryTest` (NativeSqliteDriver) covers add/delete/addFromJson flows. Requires macOS.
- **Manual**: See [DECK_ADD_DELETE_TEST_PROCEDURE.md](DECK_ADD_DELETE_TEST_PROCEDURE.md).

---

*Document version: 1.2 — Deck Management Plan*
