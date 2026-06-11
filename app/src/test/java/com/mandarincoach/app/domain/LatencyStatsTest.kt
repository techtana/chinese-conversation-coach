package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.WeekAggregate
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class LatencyStatsTest {

    private val monday = LocalDate.of(2026, 6, 8)

    @Test
    fun `samples accumulate into the same week bucket`() {
        var agg = LatencyStats.addSample(emptyMap(), monday, 2000)!!
        agg = LatencyStats.addSample(agg, monday.plusDays(2), 4000)!!
        val key = LatencyStats.weekKey(monday)
        assertEquals(1, agg.size)
        assertEquals(WeekAggregate(6000, 2), agg[key])
        assertEquals(3000, agg[key]!!.averageMs)
    }

    @Test
    fun `different weeks use different buckets`() {
        var agg = LatencyStats.addSample(emptyMap(), monday, 1000)!!
        agg = LatencyStats.addSample(agg, monday.plusWeeks(1), 1000)!!
        assertEquals(2, agg.size)
    }

    @Test
    fun `walk-away outliers are discarded`() {
        assertNull(LatencyStats.addSample(emptyMap(), monday, LatencyStats.MAX_SAMPLE_MS + 1))
    }

    @Test
    fun `non-positive samples are discarded`() {
        assertNull(LatencyStats.addSample(emptyMap(), monday, 0))
        assertNull(LatencyStats.addSample(emptyMap(), monday, -100))
    }

    @Test
    fun `week key format is stable`() {
        val key = LatencyStats.weekKey(monday)
        assertTrue(key.matches(Regex("""\d{4}-W\d{2}""")))
        // Same key throughout the same week
        assertEquals(key, LatencyStats.weekKey(monday.plusDays(6)))
        assertNotEquals(key, LatencyStats.weekKey(monday.plusDays(7)))
    }
}
