package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.BridgeStage
import org.junit.Assert.*
import org.junit.Test

class StageProgressionEngineTest {

    @Test
    fun `holds stage below session threshold`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.PASSIVE_INPUT,
            qualifyingSessions = mapOf(BridgeStage.PASSIVE_INPUT to 2),
            manuallyPinned = false
        )
        assertNull(decision)
    }

    @Test
    fun `advances at session threshold`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.PASSIVE_INPUT,
            qualifyingSessions = mapOf(BridgeStage.PASSIVE_INPUT to 3),
            manuallyPinned = false
        )
        assertEquals(BridgeStage.FRAGMENT_BUILDING, decision)
    }

    @Test
    fun `counts are tracked per stage`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.FRAGMENT_BUILDING,
            qualifyingSessions = mapOf(
                BridgeStage.PASSIVE_INPUT to 10,
                BridgeStage.FRAGMENT_BUILDING to 1
            ),
            manuallyPinned = false
        )
        assertNull(decision)
    }

    @Test
    fun `manual pin blocks advancement`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.PASSIVE_INPUT,
            qualifyingSessions = mapOf(BridgeStage.PASSIVE_INPUT to 99),
            manuallyPinned = true
        )
        assertNull(decision)
    }

    @Test
    fun `free flow has no next stage`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.FREE_FLOW,
            qualifyingSessions = mapOf(BridgeStage.FREE_FLOW to 99),
            manuallyPinned = false
        )
        assertNull(decision)
    }

    @Test
    fun `scaffolded advances to free flow`() {
        val decision = StageProgressionEngine.decide(
            currentStage = BridgeStage.SCAFFOLDED,
            qualifyingSessions = mapOf(BridgeStage.SCAFFOLDED to 3),
            manuallyPinned = false
        )
        assertEquals(BridgeStage.FREE_FLOW, decision)
    }
}
