package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands ORDER BY usageCount DESC, lastUsedTimestamp DESC, id ASC")
    fun observeAllCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands ORDER BY usageCount DESC, lastUsedTimestamp DESC, id ASC")
    suspend fun getAllCommandsOnce(): List<CommandEntity>

    @Query("SELECT COUNT(*) FROM commands")
    suspend fun getCommandCount(): Int

    @Query("SELECT * FROM commands WHERE usageCount > 0 ORDER BY usageCount DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun observeMostUsedCommands(limit: Int = 10): Flow<List<CommandEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommandsIgnore(commands: List<CommandEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommand(command: CommandEntity): Long

    @Update
    suspend fun updateCommand(command: CommandEntity)

    @Delete
    suspend fun deleteCommand(command: CommandEntity)

    @Query("DELETE FROM commands WHERE id = :id")
    suspend fun deleteCommandById(id: Long)

    @Query("UPDATE commands SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementUsageById(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE commands SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE command = :commandText")
    suspend fun incrementUsageByText(commandText: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE commands SET usageCount = 0, lastUsedTimestamp = 0")
    suspend fun resetAllUsageCounts()
}
