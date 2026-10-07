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
import com.example.data.SmartSuggestionEngine
import com.example.data.TermuxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TermuxHubViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = TermuxRepository(db.commandDao(), db.clipboardDao())
    private val clipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

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

    val recentFifteenClipboard: StateFlow<List<ClipboardEntity>> = repository.recentFifteenClipboard
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

    fun recordCommandUsageAndCopy(command: CommandEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordCommandUsed(command)
        }
        copyTextToClipboard(command.command, command.name)
    }

    fun recordClipboardUsageAndCopy(item: ClipboardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordClipboardUsed(item)
        }
        copyTextToClipboard(item.text, item.label.ifBlank { "Clipboard Item" })
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
}
