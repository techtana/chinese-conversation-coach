package com.mandarincoach.app.domain

import org.junit.Assert.*
import org.junit.Test

class VocabularyWealthCalculatorTest {

    @Test
    fun `empty vocabulary has zero wealth`() {
        assertEquals(0, VocabularyWealthCalculator.wealth(emptySet()))
    }

    @Test
    fun `words are weighted by HSK level`() {
        // 爱 is HSK 1 in the bundled dictionary
        assertEquals(1, VocabularyWealthCalculator.wealth(setOf("爱")))
    }

    @Test
    fun `words beyond the dictionary are worth the most`() {
        // 锦上添花 is a fluent-level idiom not in the HSK 1-3 dictionary
        assertEquals(4, VocabularyWealthCalculator.wealth(setOf("锦上添花")))
    }

    @Test
    fun `wealth sums across the whole set`() {
        val hsk1 = VocabularyWealthCalculator.wealth(setOf("爱"))
        val rare = VocabularyWealthCalculator.wealth(setOf("锦上添花"))
        assertEquals(hsk1 + rare, VocabularyWealthCalculator.wealth(setOf("爱", "锦上添花")))
    }

    @Test
    fun `wealth differs from raw count`() {
        val words = setOf("爱", "锦上添花")
        assertTrue(VocabularyWealthCalculator.wealth(words) > words.size)
    }
}
