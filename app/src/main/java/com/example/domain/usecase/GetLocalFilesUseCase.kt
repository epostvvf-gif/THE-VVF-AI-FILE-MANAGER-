package com.example.domain.usecase

import com.example.data.model.FileEntity
import com.example.data.repository.FileRepository
import com.example.data.ai.OnDeviceAIEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

enum class SortOption { NAME, DATE, SIZE, SEMANTIC }

class GetLocalFilesUseCase(private val repository: FileRepository) {
    operator fun invoke(query: Flow<String>, category: Flow<String?>, sortOption: Flow<SortOption>): Flow<List<FileEntity>> {
        return repository.localFiles
            .combine(query) { files, q ->
                if (q.isBlank()) {
                    files
                } else {
                    val queryVector = OnDeviceAIEngine.projectTextToVector(q)
                    val scoredFiles = files.map { file ->
                        val fileVector = if (file.mimeType.startsWith("image/")) {
                            OnDeviceAIEngine.projectImageToVector(file.name)
                        } else {
                            OnDeviceAIEngine.projectTextToVector(file.name)
                        }
                        val similarity = OnDeviceAIEngine.calculateCosineSimilarity(queryVector, fileVector)
                        Pair(file, similarity)
                    }

                    // Strict threshold of >= 0.65 for semantic similarity, or name matches (fail-safe)
                    val filtered = scoredFiles.filter { (file, similarity) ->
                        similarity >= 0.65f || file.name.contains(q, ignoreCase = true)
                    }

                    // Sort semantically based on cosine similarity
                    filtered.sortedByDescending { it.second }.map { it.first }
                }
            }
            .combine(category) { files, cat ->
                if (cat == null) files else files.filter { it.categoryId == cat }
            }
            .combine(sortOption) { files: List<FileEntity>, sort: SortOption ->
                when (sort) {
                    SortOption.NAME -> files.sortedBy { it.name.lowercase() }
                    SortOption.DATE -> files.sortedByDescending { it.dateAdded }
                    SortOption.SIZE -> files.sortedByDescending { it.size }
                    SortOption.SEMANTIC -> files // Semantically ranked files from above block
                }
            }
    }
}

