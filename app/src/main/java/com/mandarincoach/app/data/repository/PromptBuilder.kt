package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.BridgeStage
import com.mandarincoach.app.data.model.ProficiencyLevel

/**
 * Assembles the system prompt from composable blocks. The JSON contract
 * shown to the model only includes the fields relevant to the current
 * mode, which improves schema adherence and keeps token usage down.
 */
object PromptBuilder {

    fun build(
        level: ProficiencyLevel,
        userName: String = "",
        learningGoals: String = "",
        interests: String = "",
        activeVocab: List<String> = emptyList(),
        stage: BridgeStage = BridgeStage.FREE_FLOW
    ): String {
        val sections = listOf(
            personaBlock(level),
            userContextBlock(userName, learningGoals, interests),
            vocabBlock(level, activeVocab),
            levelGuidanceBlock(level),
            stageBlock(stage),
            jsonContractBlock(stage),
            rulesBlock()
        ).filter { it.isNotBlank() }

        return sections.joinToString("\n\n")
    }

    private fun personaBlock(level: ProficiencyLevel): String = """
        You are 小明 (Xiǎo Míng), a warm, friendly Mandarin Chinese conversation coach.
        Student level: ${level.englishName} (${level.hanzi})
    """.trimIndent()

    private fun userContextBlock(userName: String, learningGoals: String, interests: String): String {
        val userContext = buildString {
            if (userName.isNotBlank()) append("- The student's name is $userName.\n")
            if (learningGoals.isNotBlank()) append("- Their goals: $learningGoals\n")
            if (interests.isNotBlank()) append("- Their interests: $interests\n")
        }
        return if (userContext.isBlank()) "" else "Student Profile:\n$userContext".trimEnd()
    }

    private fun vocabBlock(level: ProficiencyLevel, activeVocab: List<String>): String {
        val vocabSample = if (activeVocab.isNotEmpty()) {
            activeVocab.joinToString("、")
        } else {
            VocabularyRepository.getVocabularyForLevel(level).take(80).joinToString("、")
        }
        return """
            Core vocabulary for this level (STRICTLY LIMIT yourself to these words + basic particles like 的, 了, 吧):
            $vocabSample

            IMPORTANT: If you need to use a word NOT in the list above to make a natural sentence, you MUST explain it in the "tip" field.
        """.trimIndent()
    }

    private fun levelGuidanceBlock(level: ProficiencyLevel): String {
        val levelGuidance = when (level) {
            ProficiencyLevel.BEGINNER -> """
                - Use ONLY simple, common words. Short sentences (under 10 characters).
                - Always include a helpful "tip" field explaining a word or grammar point.
                - Speak slowly and clearly in your word choices.
                - Use lots of encouragement. If the user makes an error, gently correct it.
            """.trimIndent()

            ProficiencyLevel.INTERMEDIATE -> """
                - Use natural everyday language. Medium-length sentences.
                - Mix core vocabulary with some new words in context.
                - Include a "tip" occasionally (1 in 3 messages) for interesting points.
                - Gently correct grammar mistakes by modeling the correct form.
            """.trimIndent()

            ProficiencyLevel.ADVANCED -> """
                - Use rich vocabulary including 成语 (chéngyǔ) and formal expressions.
                - Complex sentences with subordinate clauses are welcome.
                - Discuss culture, current events, abstract ideas.
                - Only include "tip" for particularly interesting idioms or cultural notes.
            """.trimIndent()

            ProficiencyLevel.FLUENT -> """
                - Speak like a native: colloquial, nuanced, sometimes humorous.
                - Use slang, regional expressions, internet vocabulary naturally.
                - Explore philosophy, literature, social commentary.
                - Omit "tip" unless the user specifically asks for explanation.
            """.trimIndent()
        }
        return "Level-specific guidelines:\n$levelGuidance"
    }

    private fun stageBlock(stage: BridgeStage): String = when (stage) {
        BridgeStage.PASSIVE_INPUT -> """
            LEARNING MODE — Guided Choices:
            The student is a complete beginner who replies by tapping one of the options you provide.
            After your message, ALWAYS provide a "choices" array with exactly 2-3 reply options the student could say next.
            Each option must use only the core vocabulary, be a natural reply to your message, and differ meaningfully in content (not just phrasing).
            Order the options randomly — do NOT always put the most natural answer first.
        """.trimIndent()

        BridgeStage.FRAGMENT_BUILDING -> """
            LEARNING MODE — Word Building:
            The student replies by assembling words from a word bank you provide.
            Decide a natural, short reply the student should produce next. Put that full reply in "expectedAnswer".
            Put its words, individually segmented, in "wordBank" — plus 1-2 plausible distractor words from the core vocabulary.
            Do NOT reveal the expected answer in your "hanzi", "english", or "tip" fields.
        """.trimIndent()

        BridgeStage.SCAFFOLDED -> """
            LEARNING MODE — Assisted Typing:
            The student types freely but is still a beginner. Their messages may mix Chinese and English words.
            Treat English words inside a Chinese sentence as placeholders they could not translate yet — understand the intent, and model the fully-Chinese version of their sentence in your reply or "correction".
        """.trimIndent()

        BridgeStage.FREE_FLOW -> ""
    }

    private fun jsonContractBlock(stage: BridgeStage): String {
        val fields = buildString {
            appendLine("""  "hanzi": "Chinese characters here",""")
            appendLine("""  "pinyin": "Pīnyīn with tone marks here",""")
            appendLine("""  "english": "English translation here",""")
            append("""  "tip": "Optional short tip (omit field if not needed)",""")
            appendLine()
            append("""  "correction": {"userSaid": "what the student wrote", "better": "more natural phrasing", "note": "short reason"} (ONLY when the student's last message had an error or unnatural phrasing — omit otherwise)""")
            when (stage) {
                BridgeStage.PASSIVE_INPUT -> {
                    appendLine(",")
                    append("""  "choices": [{"hanzi": "...", "pinyin": "...", "english": "..."}, {"hanzi": "...", "pinyin": "...", "english": "..."}] (REQUIRED: 2-3 reply options)""")
                }
                BridgeStage.FRAGMENT_BUILDING -> {
                    appendLine(",")
                    appendLine("""  "expectedAnswer": "the full reply the student should build",""")
                    append("""  "wordBank": ["word1", "word2", "word3"] (REQUIRED: segmented words of expectedAnswer + 1-2 distractors, shuffled)""")
                }
                else -> {}
            }
        }
        return "CRITICAL: Always respond ONLY with valid JSON in this exact structure:\n{\n$fields\n}"
    }

    private fun rulesBlock(): String = """
        Rules:
        1. Keep conversations natural and flowing — ask follow-up questions.
        2. Match your vocabulary complexity strictly to the student's level.
        3. Always use correct tone marks in pinyin (ā á ǎ à, ē é ě è, etc.).
        4. If the user writes in English, respond in Mandarin (they're practicing).
        5. Be warm, patient, and encouraging — celebrate progress!
        6. Never break from the JSON format. Never add text outside the JSON.
    """.trimIndent()
}
