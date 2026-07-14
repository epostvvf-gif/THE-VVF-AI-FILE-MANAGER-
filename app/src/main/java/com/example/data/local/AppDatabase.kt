package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CommitEntity
import com.example.data.model.FileEntity
import com.example.data.model.SecureStateEntity
import com.example.data.model.SyncQueueEntity

@Database(
    entities = [
        CategoryEntity::class,
        FileEntity::class,
        SecureStateEntity::class,
        ChatMessageEntity::class,
        CommitEntity::class,
        SyncQueueEntity::class,
        com.example.data.model.EmbeddingEntity::class,
        com.example.data.model.VectorMetadataEntity::class,
        com.example.data.model.SearchHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun secureStateDao(): SecureStateDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun commitDao(): CommitDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun vectorMetadataDao(): VectorMetadataDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_file_manager_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
