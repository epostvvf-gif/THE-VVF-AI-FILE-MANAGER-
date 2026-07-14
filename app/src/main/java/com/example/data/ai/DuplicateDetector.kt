package com.example.data.ai

import com.example.data.local.EmbeddingDao
import com.example.data.local.FileDao
import com.example.data.model.FileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.math.abs

data class DuplicateResult(
    val exactDuplicates: List<List<FileEntity>>,
    val nearDuplicates: List<List<FileEntity>>,
    val semanticDuplicates: List<List<FileEntity>>
)

class DuplicateDetector(
    private val fileDao: FileDao,
    private val embeddingDao: EmbeddingDao,
    private val semanticEngine: SemanticEngine
) {

    /**
     * Scans and detects duplicate files on three distinct levels.
     */
    suspend fun detectDuplicates(onProgress: (Float) -> Unit = {}): DuplicateResult = withContext(Dispatchers.IO) {
        val files = fileDao.getLocalFiles()
        if (files.isEmpty()) {
            onProgress(1.0f)
            return@withContext DuplicateResult(emptyList(), emptyList(), emptyList())
        }

        onProgress(0.05f)
        // --- 1. Exact Duplicate (SHA-256 Content or Metadata Hash) ---
        val exactGroups = mutableMapOf<String, MutableList<FileEntity>>()
        for (i in files.indices) {
            val file = files[i]
            val sha = computeSha256(file)
            exactGroups.getOrPut(sha) { mutableListOf() }.add(file)
            onProgress(0.05f + (0.25f * (i.toFloat() / files.size)))
        }
        val exactList = exactGroups.values.filter { it.size > 1 }.toList()

        onProgress(0.3f)
        // --- 2. Near Duplicate (Perceptual Hash based on name templates and size profiles) ---
        val nearGroups = mutableMapOf<Long, MutableList<FileEntity>>()
        for (i in files.indices) {
            val file = files[i]
            if (file.mimeType.startsWith("image/") || file.mimeType.startsWith("audio/")) {
                val pHash = computePerceptualHash(file)
                nearGroups.getOrPut(pHash) { mutableListOf() }.add(file)
            }
            onProgress(0.3f + (0.3f * (i.toFloat() / files.size)))
        }
        val nearList = nearGroups.values.filter { it.size > 1 }.toList()

        onProgress(0.6f)
        // --- 3. Semantic Duplicate (Embedding-based high cosine similarity threshold) ---
        val semanticGroups = mutableListOf<MutableList<FileEntity>>()
        val embeddings = embeddingDao.getAllEmbeddings().associateBy { it.fileId }
        val visited = mutableSetOf<Long>()

        for (i in files.indices) {
            val fileA = files[i]
            if (visited.contains(fileA.id)) continue

            val embA = embeddings[fileA.id] ?: continue
            val vecA = embA.embeddingJson.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
            if (vecA.isEmpty()) continue

            val currentGroup = mutableListOf(fileA)

            for (j in (i + 1) until files.size) {
                val fileB = files[j]
                if (visited.contains(fileB.id)) continue

                val embB = embeddings[fileB.id] ?: continue
                val vecB = embB.embeddingJson.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
                if (vecB.isEmpty()) continue

                val similarity = semanticEngine.calculateCosineSimilarity(vecA, vecB)
                // Semantic duplicates have highly similar embeddings (>= 0.88 similarity threshold)
                if (similarity >= 0.88f) {
                    currentGroup.add(fileB)
                    visited.add(fileB.id)
                }
            }

            if (currentGroup.size > 1) {
                semanticGroups.add(currentGroup)
                visited.add(fileA.id)
            }
            onProgress(0.6f + (0.4f * (i.toFloat() / files.size)))
        }

        onProgress(1.0f)
        DuplicateResult(
            exactDuplicates = exactList,
            nearDuplicates = nearList,
            semanticDuplicates = semanticGroups
        )
    }

    private fun computeSha256(fileEntity: FileEntity): String {
        return try {
            val file = File(fileEntity.path)
            if (file.exists() && file.isFile) {
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead = input.read(buffer)
                    while (bytesRead != -1) {
                        digest.update(buffer, 0, bytesRead)
                        bytesRead = input.read(buffer)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            } else {
                val digest = MessageDigest.getInstance("SHA-256")
                val inputString = "${fileEntity.name}_${fileEntity.size}_${fileEntity.categoryId}"
                digest.update(inputString.toByteArray())
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            fileEntity.name.hashCode().toString()
        }
    }

    private fun computePerceptualHash(fileEntity: FileEntity): Long {
        val cleanName = fileEntity.name.lowercase()
            .replace("_copy", "")
            .replace("copy", "")
            .replace(Regex("[()0-9\\-_]"), "")
            .substringBefore(".")
        
        val sizeGroup = fileEntity.size / 100000
        val hash = (cleanName.hashCode().toLong() shl 16) or (sizeGroup and 0xFFFF)
        return abs(hash)
    }
}
