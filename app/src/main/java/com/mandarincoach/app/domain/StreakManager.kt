package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.StreakState
import java.time.LocalDate
import kotlin.random.Random

data class StreakResult(
    val state: StreakState,
    val nudge: String? = null
)

/**
 * Day-streak bookkeeping with adult-to-adult nudges instead of
 * guilt-trips when a streak breaks.
 */
object StreakManager {

    private val WITTY_NUDGES = listOf(
        "I get it, you had a long night out. A quick 2-minute hangover-recovery chat brings the streak back.",
        "Your streak ghosted you — but unlike most ghosts, this one answers texts. One short chat revives it.",
        "小明 ordered for two yesterday and you never showed. He'll forgive you for the price of one conversation.",
        "Day off? Respect. Day two is how habits die, though. Two minutes, that's the whole ask.",
        "The vocabulary you learned is still here. It's been gossiping about you in Mandarin. Come defend yourself."
    )

    fun onAppOpen(
        today: LocalDate,
        state: StreakState,
        random: Random = Random.Default
    ): StreakResult {
        val epochDay = today.toEpochDay()
        return when {
            // First ever open
            state.lastActiveEpochDay == 0L -> StreakResult(
                StreakState(currentStreak = 1, longestStreak = maxOf(1, state.longestStreak), lastActiveEpochDay = epochDay)
            )
            // Already counted today
            epochDay == state.lastActiveEpochDay -> StreakResult(state)
            // Time went backwards (clock change) — keep state
            epochDay < state.lastActiveEpochDay -> StreakResult(state)
            // Consecutive day
            epochDay == state.lastActiveEpochDay + 1 -> {
                val streak = state.currentStreak + 1
                StreakResult(
                    StreakState(streak, maxOf(streak, state.longestStreak), epochDay)
                )
            }
            // Streak broken
            else -> StreakResult(
                state = StreakState(currentStreak = 1, longestStreak = state.longestStreak, lastActiveEpochDay = epochDay),
                nudge = WITTY_NUDGES[random.nextInt(WITTY_NUDGES.size)]
            )
        }
    }
}
