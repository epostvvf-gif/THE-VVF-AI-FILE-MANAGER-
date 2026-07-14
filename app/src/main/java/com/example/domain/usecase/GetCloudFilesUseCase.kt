package com.example.domain.usecase

import com.example.data.model.FileEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GetCloudFilesUseCase(private val repository: FileRepository) {
    operator fun invoke(activeAccount: Flow<String>, query: Flow<String>): Flow<List<FileEntity>> {
        return activeAccount
            .flatMapLatest { email ->
                repository.getCloudFilesFlow(email)
            }
            .combine(query) { files, q ->
                if (q.isBlank()) files else files.filter { it.name.contains(q, ignoreCase = true) }
            }
    }
}
