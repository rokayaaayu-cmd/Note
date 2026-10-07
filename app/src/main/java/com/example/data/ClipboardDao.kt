package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY copiedTimestamp DESC")
    fun observeAllClipboardItems(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items ORDER BY copiedTimestamp DESC LIMIT 15")
    fun observeRecentFifteen(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE usageCount > 0 ORDER BY usageCount DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun observeMostUsedClipboard(limit: Int = 8): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE text = :text LIMIT 1")
    suspend fun findByText(text: String): ClipboardEntity?

    @Query("SELECT COUNT(*) FROM clipboard_items")
    suspend fun getClipboardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboardItem(item: ClipboardEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClipboardItemsIgnore(items: List<ClipboardEntity>)

    @Query("UPDATE clipboard_items SET copiedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE clipboard_items SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementUsageById(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE clipboard_items SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearUnpinnedHistory()
}
