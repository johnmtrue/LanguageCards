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

### Phase 1: Add SQLDelight and Schema ✅ Implemented

1. **Add SQLDelight plugin and dependencies** ✅
   - `app.cash.sqldelight` plugin (version ~2.0.x)
   - `sqlite-driver` (commonMain)
   - `android-driver` (androidMain)
   - `native-driver` (iosMain)

2. **Create schema and queries** ✅
   - `.sq` files in `shared/src/commonMain/sqldelight/net/thetrues/languagecards/db/`
   - `LanguageCombo.sq`, `Deck.sq`, `Card.sq`, `DeckCard.sq`, `CardLine.sq`, `CardStats.sq`

3. **Platform drivers** ✅
   - Android: `createSqlDriver(Context)` → `AndroidSqliteDriver`
   - iOS: `createSqlDriver(path?)` → `NativeSqliteDriver` (default: Documents path)

4. **Database initialization** ✅
   - `createSqlDriver(platformContext)` expect/actual
   - `createDatabase(platformContext)` in `data/Database.kt`

### Phase 2: Repository Layer ✅ Implemented

1. **Define `DeckRepository` interface** (commonMain) ✅
   - `getLanguageCombinations(): List<LanguageCombination>`
   - `getDeck(id: String): Deck?`

2. **Implement `SqlDelightDeckRepository`** (commonMain) ✅
   - Uses generated `LanguageCardsDatabase` and queries
   - Maps `Card_line` (side_b as delimiter-separated) → `Card`, `CardLine`
   - Maps `Deck` + cards → `Deck`
   - Maps deck + combo → `LanguageCombination`

3. **Migrate `StatsRepository` to SQLite** ✅
   - `SqlDelightStatsRepository` backed by `card_stats` table
   - `insertIfNotExists`, `recordHit`, `recordMiss`, `deleteAll` queries
   - App still uses DataStore implementations until Phase 4/5

### Phase 3: Seed Data ✅ Implemented

1. **Initial data migration** ✅
   - On first launch (or DB empty): insert all data from `SampleData` into SQLite
   - `SampleData` kept as source; `DatabaseSeeder.kt` runs migration once

2. **Migration logic** ✅
   - `selectCount` query on `language_combo`; if 0, run seed
   - `seedDatabaseIfEmpty(database)` called from `createDatabase()`
   - Transaction wraps all inserts for atomicity

### Phase 4: Wire Up UI ✅ Implemented

1. **Replace `SampleData` usage** ✅
   - `App.kt`: injects `DeckRepository`; loads combinations via `LaunchedEffect` + `withContext(Dispatchers.Default)`
   - `StartScreen`: receives `languageCombinations: List<LanguageCombination>` (loaded async)

2. **Async loading** ✅
   - `LaunchedEffect(deckRepository)` loads combinations on first composition
   - Loading state: `CircularProgressIndicator` while `languageCombinations == null`

3. **Platform wiring** ✅
   - `MainActivity`: creates DB via `createDatabase(applicationContext)`, passes `SqlDelightDeckRepository`
   - `MainViewController`: creates DB via `createDatabase(null)`, passes `SqlDelightDeckRepository`

4. **`SampleData` retained** for `DatabaseSeeder`; UI no longer uses it directly

### Phase 5: Remove DataStore for Stats ✅ Implemented

1. **Unify Stats** ✅
   - Stats now use `SqlDelightStatsRepository` backed by `card_stats` table
   - `MainActivity` and `MainViewController` pass `SqlDelightStatsRepository(database)`
   - No migration of existing DataStore stats (start fresh)

2. **Cleanup** ✅
   - Deleted `AndroidStatsStore.kt` and `IosStatsStore.kt`
   - Removed DataStore dependencies from `shared` (androidMain, iosMain) and `androidApp`

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

- [x] Add SQLDelight plugin and dependencies
- [x] Create schema (.sq files)
- [x] Implement platform drivers (expect/actual)
- [x] Implement `DeckRepository` + `SqlDelightDeckRepository`
- [x] Implement `SqlDelightStatsRepository`
- [x] Seed data from SampleData on first run
- [x] Wire `App` / `StartScreen` to use `DeckRepository`
- [x] Remove DataStore usage for stats (SqlDelightStatsRepository)
- [ ] Test on Android and iOS

---

## 9. Optional: Per-Direction Stats (Future)

The SPEC recommends per-card, per-direction stats. Current `CardStats` is per-card only.

**See [PER_CARD_PER_DIRECTION_STATS_PLAN.md](PER_CARD_PER_DIRECTION_STATS_PLAN.md)** for a full implementation plan.

---

*Document version: 1.0 — SQLite Migration Plan*
