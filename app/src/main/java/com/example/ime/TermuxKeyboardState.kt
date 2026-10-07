package com.example.ime

import com.example.data.ClipboardEntity
import com.example.data.CommandEntity
import com.example.data.RankedSuggestion

enum class OneHandedMode {
    FULL,
    LEFT,
    RIGHT
}

enum class ShiftState {
    OFF,
    ONCE,
    CAPS_LOCK
}

data class TermuxKeyboardUiState(
    val currentInputLine: String = "",
    val activeQuery: String = "",
    val relatedCommands: List<CommandEntity> = emptyList(),
    val recentClipboardFifteen: List<ClipboardEntity> = emptyList(),
    val mostUsedAndRelated: List<RankedSuggestion> = emptyList(),
    val ctrlLatched: Boolean = false,
    val altLatched: Boolean = false,
    val shiftState: ShiftState = ShiftState.OFF,
    val isSymbolLayer: Boolean = false,
    val oneHandedMode: OneHandedMode = OneHandedMode.FULL,
    val isBottomVaultExpanded: Boolean = false
)

interface KeyboardActionHandler {
    fun onCharacterKey(char: String)
    fun onShortcutKey(shortcut: TermuxShortcutKey)
    fun onBackspace()
    fun onEnter()
    fun onSpace()
    fun onToggleCtrl()
    fun onToggleAlt()
    fun onToggleShift()
    fun onToggleSymbolLayer()
    fun onCycleOneHandedMode()
    fun onToggleBottomVault()
    fun onSetBottomVaultExpanded(expanded: Boolean) {}
    fun onSelectCommandSuggestion(command: CommandEntity)
    fun onSelectClipboardItem(item: ClipboardEntity)
    fun onSelectRankedSuggestion(suggestion: RankedSuggestion)
    fun onSwitchInputMethod() {}
}

enum class TermuxShortcutKey(val label: String, val insertText: String? = null) {
    CTRL("CTRL"),
    ALT("ALT"),
    ESC("ESC"),
    TAB("TAB"),
    UP("↑"),
    DOWN("↓"),
    LEFT("←"),
    RIGHT("→"),
    SLASH("/", "/"),
    TILDE("~", "~"),
    PIPE("|", " | "),
    AND_AND("&&", " && "),
    GREATER(">", " > "),
    LESS("<", " < "),
    DASH("-", "-"),
    UNDERSCORE("_", "_")
}
