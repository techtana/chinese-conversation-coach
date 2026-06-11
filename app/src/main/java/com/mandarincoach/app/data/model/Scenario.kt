package com.mandarincoach.app.data.model

data class ScenarioItem(
    val id: String,
    val name: String,
    val hanzi: String,
    val emoji: String
)

/**
 * A "Real-World Impact" mission: the AI plays [aiRole] in character and
 * reacts in-story to the learner's language instead of grading it.
 * Earning every [items] entry completes the scenario and stamps the
 * passport with [stampTitle].
 */
data class Scenario(
    val id: String,
    val title: String,
    val hanziTitle: String,
    val emoji: String,
    val description: String,
    val aiRole: String,
    val userRole: String,
    val setting: String,
    val goals: List<String>,
    val items: List<ScenarioItem>,
    val minLevel: ProficiencyLevel,
    val stampTitle: String
) {
    companion object {
        val ALLOWED_MOODS = setOf("happy", "neutral", "confused", "annoyed", "impressed")
    }

    /**
     * Drops hallucinated or repeated item ids and unknown moods, and only
     * honors `completed` once every item has been earned.
     */
    fun validateEvent(event: ScenarioEvent, alreadyEarned: Set<String>): ScenarioEvent {
        val validItem = event.itemEarned?.takeIf { id ->
            items.any { it.id == id } && id !in alreadyEarned
        }
        val earnedAfter = alreadyEarned + listOfNotNull(validItem)
        return ScenarioEvent(
            itemEarned = validItem,
            mood = event.mood?.takeIf { it in ALLOWED_MOODS },
            completed = event.completed && earnedAfter.containsAll(items.map { it.id })
        )
    }
}
