package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_items",
    indices = [
        Index(value = ["text"], unique = true),
        Index(value = ["copiedTimestamp"]),
        Index(value = ["usageCount"])
    ]
)
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val label: String = "",
    val copiedTimestamp: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val isPinned: Boolean = false
)
