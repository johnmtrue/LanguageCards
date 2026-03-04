# Plan: Moving Cards to SQLite

## 1. Current State

| Data | Location | Format |
|------|----------|--------|
| **Cards, Decks, LanguageCombinations** | `SampleData.kt` | Hardcoded Kotlin objects |
| **CardStats** (hits/misses) | `StatsRepository` → DataStore | Serialized string in Preferences |

**Models:**
- `Card`: `id`, `lines: List<CardLine>` (multi-line support; last line is quizzed)
- `CardLine`: `sideA`, `sideB: List<String>` (multiple acceptable answers)
- `Deck`: `id`, `name`, `cards: List<Card>`
- `LanguageCombination`: `id`, `name`, `sideAName`, `sideBName`, `decks: List<Deck>`
- `CardStats`: `cardId`, `hits`, `misses`

**Consumers:**
- `App.kt` → `SampleData.languageCombinations` → `StartScreen`
- `SessionFlow` → receives `Deck` with cards
- `StatsRepository` → `AndroidStatsStore` / `IosStatsStore` (DataStore)

---

## 2. Target Architecture

Use **SQLDelight** for Kotlin Multiplatform SQLite. It provides:
- Type-safe SQL, compile-time verification
- Shared schema and queries across Android and iOS
- Platform drivers: `android-driver`, `native-driver` (iOS)

**Scope of migration:**
1. **Cards + Decks + LanguageCombinations** → SQLite (replace `SampleData`)
2. **CardStats** → SQLite (replace DataStore; unify persistence in one DB)

---

## 3. Schema Design

### 3.1 Normalized Tables

```
language_combo
  id TEXT PRIMARY KEY
  name TEXT NOT NULL
  side_a_name TEXT NOT NULL
  side_b_name TEXT NOT NULL

deck
  id TEXT PRIMARY KEY
  name TEXT NOT NULL
  language_combo_id TEXT NOT NULL REFERENCES language_combo(id)

card
  id TEXT PRIMARY KEY

deck_card
  deck_id TEXT NOT NULL REFERENCES deck(id)
  card_id TEXT NOT NULL REFERENCES card(id)
  position INTEGER NOT NULL
  PRIMARY KEY (deck_id, card_id)

card_line
  id TEXT PRIMARY KEY
  card_id TEXT NOT NULL REFERENCES card(id)
  line_index INTEGER NOT NULL
  side_a TEXT NOT NULL
  side_b TEXT NOT NULL  -- JSON array: ["ans1", "ans2"]

card_stats
  card_id TEXT PRIMARY KEY REFERENCES card(id)
  hits INTEGER NOT NULL DEFAULT 0
  misses INTEGER NOT NULL DEFAULT 0
```

**CardLine storage:** Store `sideB` as JSON array (e.g. `["Bonjour","Salut"]`) for multiple answers. SQLDelight doesn't require JSON support; we can parse in Kotlin with `kotlinx.serialization` or a simple split/join.

**Alternative:** Separate `card_line_answer` table for each answer. Simpler queries but more joins. For v1, JSON in a single column is acceptable.

### 3.2 SQLDelight Schema

Create `.sq` files in `shared/src/commonMain/sqldelight`:

- `LanguageCombo.sq` – table
- `Deck.sq` – table
- `Card.sq` – table
- `DeckCard.sq` – junction table
- `CardLine.sq` – table
- `CardStats.sq` – table
- Queries for: `getAllLanguageCombinations`, `getDecksByCombo`, `getCardsByDeck`, `getCardLines`, `getStats`, `record`, `clearStats`, etc.

---

## 4. Migration Phases

### Phase 1: Add SQLDelight and Schema

1. **Add SQLDelight plugin and dependencies**
   - `app.cash.sqldelight` plugin (version ~2.0.x)
   - `sqlite-driver` (commonMain)
   - `android-driver` (androidMain)
   - `native-driver` (iosMain)

2. **Create schema and queries**
   - Define `.sq` files
   - Generate Kotlin code via Gradle

3. **Platform drivers**
   - Android: `AndroidSqliteDriver` with `context.getDatabasePath("languagecards.db")`
   - iOS: `NativeSqliteDriver` with path from `NSFileManager` (Documents or app support)

4. **Database initialization**
   - Create `DatabaseDriverFactory` (expect/actual) in `commonMain` / `androidMain` / `iosMain`
   - Single `Database` instance (singleton) per platform

### Phase 2: Repository Layer

1. **Define `DeckRepository` interface** (commonMain)
   - `suspend fun getLanguageCombinations(): List<LanguageCombination>`
   - `suspend fun getDeck(id: String): Deck?`
   - Or: `fun getLanguageCombinations(): Flow<List<LanguageCombination>>` for reactive UI

2. **Implement `SqlDelightDeckRepository`** (commonMain)
   - Uses generated `Database` and queries
   - Maps `CardLineEntity` + `CardLineAnswer` (or JSON) → `Card`, `CardLine`
   - Maps `DeckEntity` + cards → `Deck`
   - Maps deck + combo → `LanguageCombination`

