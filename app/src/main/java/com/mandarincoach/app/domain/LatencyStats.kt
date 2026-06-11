package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.WeekAggregate
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Response-latency bookkeeping: how quickly the learner replies after
 * the coach's message, bucketed per ISO week so progress can be shown
 * week-over-week.
 */
object LatencyStats {

    /** Replies slower than this mean the user walked away, not hesitation. */
    const val MAX_SAMPLE_MS = 5 * 60 * 1000L

    fun weekKey(date: LocalDate): String {
        val week = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        val year = date.get(WeekFields.ISO.weekBasedYear())
        return "%04d-W%02d".format(year, week)
    }

    /** Returns the updated aggregates, or null when the sample is discarded. */
    fun addSample(
        aggregates: Map<String, WeekAggregate>,
        date: LocalDate,
        deltaMs: Long
    ): Map<String, WeekAggregate>? {
        if (deltaMs <= 0 || deltaMs > MAX_SAMPLE_MS) return null
        val key = weekKey(date)
        val current = aggregates[key] ?: WeekAggregate()
        return aggregates + (key to WeekAggregate(current.sumMs + deltaMs, current.count + 1))
    }
}
