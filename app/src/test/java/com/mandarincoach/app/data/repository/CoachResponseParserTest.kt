package com.mandarincoach.app.data.repository

import org.junit.Assert.*
import org.junit.Test

class CoachResponseParserTest {

    @Test
    fun `parses clean JSON with core fields`() {
        val raw = """{"hanzi": "你好！", "pinyin": "Nǐ hǎo!", "english": "Hello!", "tip": "Greeting"}"""
        val result = CoachResponseParser.parse(raw)
        assertEquals("你好！", result.hanzi)
        assertEquals("Nǐ hǎo!", result.pinyin)
        assertEquals("Hello!", result.english)
        assertEquals("Greeting", result.tip)
        assertNull(result.choices)
        assertNull(result.wordBank)
        assertNull(result.correction)
    }

    @Test
    fun `parses fenced JSON code block`() {
        val raw = """
            ```json
            {"hanzi": "好", "pinyin": "hǎo", "english": "good"}
            ```
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertEquals("好", result.hanzi)
        assertNull(result.tip)
    }

    @Test
    fun `parses choices array`() {
        val raw = """
            {"hanzi": "你要喝什么？", "pinyin": "Nǐ yào hē shénme?", "english": "What do you want to drink?",
             "choices": [
               {"hanzi": "我要咖啡", "pinyin": "Wǒ yào kāfēi", "english": "I want coffee"},
               {"hanzi": "我要茶", "pinyin": "Wǒ yào chá", "english": "I want tea"}
             ]}
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertEquals(2, result.choices?.size)
        assertEquals("我要咖啡", result.choices?.first()?.hanzi)
    }

    @Test
    fun `parses wordBank and expectedAnswer`() {
        val raw = """
            {"hanzi": "你叫什么名字？", "pinyin": "Nǐ jiào shénme míngzi?", "english": "What is your name?",
             "expectedAnswer": "我叫小王", "wordBank": ["我", "叫", "小王", "是"]}
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertEquals("我叫小王", result.expectedAnswer)
        assertEquals(listOf("我", "叫", "小王", "是"), result.wordBank)
    }

    @Test
    fun `parses correction and scenario event`() {
        val raw = """
            {"hanzi": "好的", "pinyin": "hǎo de", "english": "OK",
             "correction": {"userSaid": "我想要房间", "better": "我要一个房间", "note": "measure word"},
             "scenario": {"itemEarned": "room_key", "mood": "happy", "completed": false}}
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertEquals("我想要房间", result.correction?.userSaid)
        assertEquals("room_key", result.scenario?.itemEarned)
        assertEquals("happy", result.scenario?.mood)
        assertFalse(result.scenario!!.completed)
    }

    @Test
    fun `malformed JSON falls back to regex extraction with null structured fields`() {
        val raw = """{"hanzi": "你好", "pinyin": "nǐ hǎo", "english": "hello", "choices": [BROKEN"""
        val result = CoachResponseParser.parse(raw)
        assertEquals("你好", result.hanzi)
        assertEquals("nǐ hǎo", result.pinyin)
        assertEquals("hello", result.english)
        assertNull(result.choices)
        assertNull(result.wordBank)
    }

    @Test
    fun `plain text becomes hanzi`() {
        val result = CoachResponseParser.parse("你好！")
        assertEquals("你好！", result.hanzi)
        assertEquals("", result.pinyin)
    }

    @Test
    fun `single choice is dropped as invalid`() {
        val raw = """
            {"hanzi": "好", "pinyin": "hǎo", "english": "good",
             "choices": [{"hanzi": "好", "pinyin": "hǎo", "english": "good"}]}
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertNull(result.choices)
    }

    @Test
    fun `blank choice hanzi invalidates the whole choices array`() {
        val raw = """
            {"hanzi": "好", "pinyin": "hǎo", "english": "good",
             "choices": [{"hanzi": "", "pinyin": "", "english": ""}, {"hanzi": "好", "pinyin": "hǎo", "english": "good"}]}
        """.trimIndent()
        val result = CoachResponseParser.parse(raw)
        assertNull(result.choices)
    }

    @Test
    fun `word bank with fewer than two words is dropped`() {
        val raw = """{"hanzi": "好", "pinyin": "hǎo", "english": "good", "wordBank": ["我"]}"""
        val result = CoachResponseParser.parse(raw)
        assertNull(result.wordBank)
    }
}
