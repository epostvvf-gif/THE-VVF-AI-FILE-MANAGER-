package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.CommitDao
import com.example.data.local.FileDao
import com.example.data.local.SecureStateDao
import com.example.data.model.CategoryEntity
import com.example.data.model.CommitEntity
import com.example.data.model.FileEntity
import com.example.data.model.SecureStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FileRepository(
    private val context: android.content.Context,
    private val fileDao: FileDao,
    private val categoryDao: CategoryDao,
    private val secureStateDao: SecureStateDao,
    private val commitDao: CommitDao,
    private val syncQueueDao: com.example.data.local.SyncQueueDao
) {
    val localFiles: Flow<List<FileEntity>> = fileDao.getLocalFilesFlow()
    val safeFiles: Flow<List<FileEntity>> = fileDao.getSafeFilesFlow().map { files ->
        files.map { file ->
            file.copy(
                name = com.example.util.CryptoHelper.decrypt(file.name),
                path = com.example.util.CryptoHelper.decrypt(file.path)
            )
        }
    }
    val junkFiles: Flow<List<FileEntity>> = fileDao.getJunkFilesFlow()
    val duplicates: Flow<List<FileEntity>> = fileDao.getDuplicatesFlow()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getCategoriesFlow()
    val secureState: Flow<SecureStateEntity?> = secureStateDao.getSecureStateFlow()
    val commits: Flow<List<CommitEntity>> = commitDao.getAllCommitsFlow()
    val syncQueue: Flow<List<com.example.data.model.SyncQueueEntity>> = syncQueueDao.getQueueFlow()

    suspend fun addToSyncQueue(fileId: Long, actionType: String) = withContext(Dispatchers.IO) {
        syncQueueDao.insertQueueItem(com.example.data.model.SyncQueueEntity(fileId = fileId, actionType = actionType))
    }

    suspend fun removeFromSyncQueue(queueId: Long) = withContext(Dispatchers.IO) {
        syncQueueDao.deleteQueueItem(queueId)
    }

    suspend fun clearSyncQueue() = withContext(Dispatchers.IO) {
        syncQueueDao.clearQueue()
    }

    fun getCloudFilesFlow(email: String): Flow<List<FileEntity>> {
        return fileDao.getCloudFilesFlow(email)
    }

    suspend fun getSecureState(): SecureStateEntity? = withContext(Dispatchers.IO) {
        secureStateDao.getSecureState()
    }

    suspend fun setSecureState(state: SecureStateEntity) = withContext(Dispatchers.IO) {
        secureStateDao.insertSecureState(state)
    }

    private fun generateShortHash(): String {
        val allowedChars = ('a'..'f') + ('0'..'9')
        return (1..7)
            .map { allowedChars.random() }
            .joinToString("")
    }

    suspend fun logCommit(message: String, affectedFiles: List<String>) = withContext(Dispatchers.IO) {
        val commit = CommitEntity(
            commitHash = generateShortHash(),
            message = message,
            filesAffected = affectedFiles.joinToString(", ").takeIf { it.isNotEmpty() } ?: "None"
        )
        commitDao.insertCommit(commit)
    }

    suspend fun clearAllCommits() = withContext(Dispatchers.IO) {
        commitDao.clearAllCommits()
    }

    suspend fun markCommitsAsPushed(ids: List<Long>) = withContext(Dispatchers.IO) {
        commitDao.markAsPushed(ids)
    }

    suspend fun clearJunkFiles() = withContext(Dispatchers.IO) {
        val junkFileNames = fileDao.getJunkFiles().map { it.name }
        fileDao.clearJunkFiles()
        if (junkFileNames.isNotEmpty()) {
            logCommit("Cleaned junk files to free storage", junkFileNames)
        }
    }

    suspend fun deleteFiles(ids: List<Long>) = withContext(Dispatchers.IO) {
        val fileNames = fileDao.getLocalFiles().filter { ids.contains(it.id) }.map { it.name }
        val cloudFileNames = (fileDao.getCloudFiles("epostvvf@gmail.com") + fileDao.getCloudFiles("work.drive@gmail.com"))
            .filter { ids.contains(it.id) }.map { it.name }
        val allNames = fileNames + cloudFileNames
        fileDao.deleteFilesByIds(ids)
        if (allNames.isNotEmpty()) {
            logCommit("Deleted ${allNames.size} files", allNames)
        }
    }

    suspend fun moveFilesToSafe(ids: List<Long>) = withContext(Dispatchers.IO) {
        val files = fileDao.getLocalFiles().filter { ids.contains(it.id) }
        val vaultDir = java.io.File(context.filesDir, "secure_vault")
        if (!vaultDir.exists()) vaultDir.mkdirs()

        for (file in files) {
            // Read public file or construct mock bytes for simulated initial files
            val publicFile = java.io.File(file.path)
            val originalBytes = if (publicFile.exists() && publicFile.isFile) {
                publicFile.readBytes()
            } else {
                "Simulated zero-trust physical file content for ${file.name}".toByteArray(Charsets.UTF_8)
            }

            // Encrypt content bytes
            val encryptedBytes = com.example.util.CryptoHelper.encryptBytes(originalBytes)

            // Write encrypted file to secure private sandbox vault directory
            val secureFile = java.io.File(vaultDir, "enc_${file.id}.bin")
            secureFile.writeBytes(encryptedBytes)

            // Delete original file from public storage if it physically existed
            if (publicFile.exists()) {
                publicFile.delete()
            }

            // Update database record with KeyStore-encrypted file metadata
            val encryptedName = com.example.util.CryptoHelper.encrypt(file.name)
            val encryptedPath = com.example.util.CryptoHelper.encrypt(file.path)
            fileDao.updateFile(file.copy(
                name = encryptedName,
                path = encryptedPath,
                isSafe = true
            ))
        }
        if (files.isNotEmpty()) {
            logCommit("Moved ${files.size} files to Secure Vault (AES-256 KeyStore Encrypted)", files.map { it.name })
        }
    }

    suspend fun restoreFilesFromSafe(ids: List<Long>) = withContext(Dispatchers.IO) {
        val files = fileDao.getSafeFiles().filter { ids.contains(it.id) }
        val vaultDir = java.io.File(context.filesDir, "secure_vault")

        for (file in files) {
            val decryptedName = com.example.util.CryptoHelper.decrypt(file.name)
            val decryptedPath = com.example.util.CryptoHelper.decrypt(file.path)

            // Read secure file, decrypt bytes, and restore to public location
            val secureFile = java.io.File(vaultDir, "enc_${file.id}.bin")
            if (secureFile.exists()) {
                val encryptedBytes = secureFile.readBytes()
                val originalBytes = com.example.util.CryptoHelper.decryptBytes(encryptedBytes)
                
                try {
                    val publicFile = java.io.File(decryptedPath)
                    publicFile.parentFile?.mkdirs()
                    publicFile.writeBytes(originalBytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Delete secure file from private sandbox
                secureFile.delete()
            }

            fileDao.updateFile(file.copy(
                name = decryptedName,
                path = decryptedPath,
                isSafe = false
            ))
        }
        if (files.isNotEmpty()) {
            logCommit("Restored ${files.size} files from Secure Vault", files.map { com.example.util.CryptoHelper.decrypt(it.name) })
        }
    }

    suspend fun addFile(file: FileEntity): Long = withContext(Dispatchers.IO) {
        val id = fileDao.insertFile(file)
        logCommit("Added file: ${file.name}", listOf(file.name))
        id
    }

    suspend fun populateInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingCategories = categoryDao.getCategories()
        if (existingCategories.isEmpty()) {
            val initialCategories = listOf(
                CategoryEntity("IMAGES", "Images", "image"),
                CategoryEntity("VIDEOS", "Videos", "video"),
                CategoryEntity("DOCUMENTS", "Documents", "document"),
                CategoryEntity("MUSIC", "Music", "music"),
                CategoryEntity("APKS", "APKs", "apk"),
                CategoryEntity("JUNK", "Junk", "delete")
            )
            categoryDao.insertCategories(initialCategories)
        }

        val existingLocalFiles = fileDao.getLocalFiles()
        if (existingLocalFiles.isEmpty()) {
            val initialFiles = listOf(
                // Local files
                FileEntity(
                    name = "Nature_Camping_Photo.jpg",
                    size = 1240000, // 1.2 MB
                    mimeType = "image/jpeg",
                    path = "/storage/emulated/0/DCIM/Nature_Camping_Photo.jpg",
                    categoryId = "IMAGES",
                    isCloud = false
                ),
                FileEntity(
                    name = "Selfie_With_Team.png",
                    size = 2450000, // 2.4 MB
                    mimeType = "image/png",
                    path = "/storage/emulated/0/DCIM/Selfie_With_Team.png",
                    categoryId = "IMAGES",
                    isCloud = false
                ),
                FileEntity(
                    name = "Duplicate_Travel_Pic.jpg",
                    size = 1850000, // 1.8 MB
                    mimeType = "image/jpeg",
                    path = "/storage/emulated/0/Pictures/Duplicate_Travel_Pic.jpg",
                    categoryId = "IMAGES",
                    isCloud = false,
                    isDuplicate = true,
                    duplicateGroupId = "travel_pic_group"
                ),
                FileEntity(
                    name = "Duplicate_Travel_Pic_Copy.jpg",
                    size = 1850000, // 1.8 MB
                    mimeType = "image/jpeg",
                    path = "/storage/emulated/0/DCIM/Duplicate_Travel_Pic_Copy.jpg",
                    categoryId = "IMAGES",
                    isCloud = false,
                    isDuplicate = true,
                    duplicateGroupId = "travel_pic_group"
                ),
                FileEntity(
                    name = "Annual_Budget_Report_2026.xlsx",
                    size = 380000, // 380 KB
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    path = "/storage/emulated/0/Documents/Annual_Budget_Report_2026.xlsx",
                    categoryId = "DOCUMENTS",
                    isCloud = false
                ),
                FileEntity(
                    name = "Resume_Software_Engineer.pdf",
                    size = 450000, // 450 KB
                    mimeType = "application/pdf",
                    path = "/storage/emulated/0/Download/Resume_Software_Engineer.pdf",
                    categoryId = "DOCUMENTS",
                    isCloud = false
                ),
                FileEntity(
                    name = "Smart_App_v1.0.apk",
                    size = 15800000, // 15.8 MB
                    mimeType = "application/vnd.android.package-archive",
                    path = "/storage/emulated/0/Download/Smart_App_v1.0.apk",
                    categoryId = "APKS",
                    isCloud = false
                ),
                FileEntity(
                    name = "Acoustic_Guitar_Solo.mp3",
                    size = 8900000, // 8.9 MB
                    mimeType = "audio/mpeg",
                    path = "/storage/emulated/0/Music/Acoustic_Guitar_Solo.mp3",
                    categoryId = "MUSIC",
                    isCloud = false
                ),
                FileEntity(
                    name = "Weekend_Vlog_Draft.mp4",
                    size = 124000000, // 124 MB
                    mimeType = "video/mp4",
                    path = "/storage/emulated/0/Movies/Weekend_Vlog_Draft.mp4",
                    categoryId = "VIDEOS",
                    isCloud = false
                ),
                // Junk Files
                FileEntity(
                    name = "system_temp_cache_09.tmp",
                    size = 28400000, // 28.4 MB
                    mimeType = "application/octet-stream",
                    path = "/storage/emulated/0/Android/data/com.system.cache/system_temp_cache_09.tmp",
                    categoryId = "JUNK",
                    isCloud = false,
                    isJunk = true
                ),
                FileEntity(
                    name = "old_app_telemetry.log",
                    size = 14200000, // 14.2 MB
                    mimeType = "text/plain",
                    path = "/storage/emulated/0/Android/data/com.telemetry/old_app_telemetry.log",
                    categoryId = "JUNK",
                    isCloud = false,
                    isJunk = true
                ),
                FileEntity(
                    name = "unused_diagnostic_report.dmp",
                    size = 54100000, // 54.1 MB
                    mimeType = "application/octet-stream",
                    path = "/storage/emulated/0/Download/unused_diagnostic_report.dmp",
                    categoryId = "JUNK",
                    isCloud = false,
                    isJunk = true
                ),

                // Cloud Simulation Files (Account: epostvvf@gmail.com)
                FileEntity(
                    name = "Cloud_Presentation_Slides.pptx",
                    size = 12400000, // 12.4 MB
                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    path = "Google Drive/My Drive/Cloud_Presentation_Slides.pptx",
                    categoryId = "DOCUMENTS",
                    isCloud = true,
                    cloudAccountEmail = "epostvvf@gmail.com"
                ),
                FileEntity(
                    name = "Cloud_Vacation_Video.mp4",
                    size = 245000000, // 245 MB
                    mimeType = "video/mp4",
                    path = "Google Drive/My Drive/Cloud_Vacation_Video.mp4",
                    categoryId = "VIDEOS",
                    isCloud = true,
                    cloudAccountEmail = "epostvvf@gmail.com"
                ),
                FileEntity(
                    name = "Cloud_Shared_Album_Pic.png",
                    size = 4500000, // 4.5 MB
                    mimeType = "image/png",
                    path = "Google Drive/Shared/Cloud_Shared_Album_Pic.png",
                    categoryId = "IMAGES",
                    isCloud = true,
                    cloudAccountEmail = "epostvvf@gmail.com"
                ),

                // Cloud Simulation Files (Account: work.drive@gmail.com)
                FileEntity(
                    name = "Q3_Strategic_Goals.docx",
                    size = 1400000, // 1.4 MB
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    path = "Google Drive/My Drive/Work/Q3_Strategic_Goals.docx",
                    categoryId = "DOCUMENTS",
                    isCloud = true,
                    cloudAccountEmail = "work.drive@gmail.com"
                ),
                FileEntity(
                    name = "Product_Mockup_V4.jpg",
                    size = 3200000, // 3.2 MB
                    mimeType = "image/jpeg",
                    path = "Google Drive/My Drive/Work/Product_Mockup_V4.jpg",
                    categoryId = "IMAGES",
                    isCloud = true,
                    cloudAccountEmail = "work.drive@gmail.com"
                )
            )
            fileDao.insertFiles(initialFiles)
        }

        // Initialize empty secure state if not created
        val existingSecureState = secureStateDao.getSecureState()
        if (existingSecureState == null) {
            secureStateDao.insertSecureState(SecureStateEntity(pin = null, isLocked = true, hint = null))
        }

        // Seed initial commit history
        val existingCommits = commitDao.getAllCommitsFlow().first()
        if (existingCommits.isEmpty()) {
            val initialCommits = listOf(
                CommitEntity(
                    commitHash = "8f3b2a1",
                    message = "Initial repository layout and media indexing setup",
                    author = "epostvvf@gmail.com",
                    timestamp = System.currentTimeMillis() - 86400000 * 3, // 3 days ago
                    filesAffected = "Nature_Camping_Photo.jpg, Selfie_With_Team.png, Annual_Budget_Report_2026.xlsx",
                    isPushed = true
                ),
                CommitEntity(
                    commitHash = "2d9c4e5",
                    message = "Configured Secure Vault folder and verified cryptographic salt",
                    author = "epostvvf@gmail.com",
                    timestamp = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                    filesAffected = "Resume_Software_Engineer.pdf, Smart_App_v1.0.apk",
                    isPushed = true
                ),
                CommitEntity(
                    commitHash = "5a1f7d9",
                    message = "Synchronized secondary cloud account work.drive@gmail.com",
                    author = "epostvvf@gmail.com",
                    timestamp = System.currentTimeMillis() - 86400000 * 1, // 1 day ago
                    filesAffected = "Q3_Strategic_Goals.docx, Product_Mockup_V4.jpg",
                    isPushed = true
                )
            )
            for (commit in initialCommits) {
                commitDao.insertCommit(commit)
            }
        }
    }
}
