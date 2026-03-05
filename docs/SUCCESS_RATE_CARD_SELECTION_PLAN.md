# Plan: Success-Rate-Based Card Selection

## 1. Goal

Select session cards using a hybrid approach:
- **Half**: Randomly selected, weighted toward cards most missed (low success rate)
- **Half**: Randomly selected from the cards not chosen in the first half

This balances practice on weak cards with variety across the deck.

---

## 2. Current State

| Component | Current Behavior |
|-----------|------------------|
| **SessionFlow.startSession()** | `deck.cards.shuffled().take(10)` — purely random |
| **Session size** | Fixed at 10 cards |
| **StatsRepository** | Per-card, per-direction stats; `getStats(cardId, direction)`, `getAllStats()` |
| **Call site** | `App.kt` → `SessionFlow.startSession(deck, direction)` |

---

## 3. Target Behavior

### 3.1 Selection Algorithm

Given:
- `deck: Deck` (list of cards)
- `direction: PracticeDirection`
- `sessionSize: Int` (e.g. 10)
- `statsRepository: StatsRepository`

**Step 1 — Weighted half (weak cards):**
- Compute a "weakness" weight for each card in the deck for the given direction.
- Select `sessionSize / 2` cards using weighted random sampling (without replacement).
- Cards with more misses (or lower success rate) have higher probability of being selected.

**Step 2 — Random half (remainder):**
- From the cards **not** selected in Step 1, randomly select `sessionSize - (sessionSize / 2)` cards.
- If the deck has fewer cards than `sessionSize`, use all cards (no change from current behavior).

**Step 3 — Combine and shuffle:**
- Combine the two halves and shuffle the final list so weak cards are not always first.

### 3.2 Weight Function

For "most missed" weighting, use a score that favors cards with more misses:

| Approach | Formula | Notes |
|----------|---------|-------|
| **Miss count + 1** | `weight = misses + 1` | Simple; unplayed cards (0 misses) get weight 1 |
| **Weakness score** | `weight = misses - hits + K` (e.g. K=5) | Negative values need clamping to min 1 |
| **Inverse success rate** | `weight = 1 / (hits/(hits+misses) + ε)` | More complex; handles edge cases |

**Recommendation:** Use `weight = misses + 1`. Cards never practiced get weight 1 (low but non-zero). High-miss cards get proportionally higher weight.

### 3.3 Edge Cases

| Case | Behavior |
|------|----------|
| Deck size &lt; session size | Use all cards; no splitting (or skip weighted logic) |
| All cards unplayed (no stats) | All weights equal → effectively random for both halves |
| Deck size &lt; sessionSize/2 | First half = all cards; second half = empty; pad with repeats? No — cap first half at available cards, second half = 0 |

**Simpler rule:** If `deck.cards.size <= sessionSize`, select `min(sessionSize, deck.cards.size)` cards. Only apply the two-phase selection when `deck.cards.size > sessionSize`.

---

## 4. Data Requirements

### 4.1 Stats Lookup

We need stats for each card in the deck for the given direction. Options:

| Option | Pros | Cons |
|--------|------|------|
| **Loop getStats()** | No schema change | N queries for N cards |
| **getStatsForCards(cardIds, direction)** | Single query | New repository method + SQL |

**Recommendation:** Add `getStatsForCards(cardIds: List<String>, direction: PracticeDirection): Map<String, CardStats>` to `StatsRepository`. Implement via a single query (e.g. `SELECT * FROM card_stats WHERE card_id IN (?) AND direction = ?`) or by filtering `getAllStats()`. For small decks (&lt;100 cards), filtering `getAllStats()` in memory is acceptable.

---

## 5. Implementation Phases

### Phase 1: Card Selector (Pure Logic)

1. **Create `CardSelector`** (or `SessionCardSelector`) in `session/` package:
   - `fun selectCards(deck: Deck, direction: PracticeDirection, sessionSize: Int, statsByCard: Map<String, CardStats>): List<Card>`
   - Pure function: given deck, direction, size, and a map of cardId → stats for that direction, returns the selected cards.
   - Encapsulates: weight computation, weighted random sampling, random sampling from remainder, combine + shuffle.

