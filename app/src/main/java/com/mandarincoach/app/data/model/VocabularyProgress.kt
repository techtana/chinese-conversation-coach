package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VocabularyProgress(
    val learnedWords: Set<String> = emptySet(),
    val targetNewWordsCount: Int = 30,
    val currentHSKLevel: Int = 1
)
