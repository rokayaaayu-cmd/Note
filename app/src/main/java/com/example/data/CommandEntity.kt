package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CommandCategory(val displayName: String) {
    ALL("All"),
    TERMUX("Termux"),
    LINUX("Linux"),
    FILES("Files"),
    GIT("Git"),
    PYTHON("Python"),
    NODE_NPM("Node/NPM"),
    ANDROID("Android"),
    NETWORKING("Networking"),
    SSH("SSH"),
    ADB("ADB"),
    OPENCODE("OpenCode"),
    CLAUDE("Claude"),
    KIMI("Kimi"),
    CUSTOM("Custom");

    companion object {
        val selectableCategories: List<CommandCategory> = entries.filter { it != ALL }
    }
}

@Entity(
    tableName = "commands",
    indices = [
        Index(value = ["command"], unique = true),
        Index(value = ["category"]),
        Index(value = ["usageCount"])
    ]
)
data class CommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val name: String,
    val description: String,
    val usage: String,
    val examples: String,
    val category: String,
    val relatedCommands: String = "",
    val tags: String = "",
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val isCustom: Boolean = false
) {
    fun relatedList(): List<String> =
        relatedCommands.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun exampleList(): List<String> =
        examples.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun tagList(): List<String> =
        tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
