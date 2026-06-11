package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.CoachResponse
import kotlinx.serialization.json.Json

/**
 * Parses raw LLM output into a [CoachResponse].
 *
 * Structured fields (choices, wordBank, correction, scenario) are only
 * recovered from well-formed JSON; the regex fallback yields scalars only,
 * so callers must treat null structured fields as "fall back to free input".
 */
internal object CoachResponseParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(raw: String): CoachResponse {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        return runCatching {
            json.decodeFromString<CoachResponse>(cleaned)
        }.getOrElse {
            val hanzi = extractField(cleaned, "hanzi") ?: cleaned
            val pinyin = extractField(cleaned, "pinyin") ?: ""
            val english = extractField(cleaned, "english") ?: ""
            val tip = extractField(cleaned, "tip")
            CoachResponse(hanzi = hanzi, pinyin = pinyin, english = english, tip = tip)
        }.sanitized()
    }

    private fun CoachResponse.sanitized(): CoachResponse = copy(
        choices = choices?.takeIf { opts ->
            opts.size in 2..4 && opts.all { it.hanzi.isNotBlank() }
        },
        wordBank = wordBank?.takeIf { bank ->
            bank.size >= 2 && bank.all { it.isNotBlank() }
        }
    )

    private fun extractField(json: String, field: String): String? {
        val pattern = """"$field"\s*:\s*"([^"]*(?:\\.[^"]*)*)"""".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\n", "\n")
    }
}
