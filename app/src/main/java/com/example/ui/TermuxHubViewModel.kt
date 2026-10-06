package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClipboardEntity
import com.example.data.CommandCategory
import com.example.data.CommandEntity
import com.example.data.RankedSuggestion
import com.example.data.SmartSuggestionEngine
import com.example.data.TermuxRepository
import com.example.ime.KeyboardActionHandler
import com.example.ime.OneHandedMode
import com.example.ime.ShiftState
import com.example.ime.TermuxKeyboardUiState
import com.example.ime.TermuxShortcutKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalLogEntry(
    val id: Long = System.nanoTime(),
    val promptLine: String,
    val note: String
)

class TermuxHubViewModel(application: Application) :
    AndroidViewModel(application),
    KeyboardActionHandler {

    private val db = AppDatabase.getInstance(application)
    private val repository = TermuxRepository(db.commandDao(), db.clipboardDao())
    private val clipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    // Sandbox Terminal State
    private val _sandboxInputLine = MutableStateFlow("")
    val sandboxInputLine: StateFlow<String> = _sandboxInputLine.asStateFlow()

    private val _terminalHistory = MutableStateFlow<List<TerminalLogEntry>>(
        listOf(
            TerminalLogEntry(
                promptLine = "pkg update && pkg upgrade -y",
                note = "Inserted via Termux Smart Keyboard • Press Enter to commit line"
            )
        )
    )
    val terminalHistory: StateFlow<List<TerminalLogEntry>> = _terminalHistory.asStateFlow()

    private val _commandHistoryCursor = MutableStateFlow(-1)

    // Keyboard Modifiers State for the In-App Sandbox Keyboard
    private data class SandboxModifiers(
        val ctrlLatched: Boolean = false,
        val altLatched: Boolean = false,
        val shiftState: ShiftState = ShiftState.OFF,
        val isSymbolLayer: Boolean = false,
        val oneHandedMode: OneHandedMode = OneHandedMode.FULL,
        val isBottomVaultExpanded: Boolean = true
    )

    private val sandboxModifiers = MutableStateFlow(SandboxModifiers())

    // Database Browser Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CommandCategory.ALL)
    val selectedCategory: StateFlow<CommandCategory> = _selectedCategory.asStateFlow()

    // Status Toast / Banner message
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val allCommands: StateFlow<List<CommandEntity>> = repository.allCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClipboardItems: StateFlow<List<ClipboardEntity>> = repository.allClipboardItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentThreeClipboard: StateFlow<List<ClipboardEntity>> = repository.recentThreeClipboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCommands: StateFlow<List<CommandEntity>> = combine(
        repository.allCommands,
        _searchQuery,
        _selectedCategory
    ) { commands, query, category ->
        val categoryFiltered = if (category == CommandCategory.ALL) {
            commands
        } else if (category == CommandCategory.CUSTOM) {
            commands.filter { it.isCustom || it.category.equals("Custom", ignoreCase = true) }
        } else {
            commands.filter { it.category.equals(category.displayName, ignoreCase = true) }
        }

        if (query.isBlank()) {
            categoryFiltered
        } else {
            SmartSuggestionEngine.rankCommandSuggestions(
                rawInput = query,
                commands = categoryFiltered,
                limit = 100
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sandboxKeyboardState: StateFlow<TermuxKeyboardUiState> = combine(
        _sandboxInputLine,
        repository.allCommands,
        repository.allClipboardItems,
        repository.recentThreeClipboard,
        sandboxModifiers
    ) { line, commands, clips, recent3, mods ->
        val activeQuery = SmartSuggestionEngine.extractActiveQuery(line)
        val relatedCmds = SmartSuggestionEngine.rankCommandSuggestions(line, commands, limit = 10)
        val mostUsedAndRelated = SmartSuggestionEngine.rankMostUsedAndRelated(
            rawInput = line,
            commands = commands,
            clipboardItems = clips,
            limit = 5
        )
        TermuxKeyboardUiState(
            currentInputLine = line,
            activeQuery = activeQuery,
            relatedCommands = relatedCmds,
            recentClipboardTop3 = recent3,
            mostUsedAndRelated = mostUsedAndRelated,
            ctrlLatched = mods.ctrlLatched,
            altLatched = mods.altLatched,
            shiftState = mods.shiftState,
            isSymbolLayer = mods.isSymbolLayer,
            oneHandedMode = mods.oneHandedMode,
            isBottomVaultExpanded = mods.isBottomVaultExpanded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TermuxKeyboardUiState()
    )

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        syncSystemClipboard()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded(db)
        }
        clipboardManager?.addPrimaryClipChangedListener(clipListener)
        syncSystemClipboard()
    }

    override fun onCleared() {
        clipboardManager?.removePrimaryClipChangedListener(clipListener)
        super.onCleared()
    }

    fun syncSystemClipboard() {
        val clip = clipboardManager?.primaryClip ?: return
        if (clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.coerceToText(getApplication())?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.addOrTouchClipboardText(text, label = "System Clipboard")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: CommandCategory) {
        _selectedCategory.value = category
    }

    fun setSandboxInputLine(newLine: String) {
        _sandboxInputLine.value = newLine
    }

    fun clearSandboxTerminal() {
        _sandboxInputLine.value = ""
        _terminalHistory.value = emptyList()
    }

    fun dismissStatusBanner() {
        _statusBanner.value = null
    }

    fun copyTextToClipboard(text: String, label: String = "Copied from Termux Keyboard") {
        val clean = text.trim()
        if (clean.isEmpty()) return
        clipboardManager?.setPrimaryClip(ClipData.newPlainText(label, clean))
        viewModelScope.launch(Dispatchers.IO) {
            repository.addOrTouchClipboardText(clean, label = label)
        }
        _statusBanner.value = "Copied to clipboard: \"$clean\""
    }

    fun insertIntoSandbox(text: String, recordCommand: CommandEntity? = null) {
        replaceActiveSandboxQueryWithCommand(text)
        viewModelScope.launch(Dispatchers.IO) {
            if (recordCommand != null) {
                repository.recordCommandUsed(recordCommand)
            } else {
                repository.recordCommandTextUsed(text)
            }
        }
        _statusBanner.value = "Inserted \"$text\" into Terminal Input"
    }

    fun saveCustomCommand(
        id: Long = 0L,
        command: String,
        name: String,
        description: String,
        usage: String,
        examples: String,
        category: String,
        relatedCommands: String,
        tags: String,
        usageCount: Int = 0
    ) {
        if (command.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCustomCommand(
                id = id,
                command = command,
                name = name,
                description = description,
                usage = usage,
                examples = examples,
                category = category,
                relatedCommands = relatedCommands,
                tags = tags,
                usageCount = usageCount
            )
        }
        _statusBanner.value = if (id == 0L) {
            "Saved custom command: ${command.trim()}"
        } else {
            "Updated command: ${command.trim()}"
        }
    }

    fun deleteCommand(command: CommandEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCommand(command)
        }
        _statusBanner.value = "Deleted command: ${command.command}"
    }

    fun addManualClipboardItem(text: String, label: String) {
        if (text.isBlank()) return
        copyTextToClipboard(text, label.ifBlank { "Saved Snippet" })
    }

    fun toggleClipboardPin(item: ClipboardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleClipboardPin(item)
        }
    }

    fun deleteClipboardItem(item: ClipboardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteClipboardItem(item.id)
        }
    }

    fun clearUnpinnedClipboard() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearUnpinnedClipboard()
        }
        _statusBanner.value = "Cleared unpinned clipboard items"
    }

    fun resetAllUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllCommandUsage()
        }
        _statusBanner.value = "Reset command usage counters"
    }

    // =========================================================================
    // KeyboardActionHandler for In-App Live Terminal Sandbox
    // =========================================================================

    override fun onCharacterKey(char: String) {
        val mods = sandboxModifiers.value
        if (mods.ctrlLatched || mods.altLatched) {
            val combo = buildString {
                if (mods.ctrlLatched) append("^")
                if (mods.altLatched) append("Alt+")
                append(char.uppercase())
            }
            if (mods.ctrlLatched && char.equals("l", ignoreCase = true)) {
                _terminalHistory.value = emptyList()
                _statusBanner.value = "Sent CTRL+L (Clear terminal screen)"
            } else if (mods.ctrlLatched && char.equals("c", ignoreCase = true)) {
                _terminalHistory.update {
                    it + TerminalLogEntry(
                        promptLine = "${_sandboxInputLine.value}$combo",
                        note = "SIGINT (Interrupt signal sent)"
                    )
                }
                _sandboxInputLine.value = ""
            } else {
                _sandboxInputLine.update { it + combo }
            }
            sandboxModifiers.update { it.copy(ctrlLatched = false, altLatched = false) }
            return
        }

        _sandboxInputLine.update { it + char }
        if (mods.shiftState == ShiftState.ONCE) {
            sandboxModifiers.update { it.copy(shiftState = ShiftState.OFF) }
        }
    }

    override fun onShortcutKey(shortcut: TermuxShortcutKey) {
        when (shortcut) {
            TermuxShortcutKey.ESC -> {
                sandboxModifiers.update {
                    it.copy(ctrlLatched = false, altLatched = false, shiftState = ShiftState.OFF)
                }
                _statusBanner.value = "Sent ESC keycode"
            }
            TermuxShortcutKey.TAB -> {
                // Auto-complete top suggestion if query is non-empty!
                val top = sandboxKeyboardState.value.relatedCommands.firstOrNull()
                if (_sandboxInputLine.value.isNotBlank() && top != null) {
                    replaceActiveSandboxQueryWithCommand(top.command)
                } else {
                    _sandboxInputLine.update { "$it    " }
                }
            }
            TermuxShortcutKey.UP -> {
                val history = _terminalHistory.value
                if (history.isNotEmpty()) {
                    val nextIdx = (_commandHistoryCursor.value + 1).coerceAtMost(history.lastIndex)
                    _commandHistoryCursor.value = nextIdx
                    _sandboxInputLine.value = history[history.lastIndex - nextIdx].promptLine
                }
            }
            TermuxShortcutKey.DOWN -> {
                val history = _terminalHistory.value
                if (_commandHistoryCursor.value > 0) {
                    val nextIdx = _commandHistoryCursor.value - 1
                    _commandHistoryCursor.value = nextIdx
                    _sandboxInputLine.value = history[history.lastIndex - nextIdx].promptLine
                } else {
                    _commandHistoryCursor.value = -1
                    _sandboxInputLine.value = ""
                }
            }
            TermuxShortcutKey.LEFT, TermuxShortcutKey.RIGHT -> {
                _statusBanner.value = "Sent Cursor ${shortcut.label} keycode"
            }
            else -> {
                shortcut.insertText?.let { text ->
                    _sandboxInputLine.update { it + text }
                }
            }
        }
    }

    override fun onBackspace() {
        _sandboxInputLine.update { current ->
            if (current.isNotEmpty()) current.dropLast(1) else current
        }
    }

    override fun onEnter() {
        val line = _sandboxInputLine.value.trim()
        if (line.isNotEmpty()) {
            _terminalHistory.update {
                (it + TerminalLogEntry(
                    promptLine = line,
                    note = "Line committed via Enter key (Commands are never executed automatically on tap)"
                )).takeLast(12)
            }
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordCommandTextUsed(line)
            }
        }
        _sandboxInputLine.value = ""
        _commandHistoryCursor.value = -1
    }

    override fun onSpace() {
        _sandboxInputLine.update { "$it " }
    }

    override fun onToggleCtrl() {
        sandboxModifiers.update { it.copy(ctrlLatched = !it.ctrlLatched) }
    }

    override fun onToggleAlt() {
        sandboxModifiers.update { it.copy(altLatched = !it.altLatched) }
    }

    override fun onToggleShift() {
        sandboxModifiers.update {
            val next = when (it.shiftState) {
                ShiftState.OFF -> ShiftState.ONCE
                ShiftState.ONCE -> ShiftState.CAPS_LOCK
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
            it.copy(shiftState = next)
        }
    }

    override fun onToggleSymbolLayer() {
        sandboxModifiers.update { it.copy(isSymbolLayer = !it.isSymbolLayer) }
    }

    override fun onCycleOneHandedMode() {
        sandboxModifiers.update {
            val next = when (it.oneHandedMode) {
                OneHandedMode.FULL -> OneHandedMode.RIGHT
                OneHandedMode.RIGHT -> OneHandedMode.LEFT
                OneHandedMode.LEFT -> OneHandedMode.FULL
            }
            it.copy(oneHandedMode = next)
        }
    }

    override fun onToggleBottomVault() {
        sandboxModifiers.update { it.copy(isBottomVaultExpanded = !it.isBottomVaultExpanded) }
    }

    override fun onSwitchInputMethod() {
        try {
            val imm = getApplication<Application>()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.showInputMethodPicker()
        } catch (_: Exception) {
            _statusBanner.value = "Use Select IME button above to switch system keyboards"
        }
    }

    override fun onSelectCommandSuggestion(command: CommandEntity) {
        replaceActiveSandboxQueryWithCommand(command.command)
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordCommandUsed(command)
        }
    }

    override fun onSelectClipboardItem(item: ClipboardEntity) {
        _sandboxInputLine.update { current ->
            if (current.isEmpty() || current.endsWith(" ")) {
                current + item.text
            } else {
                "$current ${item.text}"
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordClipboardUsed(item)
        }
    }

    override fun onSelectRankedSuggestion(suggestion: RankedSuggestion) {
        replaceActiveSandboxQueryWithCommand(suggestion.insertText)
        viewModelScope.launch(Dispatchers.IO) {
            if (suggestion.isClipboard && suggestion.clipboardEntity != null) {
                repository.recordClipboardUsed(suggestion.clipboardEntity)
            } else if (suggestion.commandEntity != null) {
                repository.recordCommandUsed(suggestion.commandEntity)
            } else {
                repository.recordCommandTextUsed(suggestion.insertText)
            }
        }
    }

    private fun replaceActiveSandboxQueryWithCommand(commandToInsert: String) {
        val current = _sandboxInputLine.value
        val lastAnd = current.lastIndexOf("&&").let { if (it >= 0) it + 2 else -1 }
        val lastPipe = current.lastIndexOf("|").let { if (it >= 0) it + 1 else -1 }
        val lastSemi = current.lastIndexOf(";").let { if (it >= 0) it + 1 else -1 }
        val segmentStartIndex = maxOf(0, lastAnd, lastPipe, lastSemi)

        val prefix = current.substring(0, segmentStartIndex)
        val rawSegment = current.substring(segmentStartIndex)
        val leadingSpaces = rawSegment.takeWhile { it == ' ' }
        _sandboxInputLine.value = prefix + leadingSpaces + commandToInsert
    }
}
