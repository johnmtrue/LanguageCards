# Plan: Per-Card, Per-Direction Stats

## 1. Current State

| Component | Current Behavior |
|-----------|------------------|
| **CardStats model** | `cardId`, `hits`, `misses` — no direction |
| **card_stats table** | `card_id` PRIMARY KEY — one row per card |
| **StatsRepository.record()** | `record(cardId, wasHit)` — direction not passed |
| **SessionState** | Has `direction: PracticeDirection` (A_TO_B, B_TO_A) |
| **CardScreen → onAnswer** | Passes `(cardId, wasHit)` only — direction available in `state.direction` |
| **StatsScreen** | Shows aggregate totals; "Cards practiced" = unique card count |

**Gap:** Stats are aggregated across both directions. Practicing "hello → hola" and "hola → hello" share the same row. The SPEC requires per (card, direction) so each direction can be prioritized independently.

---

## 2. Target State

| Component | Target Behavior |
|-----------|-----------------|
| **CardStats model** | `cardId`, `direction: PracticeDirection`, `hits`, `misses` |
| **card_stats table** | `(card_id, direction)` composite PRIMARY KEY |
| **StatsRepository.record()** | `record(cardId, direction, wasHit)` |
| **StatsScreen** | Show per-direction breakdown; "Card+direction practiced" count |
| **Future: prioritization** | Sort by weak card+direction for weak-first sessions |

---

## 3. Schema Change

### 3.1 New Table Definition

```sql
CREATE TABLE card_stats (
  card_id TEXT NOT NULL REFERENCES card(id),
  direction TEXT NOT NULL,  -- 'A_TO_B' | 'B_TO_A'
  hits INTEGER NOT NULL DEFAULT 0,
  misses INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (card_id, direction)
);
```

### 3.2 SQLDelight Migration

SQLDelight supports schema migrations. Add a migration file:

- **Location:** `shared/src/commonMain/sqldelight/net/thetrues/languagecards/db/migrations/`
- **Naming:** `1.sqm` (or next version number if schema versioning exists)

**Migration steps:**
1. Create new table `card_stats_new` with `(card_id, direction, hits, misses)` and `PRIMARY KEY (card_id, direction)`.
2. Copy data: `INSERT INTO card_stats_new (card_id, direction, hits, misses) SELECT card_id, 'A_TO_B', hits, misses FROM card_stats`.
3. Drop `card_stats`.
4. Rename `card_stats_new` to `card_stats`.

**Alternative (simpler for prototype):** Drop and recreate. Existing stats are lost. Acceptable if users can tolerate a one-time reset.

---

## 4. Implementation Phases

### Phase 1: Schema and Migration

1. **Add migration** (or drop/recreate if acceptable):
   - Update `CardStats.sq` with new schema.
   - Add `1.sqm` migration if preserving existing data; otherwise update schema in place and bump DB version.

2. **Update SQLDelight queries** in `CardStats.sq`:
   - `selectByCardId` → `selectByCardIdAndDirection(cardId, direction)` and optionally `selectByCardId(cardId)` returning all directions.
   - `selectAll` → returns all rows (each row = card+direction).
   - `recordHit` / `recordMiss` → take `(cardId, direction)`.
   - `insertIfNotExists` → take `(cardId, direction)`.
   - `deleteAll` → unchanged.

### Phase 2: Model and Repository

1. **Update `CardStats` model** (`model/CardStats.kt`):
   ```kotlin
   data class CardStats(
       val cardId: String,
       val direction: PracticeDirection,
       var hits: Int = 0,
       var misses: Int = 0,
   )
   ```

2. **Update `StatsRepository` interface** (`repository/StatsRepository.kt`):
   - `record(cardId: String, direction: PracticeDirection, wasHit: Boolean)`
   - `getStats(cardId: String, direction: PracticeDirection): CardStats?`
   - `getStatsForCard(cardId: String): List<CardStats>` (optional; for aggregating per card)
   - `getAllStats(): List<CardStats>` — returns all card+direction rows
   - `clearAllStats()` — unchanged

