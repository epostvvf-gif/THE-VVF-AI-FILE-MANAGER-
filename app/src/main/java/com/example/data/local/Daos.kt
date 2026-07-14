package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CommitEntity
import com.example.data.model.FileEntity
import com.example.data.model.SecureStateEntity
import com.example.data.model.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files WHERE isCloud = 0 AND isSafe = 0 ORDER BY dateAdded DESC")
    fun getLocalFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isCloud = 0 AND isSafe = 0")
    suspend fun getLocalFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE isCloud = 1 AND cloudAccountEmail = :email ORDER BY dateAdded DESC")
    fun getCloudFilesFlow(email: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isCloud = 1 AND cloudAccountEmail = :email")
    suspend fun getCloudFiles(email: String): List<FileEntity>

    @Query("SELECT * FROM files WHERE isSafe = 1 ORDER BY dateAdded DESC")
    fun getSafeFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isSafe = 1")
    suspend fun getSafeFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE isJunk = 1")
    fun getJunkFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isJunk = 1")
    suspend fun getJunkFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE isDuplicate = 1 ORDER BY duplicateGroupId, dateAdded DESC")
    fun getDuplicatesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isDuplicate = 1")
    suspend fun getDuplicates(): List<FileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id IN (:ids)")
    suspend fun deleteFilesByIds(ids: List<Long>)

    @Query("UPDATE files SET isSafe = :isSafe WHERE id IN (:ids)")
    suspend fun updateSafeStatus(ids: List<Long>, isSafe: Boolean)

    @Query("UPDATE files SET isJunk = 0 WHERE isJunk = 1")
    suspend fun clearJunkFiles()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getCategories(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
}

@Dao
interface SecureStateDao {
    @Query("SELECT * FROM secure_state WHERE id = 'secure_settings' LIMIT 1")
    fun getSecureStateFlow(): Flow<SecureStateEntity?>

    @Query("SELECT * FROM secure_state WHERE id = 'secure_settings' LIMIT 1")
    suspend fun getSecureState(): SecureStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecureState(state: SecureStateEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface CommitDao {
    @Query("SELECT * FROM commits ORDER BY timestamp DESC")
    fun getAllCommitsFlow(): Flow<List<CommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: CommitEntity)

    @Query("DELETE FROM commits")
    suspend fun clearAllCommits()

    @Query("UPDATE commits SET isPushed = 1 WHERE id IN (:ids)")
    suspend fun markAsPushed(ids: List<Long>)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getQueue(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueItem(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}

@Dao
interface EmbeddingDao {
    @Query("SELECT * FROM file_embeddings WHERE fileId = :fileId LIMIT 1")
    suspend fun getEmbeddingByFileId(fileId: Long): com.example.data.model.EmbeddingEntity?

    @Query("SELECT * FROM file_embeddings")
    suspend fun getAllEmbeddings(): List<com.example.data.model.EmbeddingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(embedding: com.example.data.model.EmbeddingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbeddings(embeddings: List<com.example.data.model.EmbeddingEntity>)

    @Query("DELETE FROM file_embeddings WHERE fileId = :fileId")
    suspend fun deleteEmbeddingByFileId(fileId: Long)

    @Query("DELETE FROM file_embeddings WHERE NOT EXISTS (SELECT 1 FROM files WHERE files.id = file_embeddings.fileId)")
    suspend fun deleteOrphanEmbeddings()
}

@Dao
interface VectorMetadataDao {
    @Query("SELECT * FROM vector_metadata WHERE key = :key LIMIT 1")
    suspend fun getMetadata(key: String): com.example.data.model.VectorMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: com.example.data.model.VectorMetadataEntity)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getSearchHistoryFlow(): Flow<List<com.example.data.model.SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: com.example.data.model.SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

