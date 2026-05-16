package com.mandarincoach.app.data.model

data class DictionaryEntry(
    val hanzi: String,
    val pinyin: String,
    val definition: String,
    val hskLevel: Int = 0,
    val partOfSpeech: String = "",
    val examples: List<Pair<String, String>> = emptyList() // hanzi to english
)
