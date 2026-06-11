package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompletionRecord(
    val epochMillis: Long,
    val stage: String = ""
)
