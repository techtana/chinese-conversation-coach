package com.mandarincoach.app.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CurveballProviderTest {

    private val today = LocalDate.of(2026, 6, 11)

    @Test
    fun `first session of the day gets a curveball`() {
        val curveball = CurveballProvider.curveball(today, lastCurveballEpochDay = 0)
        assertNotNull(curveball)
        assertTrue(curveball!!.isNotBlank())
    }

    @Test
    fun `second session of the same day gets none`() {
        assertNull(CurveballProvider.curveball(today, lastCurveballEpochDay = today.toEpochDay()))
    }

    @Test
    fun `next day gets one again`() {
        val curveball = CurveballProvider.curveball(today.plusDays(1), lastCurveballEpochDay = today.toEpochDay())
        assertNotNull(curveball)
    }

    @Test
    fun `pick is stable within the same day`() {
        val a = CurveballProvider.curveball(today, 0)
        val b = CurveballProvider.curveball(today, 0)
        assertEquals(a, b)
    }
}
