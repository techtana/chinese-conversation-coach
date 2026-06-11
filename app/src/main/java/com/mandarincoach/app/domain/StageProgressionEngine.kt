package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.BridgeStage

/**
 * Decides when a learner is ready to move up the beginner bridge.
 * A session "qualifies" once it reaches [QUALIFYING_TURNS] user turns
 * (or a scenario is completed); after [SESSIONS_TO_ADVANCE] qualifying
 * sessions at a stage the learner is offered the next stage — unless
 * they pinned a stage manually in settings.
 */
object StageProgressionEngine {

    const val QUALIFYING_TURNS = 6
    const val SESSIONS_TO_ADVANCE = 3

    fun decide(
        currentStage: BridgeStage,
        qualifyingSessions: Map<BridgeStage, Int>,
        manuallyPinned: Boolean
    ): BridgeStage? {
        if (manuallyPinned) return null
        val next = currentStage.next() ?: return null
        val count = qualifyingSessions[currentStage] ?: 0
        return if (count >= SESSIONS_TO_ADVANCE) next else null
    }
}
