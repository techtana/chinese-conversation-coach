package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.ScenarioEvent
import org.junit.Assert.*
import org.junit.Test

class ScenarioRepositoryTest {

    @Test
    fun `scenario ids are unique`() {
        val ids = ScenarioRepository.scenarios.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every scenario has goals items and a stamp`() {
        ScenarioRepository.scenarios.forEach { scenario ->
            assertTrue("${scenario.id} has no goals", scenario.goals.isNotEmpty())
            assertTrue("${scenario.id} has no items", scenario.items.isNotEmpty())
            assertTrue("${scenario.id} has no stamp title", scenario.stampTitle.isNotBlank())
        }
    }

    @Test
    fun `item ids are unique within a scenario`() {
        ScenarioRepository.scenarios.forEach { scenario ->
            val ids = scenario.items.map { it.id }
            assertEquals("${scenario.id} duplicate items", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `byId finds scenarios and returns null for unknown`() {
        assertNotNull(ScenarioRepository.byId("hotel_checkin"))
        assertNull(ScenarioRepository.byId("does_not_exist"))
    }

    // ── validateEvent ────────────────────────────────────────────────────────

    private val hotel = ScenarioRepository.byId("hotel_checkin")!!

    @Test
    fun `valid item award passes through`() {
        val event = hotel.validateEvent(ScenarioEvent(itemEarned = "room_key"), emptySet())
        assertEquals("room_key", event.itemEarned)
    }

    @Test
    fun `hallucinated item id is dropped`() {
        val event = hotel.validateEvent(ScenarioEvent(itemEarned = "golden_ticket"), emptySet())
        assertNull(event.itemEarned)
    }

    @Test
    fun `already earned item is not awarded twice`() {
        val event = hotel.validateEvent(ScenarioEvent(itemEarned = "room_key"), setOf("room_key"))
        assertNull(event.itemEarned)
    }

    @Test
    fun `completed is ignored until all items earned`() {
        val premature = hotel.validateEvent(ScenarioEvent(completed = true), setOf("room_key"))
        assertFalse(premature.completed)

        val legit = hotel.validateEvent(
            ScenarioEvent(completed = true),
            setOf("room_key", "wifi_password")
        )
        assertTrue(legit.completed)
    }

    @Test
    fun `completion counts an item awarded in the same message`() {
        val event = hotel.validateEvent(
            ScenarioEvent(itemEarned = "wifi_password", completed = true),
            setOf("room_key")
        )
        assertEquals("wifi_password", event.itemEarned)
        assertTrue(event.completed)
    }

    @Test
    fun `unknown mood is dropped`() {
        val event = hotel.validateEvent(ScenarioEvent(mood = "ecstatic"), emptySet())
        assertNull(event.mood)
        val ok = hotel.validateEvent(ScenarioEvent(mood = "confused"), emptySet())
        assertEquals("confused", ok.mood)
    }
}
