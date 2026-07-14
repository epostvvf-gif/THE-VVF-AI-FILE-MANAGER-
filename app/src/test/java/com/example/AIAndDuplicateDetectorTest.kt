package com.example

import com.example.data.ai.DuplicateDetector
import com.example.data.ai.EmbeddingProvider
import com.example.data.ai.OnDeviceAIEngine
import com.example.data.ai.SearchService
import com.example.data.local.EmbeddingDao
import com.example.data.local.FileDao
import com.example.data.local.SearchHistoryDao
import com.example.data.model.CategoryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FileEntity
import com.example.data.model.EmbeddingEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.SecureStateEntity
import com.example.data.model.SyncQueueEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIAndDuplicateDetectorTest {

    // Helper fake for FileDao
    class FakeFileDao(var files: List<FileEntity> = emptyList()) : FileDao {
        override fun getLocalFilesFlow(): Flow<List<FileEntity>> = flowOf(files)
        override suspend fun getLocalFiles(): List<FileEntity> = files
        override fun getCloudFilesFlow(email: String): Flow<List<FileEntity>> = flowOf(emptyList())
        override suspend fun getCloudFiles(email: String): List<FileEntity> = emptyList()
        override fun getSafeFilesFlow(): Flow<List<FileEntity>> = flowOf(emptyList())
        override suspend fun getSafeFiles(): List<FileEntity> = emptyList()
        override fun getJunkFilesFlow(): Flow<List<FileEntity>> = flowOf(emptyList())
        override suspend fun getJunkFiles(): List<FileEntity> = emptyList()
        override fun getDuplicatesFlow(): Flow<List<FileEntity>> = flowOf(emptyList())
        override suspend fun getDuplicates(): List<FileEntity> = emptyList()
        override suspend fun insertFile(file: FileEntity): Long = 0
        override suspend fun insertFiles(files: List<FileEntity>) {}
        override suspend fun updateFile(file: FileEntity) {}
        override suspend fun deleteFile(file: FileEntity) {}
        override suspend fun deleteFilesByIds(ids: List<Long>) {}
        override suspend fun updateSafeStatus(ids: List<Long>, isSafe: Boolean) {}
        override suspend fun clearJunkFiles() {}
    }

    // Helper fake for EmbeddingDao
    class FakeEmbeddingDao(var embeddings: List<EmbeddingEntity> = emptyList()) : EmbeddingDao {
        override suspend fun getEmbeddingByFileId(fileId: Long): EmbeddingEntity? {
            return embeddings.find { it.fileId == fileId }
        }
        override suspend fun getAllEmbeddings(): List<EmbeddingEntity> = embeddings
        override suspend fun insertEmbedding(embedding: EmbeddingEntity) {}
        override suspend fun insertEmbeddings(embeddings: List<EmbeddingEntity>) {}
        override suspend fun deleteEmbeddingByFileId(fileId: Long) {}
        override suspend fun deleteOrphanEmbeddings() {}
    }

    // Helper fake for SearchHistoryDao
    class FakeSearchHistoryDao : SearchHistoryDao {
        private val list = mutableListOf<SearchHistoryEntity>()
        override fun getSearchHistoryFlow(): Flow<List<SearchHistoryEntity>> = flowOf(list)
        override suspend fun insertSearch(search: SearchHistoryEntity) {
            list.add(search)
        }
        override suspend fun clearHistory() {
            list.clear()
        }
    }

    // Helper fake for EmbeddingProvider
    class FakeEmbeddingProvider : EmbeddingProvider {
        override suspend fun getEmbedding(text: String): FloatArray {
            return OnDeviceAIEngine.projectTextToVector(text)
        }
    }

    @Test
    fun testCosineSimilarityUnit() {
        val vectorA = floatArrayOf(1.0f, 0.0f, 0.0f)
        val vectorB = floatArrayOf(0.0f, 1.0f, 0.0f)
        val vectorC = floatArrayOf(1.0f, 0.0f, 0.0f)

        val simAB = OnDeviceAIEngine.calculateCosineSimilarity(vectorA, vectorB)
        val simAC = OnDeviceAIEngine.calculateCosineSimilarity(vectorA, vectorC)

        // Orthogonal vectors should have 0 similarity
        assertTrue(simAB < 1e-5f)
        // Identical vectors should have 1.0 similarity
        assertTrue(Math.abs(1.0f - simAC) < 1e-5f)
    }

    @Test
    fun testExactDuplicatesDetection() = runBlocking {
        // Files with exact same name and size are treated as exact duplicates (SHA-256 generated via computeSha256)
        val file1 = FileEntity(id = 1, name = "report.pdf", size = 1024, mimeType = "application/pdf", path = "/docs/report.pdf", categoryId = "docs")
        val file2 = FileEntity(id = 2, name = "report.pdf", size = 1024, mimeType = "application/pdf", path = "/download/report.pdf", categoryId = "docs")
        val file3 = FileEntity(id = 3, name = "other.pdf", size = 2048, mimeType = "application/pdf", path = "/docs/other.pdf", categoryId = "docs")

        val fileDao = FakeFileDao(listOf(file1, file2, file3))
        val embeddingDao = FakeEmbeddingDao()
        val provider = FakeEmbeddingProvider()
        val semanticEngine = com.example.data.ai.SemanticEngine(fileDao, embeddingDao, provider)

        val detector = DuplicateDetector(fileDao, embeddingDao, semanticEngine)
        val result = detector.detectDuplicates()

        assertEquals(1, result.exactDuplicates.size)
        assertEquals(2, result.exactDuplicates[0].size)
        assertTrue(result.exactDuplicates[0].any { it.id == 1L })
        assertTrue(result.exactDuplicates[0].any { it.id == 2L })
    }

    @Test
    fun testNearDuplicatesDetection() = runBlocking {
        // Files with similar name signature and same size bucket for image/audio
        val image1 = FileEntity(id = 1, name = "nature_holiday.jpg", size = 500000, mimeType = "image/jpeg", path = "/photos/nature_holiday.jpg", categoryId = "images")
        val image2 = FileEntity(id = 2, name = "nature_holiday_copy.jpg", size = 500100, mimeType = "image/jpeg", path = "/photos/nature_holiday_copy.jpg", categoryId = "images")

        val fileDao = FakeFileDao(listOf(image1, image2))
        val embeddingDao = FakeEmbeddingDao()
        val provider = FakeEmbeddingProvider()
        val semanticEngine = com.example.data.ai.SemanticEngine(fileDao, embeddingDao, provider)

        val detector = DuplicateDetector(fileDao, embeddingDao, semanticEngine)
        val result = detector.detectDuplicates()

        // Near duplicate checks filenames with matching signatures and same size bucket (e.g. 500KB range)
        assertEquals(1, result.nearDuplicates.size)
        assertEquals(2, result.nearDuplicates[0].size)
    }

    @Test
    fun testSemanticDuplicatesDetection() = runBlocking {
        val file1 = FileEntity(id = 1, name = "annual_financial_report_2026.docx", size = 2048, mimeType = "application/vnd.openxmlformats-officedocument", path = "/docs/annual_financial_report_2026.docx", categoryId = "docs")
        val file2 = FileEntity(id = 2, name = "financial_statement_yearly_2026.docx", size = 3072, mimeType = "application/vnd.openxmlformats-officedocument", path = "/docs/financial_statement_yearly_2026.docx", categoryId = "docs")

        val fileDao = FakeFileDao(listOf(file1, file2))

        // Create close embeddings JSON
        val embedding1 = OnDeviceAIEngine.projectTextToVector("financial statement")
        val embedding2 = OnDeviceAIEngine.projectTextToVector("financial statement") // identical for semantic match
        
        val embeddingEntity1 = EmbeddingEntity(fileId = 1, embeddingJson = embedding1.joinToString(","))
        val embeddingEntity2 = EmbeddingEntity(fileId = 2, embeddingJson = embedding2.joinToString(","))

        val embeddingDao = FakeEmbeddingDao(listOf(embeddingEntity1, embeddingEntity2))
        val provider = FakeEmbeddingProvider()
        val semanticEngine = com.example.data.ai.SemanticEngine(fileDao, embeddingDao, provider)

        val detector = DuplicateDetector(fileDao, embeddingDao, semanticEngine)
        val result = detector.detectDuplicates()

        assertEquals(1, result.semanticDuplicates.size)
        assertEquals(2, result.semanticDuplicates[0].size)
    }

    @Test
    fun testSearchServiceBenchmark() = runBlocking {
        val doc1 = FileEntity(id = 1, name = "Project Proposal Draft", size = 1500, mimeType = "text/plain", path = "/docs/prop.txt", categoryId = "docs")
        val doc2 = FileEntity(id = 2, name = "Vacation Packing List", size = 800, mimeType = "text/plain", path = "/docs/vacation.txt", categoryId = "docs")

        val fileDao = FakeFileDao(listOf(doc1, doc2))

        val vec1 = OnDeviceAIEngine.projectTextToVector("Project Proposal Draft")
        val vec2 = OnDeviceAIEngine.projectTextToVector("Vacation Packing List")

        val emb1 = EmbeddingEntity(fileId = 1, embeddingJson = vec1.joinToString(","))
        val emb2 = EmbeddingEntity(fileId = 2, embeddingJson = vec2.joinToString(","))

        val embeddingDao = FakeEmbeddingDao(listOf(emb1, emb2))
        val searchHistoryDao = FakeSearchHistoryDao()
        val provider = FakeEmbeddingProvider()

        val searchService = SearchService(fileDao, embeddingDao, searchHistoryDao, provider)

        // Let's benchmark the performance of finding relevant item
        val startTime = System.nanoTime()
        val results = searchService.searchSemantically("Proposal")
        val durationMs = (System.nanoTime() - startTime) / 1_000_000.0

        // Search must run extremely fast (usually well under 5ms for cached/indexed documents)
        assertTrue(durationMs < 100.0) // Relaxed benchmark margin to guard against platform load spikes
        assertEquals(1, results.size)
        assertEquals(1L, results[0].first.id)
    }
}
