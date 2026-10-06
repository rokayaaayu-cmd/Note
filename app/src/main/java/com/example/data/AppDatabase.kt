package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [CommandEntity::class, ClipboardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "termux_smart_keyboard.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                dbScope.launch {
                    seedDatabaseIfEmpty(instance)
                }
                instance
            }
        }

        suspend fun seedDatabaseIfEmpty(db: AppDatabase) {
            val commandDao = db.commandDao()
            if (commandDao.getCommandCount() == 0) {
                commandDao.insertCommandsIgnore(TermuxCommandSeedData.getInitialCommands())
            }
            val clipboardDao = db.clipboardDao()
            if (clipboardDao.getClipboardCount() == 0) {
                clipboardDao.insertClipboardItemsIgnore(TermuxCommandSeedData.getInitialClipboardItems())
            }
        }
    }
}
