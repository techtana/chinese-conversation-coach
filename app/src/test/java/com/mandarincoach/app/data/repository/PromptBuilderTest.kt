package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.BridgeStage
import com.mandarincoach.app.data.model.ProficiencyLevel
import org.junit.Assert.*
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun `default prompt keeps persona, vocab and JSON contract`() {
        val prompt = PromptBuilder.build(ProficiencyLevel.BEGINNER)
        assertTrue(prompt.contains("小明"))
        assertTrue(prompt.contains("Student level: Beginner"))
        assertTrue(prompt.contains("Core vocabulary"))
        assertTrue(prompt.contains("\"hanzi\""))
        assertTrue(prompt.contains("\"pinyin\""))
        assertTrue(prompt.contains("\"english\""))
        assertTrue(prompt.contains("\"tip\""))
        assertTrue(prompt.contains("Never break from the JSON format"))
    }

    @Test
    fun `user context appears when provided and is omitted when blank`() {
        val with = PromptBuilder.build(ProficiencyLevel.BEGINNER, userName = "John", learningGoals = "Travel")
        assertTrue(with.contains("John"))
        assertTrue(with.contains("Travel"))

        val without = PromptBuilder.build(ProficiencyLevel.BEGINNER)
        assertFalse(without.contains("Student Profile"))
    }

    @Test
    fun `active vocabulary is used verbatim when provided`() {
        val prompt = PromptBuilder.build(
            ProficiencyLevel.BEGINNER,
            activeVocab = listOf("你好", "谢谢")
        )
        assertTrue(prompt.contains("你好、谢谢"))
    }

    @Test
    fun `delegation through VocabularyRepository matches direct build`() {
        val direct = PromptBuilder.build(
            ProficiencyLevel.INTERMEDIATE,
            userName = "Ana",
            learningGoals = "HSK 4",
            interests = "Cooking",
            activeVocab = listOf("旅游", "飞机")
        )
        val delegated = VocabularyRepository.buildSystemPrompt(
            ProficiencyLevel.INTERMEDIATE,
            userName = "Ana",
            learningGoals = "HSK 4",
            interests = "Cooking",
            activeVocab = listOf("旅游", "飞机")
        )
        assertEquals(direct, delegated)
    }

    @Test
    fun `passive input stage requires choices in contract`() {
        val prompt = PromptBuilder.build(ProficiencyLevel.BEGINNER, stage = BridgeStage.PASSIVE_INPUT)
        assertTrue(prompt.contains("Guided Choices"))
        assertTrue(prompt.contains("\"choices\""))
        assertFalse(prompt.contains("\"wordBank\""))
    }

    @Test
    fun `fragment building stage requires word bank and hides answer`() {
        val prompt = PromptBuilder.build(ProficiencyLevel.BEGINNER, stage = BridgeStage.FRAGMENT_BUILDING)
        assertTrue(prompt.contains("\"wordBank\""))
        assertTrue(prompt.contains("\"expectedAnswer\""))
        assertTrue(prompt.contains("Do NOT reveal the expected answer"))
        assertFalse(prompt.contains("\"choices\": ["))
    }

    @Test
    fun `free flow stage omits scaffolding fields`() {
        val prompt = PromptBuilder.build(ProficiencyLevel.BEGINNER, stage = BridgeStage.FREE_FLOW)
        assertFalse(prompt.contains("\"choices\""))
        assertFalse(prompt.contains("\"wordBank\""))
        assertFalse(prompt.contains("LEARNING MODE"))
    }

    @Test
    fun `correction instruction always present`() {
        BridgeStage.entries.forEach { stage ->
            val prompt = PromptBuilder.build(ProficiencyLevel.BEGINNER, stage = stage)
            assertTrue("stage $stage missing correction", prompt.contains("\"correction\""))
        }
    }
}
