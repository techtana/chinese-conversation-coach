package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.model.Scenario
import kotlin.random.Random

/**
 * The "Spotify model": 80% exploitation (the next sensible mission for
 * this learner), 20% exploration (a random one). Seed the random with
 * the epoch day so the suggestion is stable within a day.
 */
object ContentModeSelector {

    const val EXPLORATION_RATE = 0.2

    fun suggestScenario(
        scenarios: List<Scenario>,
        completedIds: Set<String>,
        userLevel: ProficiencyLevel,
        random: Random
    ): String? {
        if (scenarios.isEmpty()) return null

        if (random.nextDouble() < EXPLORATION_RATE) {
            return scenarios[random.nextInt(scenarios.size)].id
        }

        val uncompleted = scenarios.filter { it.id !in completedIds }
        val atLevel = uncompleted.filter { it.minLevel.ordinal <= userLevel.ordinal }
        return (atLevel.firstOrNull() ?: uncompleted.firstOrNull() ?: scenarios.first()).id
    }
}
