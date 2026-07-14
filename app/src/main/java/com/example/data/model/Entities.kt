package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String
)

@Entity(
    tableName = "files",
    indices = [
        Index(value = ["isCloud", "isSafe", "dateAdded"]),
        Index(value = ["isCloud", "cloudAccountEmail", "dateAdded"]),
        Index(value = ["isDuplicate", "duplicateGroupId", "dateAdded"]),
        Index(value = ["isSafe", "dateAdded"]),
        Index(value = ["isJunk"]),
        Index(value = ["categoryId"]),
        Index(value = ["path"])
    ]
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val size: Long,
    val mimeType: String,
    val path: String,
    val isCloud: Boolean = false,
    val isSafe: Boolean = false,
    val isJunk: Boolean = false,
    val cloudAccountEmail: String? = null,
    val isDuplicate: Boolean = false,
    val duplicateGroupId: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val categoryId: String
)

@Entity(tableName = "secure_state")
data class SecureStateEntity(
    @PrimaryKey val id: String = "secure_settings",
    val pin: String?, // 4-digit PIN stored securely
    val isLocked: Boolean = true,
    val hint: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sender: String, // "user" or "model"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "commits")
data class CommitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commitHash: String,
    val message: String,
    val author: String = "user@aistudio.com",
    val timestamp: Long = System.currentTimeMillis(),
    val filesAffected: String,
    val isPushed: Boolean = false
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val actionType: String, // "ADD" or "DELETE"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "file_embeddings")
data class EmbeddingEntity(
    @PrimaryKey val fileId: Long,
    val embeddingJson: String, // Stringified FloatArray
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vector_metadata")
data class VectorMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

