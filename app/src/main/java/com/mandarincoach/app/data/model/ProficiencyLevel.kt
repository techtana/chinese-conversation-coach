package com.mandarincoach.app.data.model

enum class ProficiencyLevel(
    val hanzi: String,
    val pinyin: String,
    val englishName: String,
    val description: String,
    val emoji: String
) {
    BEGINNER(
        hanzi = "初级",
        pinyin = "Chūjí",
        englishName = "Beginner",
        description = "Greetings, numbers, basic daily phrases",
        emoji = "🌱"
    ),
    INTERMEDIATE(
        hanzi = "中级",
        pinyin = "Zhōngjí",
        englishName = "Intermediate",
        description = "Daily life, travel, emotions, simple opinions",
        emoji = "🌿"
    ),
    ADVANCED(
        hanzi = "高级",
        pinyin = "Gāojí",
        englishName = "Advanced",
        description = "Complex topics, idioms (成语), news & culture",
        emoji = "🎋"
    ),
    FLUENT(
        hanzi = "流利",
        pinyin = "Liúlì",
        englishName = "Fluent",
        description = "Native-level conversation, slang, nuance",
        emoji = "🏮"
    )
}
