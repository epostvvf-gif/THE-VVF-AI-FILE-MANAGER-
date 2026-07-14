package com.example.data.ai

import com.example.data.local.EmbeddingDao
import com.example.data.local.FileDao
import com.example.data.local.SearchHistoryDao
import com.example.data.model.FileEntity
import com.example.data.model.SearchHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class SearchService(
    private val fileDao: FileDao,
    private val embeddingDao: EmbeddingDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val embeddingService: EmbeddingProvider
) {
    val searchHistoryFlow: Flow<List<SearchHistoryEntity>> = searchHistoryDao.getSearchHistoryFlow()

    suspend fun saveSearchQuery(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query))
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao.clearHistory()
    }

    /**
     * Performs a semantic search over all local indexed files based on a search query.
     */
    suspend fun searchSemantically(query: String): List<Pair<FileEntity, Float>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val queryVector = embeddingService.getEmbedding(query)

        val files = fileDao.getLocalFiles()
        val embeddings = embeddingDao.getAllEmbeddings().associateBy { it.fileId }

        val scored = files.map { file ->
            val emb = embeddings[file.id]
            val similarity = if (emb != null) {
                val vec = emb.embeddingJson.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
                calculateCosineSimilarity(queryVector, vec)
            } else {
                val vec = if (file.mimeType.startsWith("image/")) {
                    OnDeviceAIEngine.projectImageToVector(file.name)
                } else {
                    OnDeviceAIEngine.projectTextToVector(file.name)
                }
                calculateCosineSimilarity(queryVector, vec)
            }
            Pair(file, similarity)
        }

        scored.filter { (file, similarity) ->
            similarity >= 0.40f
        }.sortedByDescending { it.second }
    }

    private fun calculateCosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
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
