package com.example.data.ai

import com.example.data.local.EmbeddingDao
import com.example.data.local.FileDao
import com.example.data.model.EmbeddingEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.math.sqrt

class SemanticEngine(
    private val fileDao: FileDao,
    private val embeddingDao: EmbeddingDao,
    private val embeddingService: EmbeddingProvider
) {
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _overlapPercentage = MutableStateFlow(0)
    val overlapPercentage: StateFlow<Int> = _overlapPercentage

    /**
     * Incremental Parallel Scan with custom cancellation support and Database Cache.
     */
    suspend fun runIncrementalScan() = withContext(Dispatchers.IO) {
        if (_isScanning.value) return@withContext
        _isScanning.value = true
        _scanProgress.value = 0f

        try {
            // 1. Retrieve all local and cloud files
            val localFiles = fileDao.getLocalFiles()
            val cloudFiles1 = fileDao.getCloudFiles("epostvvf@gmail.com")
            val cloudFiles2 = fileDao.getCloudFiles("work.drive@gmail.com")
            val allFiles = localFiles + cloudFiles1 + cloudFiles2

            if (allFiles.isEmpty()) {
                _scanProgress.value = 1.0f
                _overlapPercentage.value = 0
                _isScanning.value = false
                return@withContext
            }

            // 2. Clear orphans
            embeddingDao.deleteOrphanEmbeddings()

            // 3. Parallel Incremental Indexing (using database cache)
            val semaphore = Semaphore(4)
            var processedCount = 0

            val jobs = allFiles.map { file ->
                async {
                    if (!isActive) return@async
                    
                    val existing = embeddingDao.getEmbeddingByFileId(file.id)
                    if (existing == null) {
                        semaphore.withPermit {
                            if (!isActive) return@withPermit
                            try {
                                val cleanText = "${file.name} located at ${file.path} size ${file.size} mime ${file.mimeType}"
                                val vector = embeddingService.getEmbedding(cleanText)
                                val vectorJson = vector.joinToString(",")
                                embeddingDao.insertEmbedding(
                                    EmbeddingEntity(
                                        fileId = file.id,
                                        embeddingJson = vectorJson,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    synchronized(this@SemanticEngine) {
                        processedCount++
                        _scanProgress.value = processedCount.toFloat() / allFiles.size
                    }
                }
            }

            jobs.awaitAll()

            // 4. Calculate actual Cross-Cloud Semantic Overlap
            val embeddings = embeddingDao.getAllEmbeddings()
            val embeddingMap = embeddings.associateBy { it.fileId }

            var overlappingCount = 0
            var comparisonPairsCount = 0

            for (i in allFiles.indices) {
                if (!isActive) break
                val fileA = allFiles[i]
                val embAJson = embeddingMap[fileA.id]?.embeddingJson ?: continue
                val vecA = embAJson.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
                if (vecA.isEmpty()) continue

                for (j in (i + 1) until allFiles.size) {
                    if (!isActive) break
                    val fileB = allFiles[j]
                    
                    val isCrossCompare = (fileA.isCloud != fileB.isCloud) || 
                            (fileA.isCloud && fileB.isCloud && fileA.cloudAccountEmail != fileB.cloudAccountEmail)

                    if (!isCrossCompare) continue

                    val embBJson = embeddingMap[fileB.id]?.embeddingJson ?: continue
                    val vecB = embBJson.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
                    if (vecB.isEmpty()) continue

                    val similarity = calculateCosineSimilarity(vecA, vecB)
                    comparisonPairsCount++
                    if (similarity >= 0.78f) {
                        overlappingCount++
                    }
                }
            }

            val overlapPct = if (comparisonPairsCount > 0) {
                ((overlappingCount.toDouble() / comparisonPairsCount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            _overlapPercentage.value = overlapPct
            _scanProgress.value = 1.0f

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }
    }

    fun calculateCosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        val len = minOf(vec1.size, vec2.size)
        if (len == 0) return 0f
        for (i in 0 until len) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        val denom = sqrt(normA.toDouble()) * sqrt(normB.toDouble())
        return if (denom == 0.0) 0f else (dotProduct / denom).toFloat()
    }
}
