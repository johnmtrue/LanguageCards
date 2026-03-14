package net.thetrues.languagecards.repository

import net.thetrues.languagecards.db.LanguageCardsDatabase
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.platform.IgnoreIosSimulator
import net.thetrues.languagecards.platform.createTestDriver
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests recording and clearing stats via [SqlDelightStatsRepository].
 * Uses in-memory SQLite; skipped on iOS simulator (same as [DeckRepositoryTest]).
 */
@IgnoreIosSimulator
class StatsRepositoryTest {

    private lateinit var database: LanguageCardsDatabase
    private lateinit var repository: SqlDelightStatsRepository

    @BeforeTest
    fun setUp() {
        val driver = createTestDriver()
        database = LanguageCardsDatabase(driver)
        repository = SqlDelightStatsRepository(database)
    }

    @Test
    fun recordHit_incrementsHits() {
        repository.record("card-1", PracticeDirection.A_TO_B, wasHit = true)

        val stats = repository.getStats("card-1", PracticeDirection.A_TO_B)
        assertEquals("card-1", stats?.cardId)
        assertEquals(PracticeDirection.A_TO_B, stats?.direction)
        assertEquals(1, stats?.hits)
        assertEquals(0, stats?.misses)
    }

    @Test
    fun recordMiss_incrementsMisses() {
        repository.record("card-1", PracticeDirection.A_TO_B, wasHit = false)

        val stats = repository.getStats("card-1", PracticeDirection.A_TO_B)
        assertEquals(0, stats?.hits)
        assertEquals(1, stats?.misses)
    }

    @Test
    fun recordMultiple_accumulatesHitsAndMisses() {
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = false)

        val stats = repository.getStats("c1", PracticeDirection.A_TO_B)
        assertEquals(2, stats?.hits)
        assertEquals(1, stats?.misses)
    }

    @Test
    fun recordDifferentDirections_storedSeparately() {
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c1", PracticeDirection.B_TO_A, wasHit = false)

        val aToB = repository.getStats("c1", PracticeDirection.A_TO_B)
        assertEquals(2, aToB?.hits)
        assertEquals(0, aToB?.misses)

        val bToA = repository.getStats("c1", PracticeDirection.B_TO_A)
        assertEquals(0, bToA?.hits)
        assertEquals(1, bToA?.misses)
    }

    @Test
    fun getAllStats_returnsAllRows() {
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c2", PracticeDirection.A_TO_B, wasHit = false)
        repository.record("c1", PracticeDirection.B_TO_A, wasHit = true)

        val all = repository.getAllStats()
        assertEquals(3, all.size)
        assertEquals(
            setOf(
                Triple("c1", PracticeDirection.A_TO_B, 1 to 0),
                Triple("c2", PracticeDirection.A_TO_B, 0 to 1),
                Triple("c1", PracticeDirection.B_TO_A, 1 to 0),
            ),
            all.map { Triple(it.cardId, it.direction, it.hits to it.misses) }.toSet(),
        )
    }

    @Test
    fun clearAllStats_removesAllRows() {
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c2", PracticeDirection.B_TO_A, wasHit = false)

        repository.clearAllStats()

        assertEquals(0, repository.getAllStats().size)
        assertNull(repository.getStats("c1", PracticeDirection.A_TO_B))
        assertNull(repository.getStats("c2", PracticeDirection.B_TO_A))
    }

    @Test
    fun getStats_nonexistentCard_returnsNull() {
        assertNull(repository.getStats("nonexistent", PracticeDirection.A_TO_B))
    }

    @Test
    fun getStatsForCards_returnsOnlyRequestedCardsAndDirection() {
        repository.record("c1", PracticeDirection.A_TO_B, wasHit = true)
        repository.record("c2", PracticeDirection.A_TO_B, wasHit = false)
        repository.record("c3", PracticeDirection.B_TO_A, wasHit = true)

        val map = repository.getStatsForCards(listOf("c1", "c2", "c99"), PracticeDirection.A_TO_B)

        assertEquals(2, map.size)
        assertEquals(1, map["c1"]?.hits)
        assertEquals(0, map["c1"]?.misses)
        assertEquals(0, map["c2"]?.hits)
        assertEquals(1, map["c2"]?.misses)
        assertNull(map["c99"])
    }
}
