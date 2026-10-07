package com.example.data

import kotlinx.coroutines.flow.Flow

class TermuxRepository(
    private val commandDao: CommandDao,
    private val clipboardDao: ClipboardDao
) {
    val allCommands: Flow<List<CommandEntity>> = commandDao.observeAllCommands()
    val mostUsedCommands: Flow<List<CommandEntity>> = commandDao.observeMostUsedCommands(12)

    val allClipboardItems: Flow<List<ClipboardEntity>> = clipboardDao.observeAllClipboardItems()
    val recentFifteenClipboard: Flow<List<ClipboardEntity>> = clipboardDao.observeRecentFifteen()
    val mostUsedClipboard: Flow<List<ClipboardEntity>> = clipboardDao.observeMostUsedClipboard(10)

    suspend fun ensureSeeded(db: AppDatabase) {
        AppDatabase.seedDatabaseIfEmpty(db)
    }

    suspend fun recordCommandUsed(command: CommandEntity) {
        if (command.id != 0L) {
            commandDao.incrementUsageById(command.id)
        } else {
            commandDao.incrementUsageByText(command.command)
        }
    }

    suspend fun recordCommandTextUsed(commandText: String) {
        commandDao.incrementUsageByText(commandText.trim())
    }

    suspend fun saveCustomCommand(
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
    ): Long {
        val entity = CommandEntity(
            id = id,
            command = command.trim(),
            name = name.trim().ifEmpty { command.trim() },
            description = description.trim().ifEmpty { "Custom Termux command" },
            usage = usage.trim().ifEmpty { command.trim() },
            examples = examples.trim().ifEmpty { command.trim() },
            category = category.ifBlank { CommandCategory.CUSTOM.displayName },
            relatedCommands = relatedCommands.trim(),
            tags = tags.trim(),
            usageCount = usageCount,
            lastUsedTimestamp = System.currentTimeMillis(),
            isCustom = true
        )
        return commandDao.upsertCommand(entity)
    }

    suspend fun deleteCommand(command: CommandEntity) {
        commandDao.deleteCommand(command)
    }

    suspend fun resetAllCommandUsage() {
        commandDao.resetAllUsageCounts()
    }

    suspend fun addOrTouchClipboardText(rawText: String, label: String = "") {
        val clean = rawText.trim()
        if (clean.isEmpty()) return
        val existing = clipboardDao.findByText(clean)
        val now = System.currentTimeMillis()
        if (existing != null) {
            clipboardDao.updateTimestamp(existing.id, now)
        } else {
            clipboardDao.insertClipboardItem(
                ClipboardEntity(
                    text = clean,
                    label = label,
                    copiedTimestamp = now,
                    usageCount = 0,
                    lastUsedTimestamp = 0L,
                    isPinned = false
                )
            )
        }
    }

    suspend fun recordClipboardUsed(item: ClipboardEntity) {
        clipboardDao.incrementUsageById(item.id)
    }

    suspend fun toggleClipboardPin(item: ClipboardEntity) {
        clipboardDao.setPinned(item.id, !item.isPinned)
    }

    suspend fun deleteClipboardItem(id: Long) {
        clipboardDao.deleteById(id)
    }

    suspend fun clearUnpinnedClipboard() {
        clipboardDao.clearUnpinnedHistory()
    }
}
