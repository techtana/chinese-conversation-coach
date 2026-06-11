package com.mandarincoach.app.domain

import kotlin.random.Random

/**
 * Immutable state for the fragment-building input: a shuffled bank of
 * word chips and the learner's assembled reply. Words can repeat, so
 * chips are tracked by index rather than value.
 */
data class WordBankState(
    val bank: List<String>,
    val assembled: List<String> = emptyList()
) {
    companion object {
        fun fromWords(words: List<String>, random: Random = Random.Default) =
            WordBankState(bank = words.shuffled(random))
    }

    fun pick(index: Int): WordBankState {
        val word = bank.getOrNull(index) ?: return this
        return copy(
            bank = bank.toMutableList().also { it.removeAt(index) },
            assembled = assembled + word
        )
    }

    fun unpick(index: Int): WordBankState {
        val word = assembled.getOrNull(index) ?: return this
        return copy(
            bank = bank + word,
            assembled = assembled.toMutableList().also { it.removeAt(index) }
        )
    }

    fun clear(): WordBankState = copy(bank = bank + assembled, assembled = emptyList())

    fun assembledText(): String = assembled.joinToString("")
}
