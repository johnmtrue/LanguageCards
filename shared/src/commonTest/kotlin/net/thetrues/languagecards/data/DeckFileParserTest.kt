package net.thetrues.languagecards.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests that JSON deck file format errors are caught and reported.
 * Runs on iOS simulator (iosSimulatorArm64Test) and Android/JVM (androidUnitTest).
 */
class DeckFileParserTest {

    @Test
    fun parse_invalidJson_returnsFailure() {
        val result = DeckFileParser.parse("not valid json {{{")
        assertFalse(result.isSuccess)
        assertNotNull(result.exceptionOrNull())
        val message = DeckFileParser.formatParseError(result.exceptionOrNull())
        assertTrue(message.isNotBlank())
        assertTrue(message.startsWith("Invalid deck file format"))
    }

    @Test
    fun parse_emptyObject_returnsFailure() {
        val result = DeckFileParser.parse("{}")
        assertFalse(result.isSuccess)
        val message = DeckFileParser.formatParseError(result.exceptionOrNull())
        assertTrue(message.startsWith("Invalid deck file format"))
    }

    @Test
    fun parse_missingRequiredFields_returnsFailure() {
        val json = """{"languageCombo": {"id": "en-fr", "name": "E-F", "sideAName": "E", "sideBName": "F"}}"""
        val result = DeckFileParser.parse(json)
        assertFalse(result.isSuccess)
        val message = DeckFileParser.formatParseError(result.exceptionOrNull())
        assertTrue(message.startsWith("Invalid deck file format"))
    }

    @Test
    fun formatParseError_withNull_returnsDefaultMessage() {
        val message = DeckFileParser.formatParseError(null)
        assertTrue(message.isNotBlank())
        assertTrue(message.startsWith("Invalid deck file format"))
    }
}