3. **Migrate `StatsRepository` to SQLite**
   - Replace DataStore with `card_stats` table
   - `AndroidStatsStore` / `IosStatsStore` → `SqlDelightStatsRepository` (or merge into single DB)

### Phase 3: Seed Data

1. **Initial data migration**
   - On first launch (or DB empty): insert all data from `SampleData` into SQLite
   - Option A: Keep `SampleData` as source, run migration once
   - Option B: Convert `SampleData` to SQL (e.g. script or resource file) and run on init

2. **Migration logic**
   - Check if DB has rows in `language_combo`; if empty, run seed
   - Use transactions for atomic insert

### Phase 4: Wire Up UI

1. **Replace `SampleData` usage**
   - `App.kt`: inject `DeckRepository` instead of using `SampleData.languageCombinations`
   - `StartScreen`: receive `languageCombinations: Flow<List<LanguageCombination>>` or `suspend` load

2. **Async loading**
   - Use `CoroutineScope` / `rememberCoroutineScope` (already in use)
   - Load combinations on screen appear; show loading state if needed

3. **Remove `SampleData`**
   - Delete `SampleData.kt` after migration verified
   - Remove `decks` property if used elsewhere

### Phase 5: Remove DataStore for Stats

1. **Unify Stats**
   - Migrate existing DataStore stats to SQLite on first run (optional; or start fresh)
   - Remove `AndroidStatsStore` / `IosStatsStore` DataStore usage
   - Use single `SqlDelightStatsRepository` backed by `card_stats` table

2. **Cleanup**
   - Remove DataStore dependency from `stats` path if no longer needed
   - Keep DataStore for app settings if used elsewhere (e.g. theme, default direction)

---

## 5. File Structure (After Migration)

```
shared/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── model/           # Card, Deck, CardLine, etc. (unchanged)
│   │   │   ├── data/
│   │   │   │   ├── DeckRepository.kt      # interface
│   │   │   │   ├── SqlDelightDeckRepository.kt
│   │   │   │   └── DatabaseDriver.kt       # expect/actual
│   │   │   ├── repository/
│   │   │   │   ├── StatsRepository.kt     # interface (unchanged)
│   │   │   │   └── SqlDelightStatsRepository.kt
│   │   │   └── ...
│   │   └── sqldelight/
│   │       └── net/thetrues/languagecards/db/
│   │           ├── LanguageCombo.sq
│   │           ├── Deck.sq
│   │           ├── Card.sq
│   │           ├── DeckCard.sq
│   │           ├── CardLine.sq
│   │           ├── CardStats.sq
│   │           └── *.sq (queries)
│   ├── androidMain/kotlin/
│   │   └── platform/
│   │       └── DatabaseDriver.android.kt   # AndroidSqliteDriver
│   └── iosMain/kotlin/
│       └── platform/
│           └── DatabaseDriver.ios.kt      # NativeSqliteDriver
```

---

## 6. Dependencies

```toml
# gradle/libs.versions.toml
sqldelight = "2.0.2"  # or latest
```

```kotlin
// shared/build.gradle.kts
plugins {
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("LanguageCardsDatabase") {
            packageName.set("net.thetrues.languagecards.db")
        }
    }
}

// commonMain
implementation(libs.sqldelight.runtime)
// androidMain
implementation(libs.sqldelight.android.driver)
// iosMain
implementation(libs.sqldelight.native.driver)
```

---

## 7. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| DataStore → SQLite stats migration | Option: one-time migration on first run; or start fresh (acceptable for prototype) |
| JSON for `sideB` | Use `kotlinx.serialization` or simple `joinToString`/`split`; avoid complex JSON if not needed |
| Schema changes later | SQLDelight supports migrations; add migration files when schema evolves |
| iOS path for DB | Use `NSDocumentDirectory` or `NSApplicationSupportDirectory`; ensure single instance per process |

---

## 8. Checklist

- [ ] Add SQLDelight plugin and dependencies
- [ ] Create schema (.sq files)
- [ ] Implement platform drivers (expect/actual)
- [ ] Implement `DeckRepository` + `SqlDelightDeckRepository`
- [ ] Migrate `StatsRepository` to SQLite
- [ ] Seed data from SampleData on first run
- [ ] Wire `App` / `StartScreen` to use `DeckRepository`
- [ ] Remove `SampleData` and DataStore usage for stats
- [ ] Test on Android and iOS

---

## 9. Optional: Per-Direction Stats (Future)

The SPEC recommends per-card, per-direction stats. Current `CardStats` is per-card only. To add direction later:

```sql
card_stats
  card_id TEXT NOT NULL
  direction TEXT NOT NULL  -- 'A_TO_B' | 'B_TO_A'
  hits INTEGER NOT NULL DEFAULT 0
  misses INTEGER NOT NULL DEFAULT 0
  PRIMARY KEY (card_id, direction)
```

---

*Document version: 1.0 — SQLite Migration Plan*
