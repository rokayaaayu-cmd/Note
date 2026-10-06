package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.ClipboardEntity
import com.example.data.CommandEntity
import com.example.data.RankedSuggestion
import com.example.data.SmartSuggestionEngine
import com.example.data.TermuxRepository
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TermuxInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    KeyboardActionHandler {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var repository: TermuxRepository
    private var clipboardManager: ClipboardManager? = null

    // Tracks the current line for both standard EditTexts and raw terminal views (InputType.TYPE_NULL in Termux)
    private var shadowLineBuffer: String = ""
    private val currentLineFlow = MutableStateFlow("")
    private val modifiersFlow = MutableStateFlow(ModifierConfig())

    private data class ModifierConfig(
        val ctrlLatched: Boolean = false,
        val altLatched: Boolean = false,
        val shiftState: ShiftState = ShiftState.OFF,
        val isSymbolLayer: Boolean = false,
        val oneHandedMode: OneHandedMode = OneHandedMode.FULL,
        val isBottomVaultExpanded: Boolean = true
    )

    private lateinit var keyboardUiState: StateFlow<TermuxKeyboardUiState>

    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        syncSystemClipboardToLocalDb()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val db = AppDatabase.getInstance(applicationContext)
        repository = TermuxRepository(db.commandDao(), db.clipboardDao())

        serviceScope.launch(Dispatchers.IO) {
            repository.ensureSeeded(db)
        }

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
        syncSystemClipboardToLocalDb()

        keyboardUiState = combine(
            currentLineFlow,
            repository.allCommands,
            repository.allClipboardItems,
            repository.recentThreeClipboard,
            modifiersFlow
        ) { line, allCommands, allClips, recent3, mods ->
            val activeQuery = SmartSuggestionEngine.extractActiveQuery(line)
            val relatedCmds = SmartSuggestionEngine.rankCommandSuggestions(line, allCommands, limit = 10)
            val mostUsedAndRelated = SmartSuggestionEngine.rankMostUsedAndRelated(
                rawInput = line,
                commands = allCommands,
                clipboardItems = allClips,
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
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = TermuxKeyboardUiState()
        )
    }

    // Never enter opaque fullscreen ExtractEditText mode so Termux terminal stays visible
    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@TermuxInputMethodService)
            setViewTreeViewModelStoreOwner(this@TermuxInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@TermuxInputMethodService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                MyApplicationTheme {
                    val state by keyboardUiState.collectAsState()
                    TermuxKeyboardSurface(
                        state = state,
                        handler = this@TermuxInputMethodService,
                        onOpenFullAppClick = {
                            val intent = Intent(this@TermuxInputMethodService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        if (!restarting) {
            shadowLineBuffer = ""
        }
        syncSystemClipboardToLocalDb()
        refreshInputLineFromConnection()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        refreshInputLineFromConnection()
    }

    override fun onDestroy() {
        clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun syncSystemClipboardToLocalDb() {
        val clip = clipboardManager?.primaryClip ?: return
        if (clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                serviceScope.launch(Dispatchers.IO) {
                    repository.addOrTouchClipboardText(text, label = "System Clipboard")
                }
            }
        }
    }

    private fun refreshInputLineFromConnection() {
        val ic = currentInputConnection
        val beforeCursor = ic?.getTextBeforeCursor(160, 0)?.toString().orEmpty()
        if (beforeCursor.isNotEmpty()) {
            val line = beforeCursor.substringAfterLast('\n')
            shadowLineBuffer = line
            currentLineFlow.value = line
        } else {
            currentLineFlow.value = shadowLineBuffer
        }
    }

    // =========================================================================
    // KeyboardActionHandler Implementations
    // =========================================================================

    override fun onCharacterKey(char: String) {
        val ic = currentInputConnection ?: return
        val mods = modifiersFlow.value

        if (mods.ctrlLatched || mods.altLatched) {
            sendModifiedKeyEvent(ic, char, mods.ctrlLatched, mods.altLatched)
            if (mods.ctrlLatched && (char.equals("c", true) || char.equals("u", true))) {
                shadowLineBuffer = ""
            }
            modifiersFlow.update { it.copy(ctrlLatched = false, altLatched = false) }
            refreshInputLineFromConnection()
            return
        }

        ic.commitText(char, 1)
        shadowLineBuffer += char
        if (mods.shiftState == ShiftState.ONCE) {
            modifiersFlow.update { it.copy(shiftState = ShiftState.OFF) }
        }
        refreshInputLineFromConnection()
    }

    override fun onShortcutKey(shortcut: TermuxShortcutKey) {
        val ic = currentInputConnection ?: return
        when (shortcut) {
            TermuxShortcutKey.ESC -> sendKeyCode(ic, KeyEvent.KEYCODE_ESCAPE)
            TermuxShortcutKey.TAB -> sendKeyCode(ic, KeyEvent.KEYCODE_TAB)
            TermuxShortcutKey.UP -> sendKeyCode(ic, KeyEvent.KEYCODE_DPAD_UP)
            TermuxShortcutKey.DOWN -> sendKeyCode(ic, KeyEvent.KEYCODE_DPAD_DOWN)
            TermuxShortcutKey.LEFT -> sendKeyCode(ic, KeyEvent.KEYCODE_DPAD_LEFT)
            TermuxShortcutKey.RIGHT -> sendKeyCode(ic, KeyEvent.KEYCODE_DPAD_RIGHT)
            else -> {
                shortcut.insertText?.let { text ->
                    ic.commitText(text, 1)
                    shadowLineBuffer += text
                }
            }
        }
        refreshInputLineFromConnection()
    }

    override fun onBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            val before = ic.getTextBeforeCursor(1, 0)
            if (!before.isNullOrEmpty()) {
                ic.deleteSurroundingText(1, 0)
            } else {
                // Fallback for raw terminal mode (InputType.TYPE_NULL in Termux)
                sendKeyCode(ic, KeyEvent.KEYCODE_DEL)
            }
        }
        if (shadowLineBuffer.isNotEmpty()) {
            shadowLineBuffer = shadowLineBuffer.dropLast(1)
        }
        refreshInputLineFromConnection()
    }

    override fun onEnter() {
        val ic = currentInputConnection ?: return
        val lineToRecord = currentLineFlow.value.trim()
        if (lineToRecord.isNotEmpty()) {
            serviceScope.launch(Dispatchers.IO) {
                repository.recordCommandTextUsed(lineToRecord)
            }
        }
        sendKeyCode(ic, KeyEvent.KEYCODE_ENTER)
        shadowLineBuffer = ""
        currentLineFlow.value = ""
    }

    override fun onSpace() {
        val ic = currentInputConnection ?: return
        ic.commitText(" ", 1)
        shadowLineBuffer += " "
        refreshInputLineFromConnection()
    }

    override fun onToggleCtrl() {
        modifiersFlow.update { it.copy(ctrlLatched = !it.ctrlLatched) }
    }

    override fun onToggleAlt() {
        modifiersFlow.update { it.copy(altLatched = !it.altLatched) }
    }

    override fun onToggleShift() {
        modifiersFlow.update {
            val next = when (it.shiftState) {
                ShiftState.OFF -> ShiftState.ONCE
                ShiftState.ONCE -> ShiftState.CAPS_LOCK
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
            it.copy(shiftState = next)
        }
    }

    override fun onToggleSymbolLayer() {
        modifiersFlow.update { it.copy(isSymbolLayer = !it.isSymbolLayer) }
    }

    override fun onCycleOneHandedMode() {
        modifiersFlow.update {
            val next = when (it.oneHandedMode) {
                OneHandedMode.FULL -> OneHandedMode.RIGHT
                OneHandedMode.RIGHT -> OneHandedMode.LEFT
                OneHandedMode.LEFT -> OneHandedMode.FULL
            }
            it.copy(oneHandedMode = next)
        }
    }

    override fun onToggleBottomVault() {
        modifiersFlow.update { it.copy(isBottomVaultExpanded = !it.isBottomVaultExpanded) }
    }

    override fun onSwitchInputMethod() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val switched = switchToNextInputMethod(false)
                if (!switched) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            } else {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
        } catch (_: Exception) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
    }

    override fun onSelectCommandSuggestion(command: CommandEntity) {
        val ic = currentInputConnection ?: return
        replaceActiveQueryWithCommand(ic, command.command)
        serviceScope.launch(Dispatchers.IO) {
            repository.recordCommandUsed(command)
        }
        refreshInputLineFromConnection()
    }

    override fun onSelectClipboardItem(item: ClipboardEntity) {
        val ic = currentInputConnection ?: return
        ic.commitText(item.text, 1)
        shadowLineBuffer += item.text
        serviceScope.launch(Dispatchers.IO) {
            repository.recordClipboardUsed(item)
        }
        refreshInputLineFromConnection()
    }

    override fun onSelectRankedSuggestion(suggestion: RankedSuggestion) {
        val ic = currentInputConnection ?: return
        replaceActiveQueryWithCommand(ic, suggestion.insertText)
        serviceScope.launch(Dispatchers.IO) {
            if (suggestion.isClipboard && suggestion.clipboardEntity != null) {
                repository.recordClipboardUsed(suggestion.clipboardEntity)
            } else if (suggestion.commandEntity != null) {
                repository.recordCommandUsed(suggestion.commandEntity)
            } else {
                repository.recordCommandTextUsed(suggestion.insertText)
            }
        }
        refreshInputLineFromConnection()
    }

    /**
     * Replaces the currently typed partial query (e.g., "install node", "list files", "opencode")
     * on the active command segment with the full command string, without ever executing it automatically.
     * Supports both standard EditText InputConnections and raw terminal InputConnections (Termux).
     */
    private fun replaceActiveQueryWithCommand(ic: InputConnection, commandToInsert: String) {
        val beforeFromIc = ic.getTextBeforeCursor(160, 0)?.toString().orEmpty()
        val hasStandardTextBefore = beforeFromIc.isNotEmpty()
        val lastLine = if (hasStandardTextBefore) {
            beforeFromIc.substringAfterLast('\n')
        } else {
            shadowLineBuffer
        }

        // Find where the current command segment begins (after last &&, |, or ;)
        val lastAnd = lastLine.lastIndexOf("&&").let { if (it >= 0) it + 2 else -1 }
        val lastPipe = lastLine.lastIndexOf("|").let { if (it >= 0) it + 1 else -1 }
        val lastSemi = lastLine.lastIndexOf(";").let { if (it >= 0) it + 1 else -1 }
        val segmentStartIndex = maxOf(0, lastAnd, lastPipe, lastSemi)

        val prefix = lastLine.substring(0, segmentStartIndex)
        val rawSegment = lastLine.substring(segmentStartIndex)
        val leadingSpaces = rawSegment.takeWhile { it == ' ' }
        val charsToDelete = rawSegment.length - leadingSpaces.length

        if (hasStandardTextBefore) {
            ic.beginBatchEdit()
            if (charsToDelete > 0) {
                ic.deleteSurroundingText(charsToDelete, 0)
            }
            ic.commitText(commandToInsert, 1)
            ic.endBatchEdit()
        } else {
            // Raw terminal mode fallback: send KEYCODE_DEL for each typed character in the query, then commit
            for (i in 0 until charsToDelete) {
                sendKeyCode(ic, KeyEvent.KEYCODE_DEL)
            }
            ic.commitText(commandToInsert, 1)
        }

        shadowLineBuffer = prefix + leadingSpaces + commandToInsert
    }

    private fun sendKeyCode(ic: InputConnection, keyCode: Int, metaState: Int = 0) {
        val now = SystemClock.uptimeMillis()
        ic.sendKeyEvent(
            KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0)
        )
        ic.sendKeyEvent(
            KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0)
        )
    }

    private fun sendModifiedKeyEvent(
        ic: InputConnection,
        char: String,
        ctrl: Boolean,
        alt: Boolean
    ) {
        val ch = char.lowercase().firstOrNull() ?: return
        val keyCode = when (ch) {
            in 'a'..'z' -> KeyEvent.KEYCODE_A + (ch - 'a')
            in '0'..'9' -> KeyEvent.KEYCODE_0 + (ch - '0')
            '/' -> KeyEvent.KEYCODE_SLASH
            '-' -> KeyEvent.KEYCODE_MINUS
            else -> null
        }
        var meta = 0
        if (ctrl) meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (alt) meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON

        if (keyCode != null) {
            sendKeyCode(ic, keyCode, meta)
        } else if (ctrl && ch in 'a'..'z') {
            val ctrlChar = (ch.code - 'a'.code + 1).toChar().toString()
            ic.commitText(ctrlChar, 1)
        }
    }
}
