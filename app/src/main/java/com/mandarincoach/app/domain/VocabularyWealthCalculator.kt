package com.mandarincoach.app.domain

import com.mandarincoach.app.data.repository.DictionaryRepository

/**
 * "Vocabulary Wealth": the utility-weighted worth of the words a learner
 * has mastered, instead of a raw word count. Words are weighted by HSK
 * level; words beyond the bundled HSK 1-3 dictionary are rarer and
 * therefore worth the most.
 */
object VocabularyWealthCalculator {

    private const val BEYOND_DICTIONARY_WEIGHT = 4

    fun wealth(learnedWords: Set<String>): Int = learnedWords.sumOf { word ->
        val hsk = DictionaryRepository.lookup(word)?.hskLevel ?: 0
        if (hsk in 1..3) hsk else BEYOND_DICTIONARY_WEIGHT
    }
}
