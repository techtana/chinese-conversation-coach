package com.mandarincoach.app.domain

import com.mandarincoach.app.data.model.StreakState
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StreakManagerTest {

    private val day = LocalDate.of(2026, 6, 10)

    @Test
    fun `first ever open starts a streak of one`() {
        val result = StreakManager.onAppOpen(day, StreakState())
        assertEquals(1, result.state.currentStreak)
        assertEquals(1, result.state.longestStreak)
        assertEquals(day.toEpochDay(), result.state.lastActiveEpochDay)
        assertNull(result.nudge)
    }

    @Test
    fun `same day reopen changes nothing`() {
        val state = StreakState(3, 5, day.toEpochDay())
        val result = StreakManager.onAppOpen(day, state)
        assertEquals(state, result.state)
        assertNull(result.nudge)
    }

    @Test
    fun `consecutive day increments and tracks longest`() {
        val state = StreakState(5, 5, day.toEpochDay())
        val result = StreakManager.onAppOpen(day.plusDays(1), state)
        assertEquals(6, result.state.currentStreak)
        assertEquals(6, result.state.longestStreak)
        assertNull(result.nudge)
    }

    @Test
    fun `one missed day resets streak with a nudge`() {
        val state = StreakState(7, 9, day.toEpochDay())
        val result = StreakManager.onAppOpen(day.plusDays(2), state)
        assertEquals(1, result.state.currentStreak)
        assertEquals(9, result.state.longestStreak)
        assertNotNull(result.nudge)
    }

    @Test
    fun `long gap also resets with longest retained`() {
        val state = StreakState(2, 14, day.toEpochDay())
        val result = StreakManager.onAppOpen(day.plusDays(30), state)
        assertEquals(1, result.state.currentStreak)
        assertEquals(14, result.state.longestStreak)
        assertNotNull(result.nudge)
    }

    @Test
    fun `clock moving backwards keeps state`() {
        val state = StreakState(4, 4, day.toEpochDay())
        val result = StreakManager.onAppOpen(day.minusDays(1), state)
        assertEquals(state, result.state)
        assertNull(result.nudge)
    }
}
