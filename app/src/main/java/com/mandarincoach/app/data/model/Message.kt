package com.mandarincoach.app.data.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val hanzi: String,
    val pinyin: String = "",
    val english: String = "",
    val tip: String? = null,
    val correction: Correction? = null,
    val timestamp: Long = System.currentTimeMillis()
)
