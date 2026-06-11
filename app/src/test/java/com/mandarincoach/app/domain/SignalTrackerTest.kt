package com.mandarincoach.app.domain

import org.junit.Assert.*
import org.junit.Test

class SignalTrackerTest {

    @Test
    fun `typing forward counts no churn`() {
        val tracker = SignalTracker()
        tracker.onDraftChanged("我")
        tracker.onDraftChanged("我想")
        tracker.onDraftChanged("我想要")
        assertEquals(SignalTracker.Snapshot(0, 0), tracker.snapshotAndReset())
    }

    @Test
    fun `deletions count as self-correction churn`() {
        val tracker = SignalTracker()
        tracker.onDraftChanged("我想要")
        tracker.onDraftChanged("我想")     // deleted
        tracker.onDraftChanged("我想吃")
        tracker.onDraftChanged("我")       // deleted again
        assertEquals(2, tracker.snapshotAndReset().editChurn)
    }

    @Test
    fun `mic taps without a send count as restarts`() {
        val tracker = SignalTracker()
        tracker.onMicTap()
        tracker.onMicTap()
        tracker.onSttSend()
        assertEquals(1, tracker.snapshotAndReset().sttRestarts)
    }

    @Test
    fun `sends never make restarts negative`() {
        val tracker = SignalTracker()
        tracker.onMicTap()
        tracker.onSttSend()
        tracker.onSttSend()
        assertEquals(0, tracker.snapshotAndReset().sttRestarts)
    }

    @Test
    fun `snapshot resets state for the next turn`() {
        val tracker = SignalTracker()
        tracker.onDraftChanged("abc")
        tracker.onDraftChanged("a")
        tracker.onMicTap()
        tracker.snapshotAndReset()
        assertEquals(SignalTracker.Snapshot(0, 0), tracker.snapshotAndReset())
    }
}
