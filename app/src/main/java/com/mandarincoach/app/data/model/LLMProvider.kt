package com.mandarincoach.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LLMProvider(
    val displayName: String,
    val defaultModel: String,
    val baseUrl: String,
    val inputCostPerMillion: Double,
    val outputCostPerMillion: Double
) {
    CLAUDE(
        "Anthropic Claude",
        "claude-3-haiku-20240307",
        "https://api.anthropic.com/v1/messages",
        0.25,
        1.25
    ),
    OPENAI(
        "OpenAI GPT",
        "gpt-4o-mini",
        "https://api.openai.org/v1/chat/completions",
        0.15,
        0.60
    ),
    DEEPSEEK(
        "DeepSeek",
        "deepseek-chat",
        "https://api.deepseek.com/chat/completions",
        0.14,
        0.28
    ),
    GOOGLE(
        "Google Gemini",
        "gemini-1.5-flash",
        "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
        0.075,
        0.30
    ),
    DEEPINFRA(
        "DeepInfra",
        "meta-llama/Llama-3-70b-chat-hf",
        "https://api.deepinfra.com/v1/openai/chat/completions",
        0.10,
        0.10
    ),
    AZURE(
        "Azure AI Foundry",
        "gpt-4o",
        "", // Requires custom endpoint
        0.15,
        0.60
    ),
    AWS_BEDROCK(
        "AWS Bedrock (via Gateway)",
        "anthropic.claude-3-haiku",
        "", // Requires custom gateway
        0.25,
        1.25
    ),
    PRIVATE(
        "Private API / Custom",
        "custom-model",
        "http://localhost:11434/v1/chat/completions",
        0.0,
        0.0
    )
}
