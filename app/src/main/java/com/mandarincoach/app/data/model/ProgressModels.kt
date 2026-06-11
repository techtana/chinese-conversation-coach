package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompletionRecord(
    val epochMillis: Long,
    val stage: String = ""
)

@Serializable
data class StreakState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveEpochDay: Long = 0
)

/** Aggregate-on-write bucket so per-sample data never accumulates. */
@Serializable
data class WeekAggregate(
    val sumMs: Long = 0,
    val count: Int = 0
) {
    val averageMs: Long get() = if (count == 0) 0 else sumMs / count
}
