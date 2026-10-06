package com.example

import com.example.data.SmartSuggestionEngine
import com.example.data.TermuxCommandSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun smartSuggestions_mapsNaturalLanguageIntentsCorrectly() {
        val commands = TermuxCommandSeedData.getInitialCommands()

        val installNode = SmartSuggestionEngine.rankCommandSuggestions("install node", commands)
        assertEquals("pkg install nodejs", installNode.first().command)

        val listFiles = SmartSuggestionEngine.rankCommandSuggestions("list files", commands)
        assertEquals("ls -la", listFiles.first().command)

        val openProject = SmartSuggestionEngine.rankCommandSuggestions("open project", commands)
        assertEquals("cd ~/projects", openProject.first().command)

        val gitStatus = SmartSuggestionEngine.rankCommandSuggestions("git status", commands)
        assertEquals("git status", gitStatus.first().command)
    }

    @Test
    fun relatedClipboardSuggestions_matchesTypedWord() {
        val commands = TermuxCommandSeedData.getInitialCommands()
        val clips = TermuxCommandSeedData.getInitialClipboardItems()

        val opencodeRelated = SmartSuggestionEngine.rankMostUsedAndRelated(
            rawInput = "opencode",
            commands = commands,
            clipboardItems = clips,
            limit = 6
        )

        assertTrue(opencodeRelated.isNotEmpty())
        assertTrue(opencodeRelated.any { it.isClipboard && it.insertText.contains("opencode", ignoreCase = true) })
        assertTrue(opencodeRelated.any { !it.isClipboard && it.insertText.contains("opencode", ignoreCase = true) })
    }
}