3. **Update `SqlDelightStatsRepository`**:
   - Implement new signatures; pass `direction.name` to SQL (e.g. `"A_TO_B"`, `"B_TO_A"`).
   - Map `direction` string back to `PracticeDirection` when reading.

### Phase 3: Call Sites

1. **App.kt** — `CardScreen` callback:
   - Change `onAnswer: (cardId: String, wasHit: Boolean) -> Unit` to include direction.
   - Pass `state.direction` when calling `statsStore.record(cardId, state.direction, wasHit)`.

2. **CardScreen.kt**:
   - Update `onAnswer` signature to `(cardId: String, direction: PracticeDirection, wasHit: Boolean) -> Unit` **or** keep `(cardId, wasHit)` and have `App.kt` inject direction from `sessionState.direction` (simpler — direction is in state, not in CardScreen’s callback params).
   - **Recommended:** Keep `onAnswer(cardId, wasHit)`; `App.kt` has `sessionState` and passes `statsStore.record(cardId, sessionState.direction, wasHit)`. No CardScreen signature change.

### Phase 4: StatsScreen UI

1. **StatsScreen** receives `List<CardStats>` where each item is a card+direction.
2. **Display options:**
   - **Option A:** Aggregate totals (current behavior) — sum hits/misses across all rows. "Cards practiced" = number of unique (cardId, direction) pairs.
   - **Option B:** Per-direction breakdown — e.g. "A→B: X hits, Y misses" and "B→A: X hits, Y misses".
   - **Option C:** Per-card, per-direction list — table/list of each card+direction with its stats.

   **Recommendation:** Start with Option A (aggregate) for minimal UI change; add Option B or C later for richer feedback.

### Phase 5: Future — Weak-First Prioritization

Once per-direction stats exist, `SessionFlow.startSession()` (or a new `getPrioritizedCards()`) can:
- Query stats for the deck’s cards in the chosen direction.
- Sort by `(hits + misses)` ascending (least practiced) or by success rate (lowest first).
- Use that order instead of `shuffled().take(10)`.

---

## 5. File Change Summary

| File | Changes |
|------|---------|
| `CardStats.sq` | New schema; queries take `direction` |
| `migrations/1.sqm` (optional) | Migrate old → new schema |
| `model/CardStats.kt` | Add `direction: PracticeDirection` |
| `repository/StatsRepository.kt` | `record(cardId, direction, wasHit)`; `getStats(cardId, direction)` |
| `repository/SqlDelightStatsRepository.kt` | Implement new interface; map direction to/from string |
| `ui/App.kt` | `statsStore.record(cardId, sessionState.direction, wasHit)` |
| `ui/screens/StatsScreen.kt` | Optional: per-direction breakdown; "Cards practiced" = `stats.size` |

---

## 6. Direction Storage

Store direction as `TEXT` in SQLite: `"A_TO_B"` or `"B_TO_A"` (matching `PracticeDirection.name`). Use `PracticeDirection.valueOf(string)` when reading; use `direction.name` when writing.

---

## 7. Data Migration Strategy

| Strategy | Pros | Cons |
|----------|------|------|
| **Drop and recreate** | Simple; no migration file | Existing stats lost |
| **Migrate with default direction** | Preserves totals | All existing stats attributed to A_TO_B; B_TO_A starts at 0 |

**Recommendation:** For prototype/MVP, drop and recreate. Document in release notes. For production, migrate and assign existing stats to `A_TO_B`.

---

## 8. Checklist

- [x] Add migration (or update schema + document reset)
- [x] Update `CardStats.sq` schema and queries
- [x] Update `CardStats` model with `direction`
- [x] Update `StatsRepository` interface
- [x] Update `SqlDelightStatsRepository` implementation
- [x] Update `App.kt` to pass `sessionState.direction` to `record()`
- [x] Update `StatsScreen` with per-direction breakdown and language-aware labels
- [x] Add unit tests for stats recording/clearing (shared + Android)
- [ ] Test on Android and iOS
- [x] Implement weak-first prioritization using per-direction stats (CardSelector: weight = 1 + misses + underPracticedBonus)

---

*Document version: 1.0 — Per-Card, Per-Direction Stats Plan*
