package com.mandarincoach.app.domain

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class WordBankStateTest {

    @Test
    fun `fromWords shuffles deterministically with seed`() {
        val words = listOf("我", "叫", "小王", "是", "你")
        val a = WordBankState.fromWords(words, Random(42))
        val b = WordBankState.fromWords(words, Random(42))
        assertEquals(a.bank, b.bank)
        assertEquals(words.sorted(), a.bank.sorted())
    }

    @Test
    fun `pick moves word from bank to assembled in order`() {
        val state = WordBankState(bank = listOf("我", "叫", "小王"))
            .pick(0)
            .pick(1) // "小王" after "叫" shifted? bank is now ["叫","小王"], index 1 = "小王"
        assertEquals(listOf("我", "小王"), state.assembled)
        assertEquals(listOf("叫"), state.bank)
    }

    @Test
    fun `unpick returns word to bank`() {
        val state = WordBankState(bank = listOf("我", "叫"))
            .pick(0)
            .unpick(0)
        assertEquals(emptyList<String>(), state.assembled)
        assertEquals(listOf("叫", "我"), state.bank)
    }

    @Test
    fun `duplicate words are tracked independently`() {
        val state = WordBankState(bank = listOf("很", "很", "好"))
            .pick(0)
            .pick(1) // bank now ["很","好"], picks "好"
        assertEquals(listOf("很", "好"), state.assembled)
        assertEquals(listOf("很"), state.bank)
    }

    @Test
    fun `clear returns all assembled words to bank`() {
        val state = WordBankState(bank = listOf("我", "叫", "小王"))
            .pick(0).pick(0).clear()
        assertTrue(state.assembled.isEmpty())
        assertEquals(listOf("我", "叫", "小王").sorted(), state.bank.sorted())
    }

    @Test
    fun `assembledText joins without separator`() {
        val state = WordBankState(bank = listOf("我", "叫", "小王"))
            .pick(0).pick(0).pick(0)
        assertEquals("我叫小王", state.assembledText())
    }

    @Test
    fun `out of range indices are ignored`() {
        val state = WordBankState(bank = listOf("我"))
        assertEquals(state, state.pick(5))
        assertEquals(state, state.unpick(0))
    }
}
