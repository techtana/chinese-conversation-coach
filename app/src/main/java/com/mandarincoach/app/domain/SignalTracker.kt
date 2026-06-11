package com.mandarincoach.app.domain

/**
 * Per-session interaction signals for the adaptive engine: how much the
 * learner deletes while drafting (self-correction churn) and how often
 * speech input is restarted without producing a message.
 */
class SignalTracker {

    private var lastDraftLength = 0
    private var editChurn = 0
    private var micTaps = 0
    private var sttSends = 0

    fun onDraftChanged(text: String) {
        if (text.length < lastDraftLength) editChurn++
        lastDraftLength = text.length
    }

    fun onMicTap() {
        micTaps++
    }

    fun onSttSend() {
        sttSends++
    }

    /** Mic sessions that never produced a message. */
    val sttRestarts: Int get() = (micTaps - sttSends).coerceAtLeast(0)

    data class Snapshot(val editChurn: Int, val sttRestarts: Int)

    /** Returns the accumulated signals and resets for the next turn. */
    fun snapshotAndReset(): Snapshot {
        val snap = Snapshot(editChurn, sttRestarts)
        editChurn = 0
        micTaps = 0
        sttSends = 0
        lastDraftLength = 0
        return snap
    }
}
