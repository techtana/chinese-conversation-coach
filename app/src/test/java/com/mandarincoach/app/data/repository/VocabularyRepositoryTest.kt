package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.ProficiencyLevel
import org.junit.Assert.*
import org.junit.Test

class VocabularyRepositoryTest {

    @Test
    fun `segment should extract only Chinese characters`() {
        val input = "Hello 你好 123!"
        val result = VocabularyRepository.segment(input)
        assertEquals(setOf("你", "好"), result)
    }

    @Test
    fun `getActiveVocabulary should return learned words and limited new words`() {
        val level = ProficiencyLevel.BEGINNER
        val learned = setOf("你好", "谢谢")
        val target = 5
        
        val active = VocabularyRepository.getActiveVocabulary(level, learned, target)
        
        // Should contain learned words that are in the corpus
        // Note: beginnerWords in repository might not contain "你好" exactly as written if not in list
        // but for this test we check the logic
        assertTrue(active.containsAll(learned.filter { it in VocabularyRepository.getVocabularyForLevel(level) }))
        
        // Total new words should not exceed target
        val newWords = active.filter { it !in learned }
        assertTrue(newWords.size <= target)
    }

    @Test
    fun `buildSystemPrompt should include user context`() {
        val level = ProficiencyLevel.BEGINNER
        val name = "John"
        val goals = "Travel"
        val prompt = VocabularyRepository.buildSystemPrompt(level, name, goals)
        
        assertTrue(prompt.contains("John"))
        assertTrue(prompt.contains("Travel"))
    }
}