2. **Weighted random sampling:**
   - Use `kotlin.random.Random` (or inject for testability).
   - Algorithm: assign each card a weight; use cumulative-weight sampling (or `random.nextDouble() * totalWeight`) to pick without replacement.

### Phase 2: Repository Extension

1. **Add `getStatsForCards` to `StatsRepository`:**
   - `fun getStatsForCards(cardIds: List<String>, direction: PracticeDirection): Map<String, CardStats>`
   - Returns map of cardId → stats; cards with no stats are omitted (treated as 0 hits, 0 misses).

2. **Implement in `SqlDelightStatsRepository`:**
   - Option A: Add query `selectByCardIdsAndDirection(cardIds: List<String>, direction: String)` if SQLDelight supports `IN` with a list.
   - Option B: Filter `getAllStats()` by `cardIds` and `direction` in Kotlin. Simpler for small decks.

### Phase 3: Wire Up SessionFlow

1. **Change `SessionFlow.startSession` signature:**
   - Option A: `startSession(deck, direction, statsRepository)` — SessionFlow fetches stats and delegates to CardSelector.
   - Option B: `startSession(deck, direction, statsByCard: Map<String, CardStats>)` — caller fetches stats; SessionFlow stays pure.
   - **Recommendation:** Option A — keep stats fetching inside SessionFlow; App.kt already has `statsStore`, so pass it in.

2. **Update `App.kt`:**
   - `SessionFlow.startSession(deck, direction, statsStore)` (or keep current call if we add an overload that takes statsStore and fetches internally).

### Phase 4: Session Size Constant

- Extract `sessionSize = 10` to a constant (e.g. `SessionFlow.DEFAULT_SESSION_SIZE` or `companion object`) for clarity and future configurability.

---

## 6. Weighted Random Sampling (Detail)

### Algorithm: Weighted Random Without Replacement

```
Given: cards = [C1, C2, ...], weights = [w1, w2, ...], count = N
Output: N distinct cards

1. Build list of (card, weight) pairs.
2. For i = 1 to N:
   a. totalWeight = sum of remaining weights
   b. r = random(0, totalWeight)
   c. Walk cumulative weights; pick card where cumulative >= r
   d. Remove picked card from list; repeat
3. Return selected cards
```

**Kotlin sketch:**
```kotlin
fun weightedSample(cards: List<Card>, weights: List<Int>, count: Int): List<Card> {
    val pairs = cards.zip(weights).toMutableList()
    val result = mutableListOf<Card>()
    repeat(minOf(count, pairs.size)) {
        val total = pairs.sumOf { it.second }
        var r = Random.nextInt(total)
        for (i in pairs.indices) {
            r -= pairs[i].second
            if (r < 0) {
                result.add(pairs[i].first)
                pairs.removeAt(i)
                break
            }
        }
    }
    return result
}
```

---

## 7. File Change Summary

| File | Changes |
|------|---------|
| `session/CardSelector.kt` (new) | `selectCards(deck, direction, sessionSize, statsByCard)` + weighted sampling logic |
| `repository/StatsRepository.kt` | Add `getStatsForCards(cardIds, direction): Map<String, CardStats>` |
| `repository/SqlDelightStatsRepository.kt` | Implement `getStatsForCards` (filter `getAllStats()` or add query) |
| `session/SessionFlow.kt` | `startSession(deck, direction, statsRepository)`; fetch stats, call CardSelector, build SessionState |
| `ui/App.kt` | Pass `statsStore` to `SessionFlow.startSession(deck, direction, statsStore)` |

---

## 8. Testing Considerations

- **Unit test CardSelector** with fixed `Random` seed: given known weights, verify distribution.
- **Edge cases:** empty deck, deck size 1, all weights equal, all weights zero.
- **Integration:** SessionFlow with mock StatsRepository returns expected card list.

---

## 9. Checklist

- [x] Create `CardSelector` with `selectCards()` and weighted sampling
- [x] Add `getStatsForCards()` to StatsRepository and SqlDelightStatsRepository
- [x] Update `SessionFlow.startSession()` to use CardSelector and StatsRepository
- [x] Update `App.kt` to pass statsStore to startSession
- [x] Extract session size constant
- [x] Add unit tests for CardSelector
- [ ] Manual test on Android/iOS

---

*Document version: 1.0 — Success-Rate-Based Card Selection Plan*
