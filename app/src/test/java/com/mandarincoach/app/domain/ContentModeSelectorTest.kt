package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.repository.ScenarioRepository
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ContentModeSelectorTest {

    private val scenarios = ScenarioRepository.scenarios

    /** Deterministic random: fixed exploration roll, fixed pick index. */
    private fun fixedRandom(roll: Double, pick: Int = 0) = object : Random() {
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextDouble(): Double = roll
        override fun nextInt(until: Int): Int = pick % until
    }

    @Test
    fun `empty scenario list returns null`() {
        assertNull(
            ContentModeSelector.suggestScenario(emptyList(), emptySet(), ProficiencyLevel.BEGINNER, fixedRandom(0.9))
        )
    }

    @Test
    fun `exploitation picks the first uncompleted mission at the user's level`() {
        val suggested = ContentModeSelector.suggestScenario(
            scenarios,
            completedIds = setOf("hotel_checkin"),
            userLevel = ProficiencyLevel.BEGINNER,
            random = fixedRandom(roll = 0.9)
        )
        assertEquals("ordering_food", suggested)
    }

    @Test
    fun `exploration picks randomly regardless of completion`() {
        val suggested = ContentModeSelector.suggestScenario(
            scenarios,
            completedIds = setOf(scenarios[2].id),
            userLevel = ProficiencyLevel.BEGINNER,
            random = fixedRandom(roll = 0.1, pick = 2)
        )
        assertEquals(scenarios[2].id, suggested)
    }

    @Test
    fun `exploitation falls back to any uncompleted mission when none match the level`() {
        val beginnerIds = scenarios.filter { it.minLevel == ProficiencyLevel.BEGINNER }.map { it.id }
        val suggested = ContentModeSelector.suggestScenario(
            scenarios,
            completedIds = beginnerIds.toSet(),
            userLevel = ProficiencyLevel.BEGINNER,
            random = fixedRandom(roll = 0.9)
        )
        assertTrue(suggested in scenarios.map { it.id })
        assertFalse(suggested in beginnerIds)
    }

    @Test
    fun `everything completed still suggests something`() {
        val suggested = ContentModeSelector.suggestScenario(
            scenarios,
            completedIds = scenarios.map { it.id }.toSet(),
            userLevel = ProficiencyLevel.FLUENT,
            random = fixedRandom(roll = 0.9)
        )
        assertNotNull(suggested)
    }

    @Test
    fun `roughly 20 percent of suggestions explore`() {
        val random = Random(42)
        var explorations = 0
        repeat(1000) {
            val suggested = ContentModeSelector.suggestScenario(
                scenarios,
                completedIds = emptySet(),
                userLevel = ProficiencyLevel.BEGINNER,
                random = random
            )
            // Exploitation always picks hotel_checkin here; a different pick
            // means the exploration branch fired (4/5 of exploration picks)
            if (suggested != "hotel_checkin") explorations++
        }
        // 20% exploration * 4/5 distinguishable ≈ 160 of 1000
        assertTrue("explorations=$explorations", explorations in 80..260)
    }
}
