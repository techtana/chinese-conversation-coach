package com.mandarincoach.app.data.model

/**
 * The 4-step "Beginner Bridge": scaffolding that is removed stage by stage
 * as the learner gains confidence. Only applies to the BEGINNER level;
 * other levels always converse in FREE_FLOW.
 */
enum class BridgeStage(
    val hanzi: String,
    val englishName: String,
    val description: String,
    val emoji: String
) {
    PASSIVE_INPUT(
        hanzi = "选择回答",
        englishName = "Guided Choices",
        description = "Pick your reply from curated options",
        emoji = "🔁"
    ),
    FRAGMENT_BUILDING(
        hanzi = "词语拼接",
        englishName = "Word Building",
        description = "Assemble your reply from a word bank",
        emoji = "🏗️"
    ),
    SCAFFOLDED(
        hanzi = "辅助输入",
        englishName = "Assisted Typing",
        description = "Type freely with inline translation help",
        emoji = "🤝"
    ),
    FREE_FLOW(
        hanzi = "自由对话",
        englishName = "Free Conversation",
        description = "Open text and voice conversation",
        emoji = "🚀"
    );

    fun next(): BridgeStage? = entries.getOrNull(ordinal + 1)
}
