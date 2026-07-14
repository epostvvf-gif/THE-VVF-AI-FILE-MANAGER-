package com.example.domain.usecase

import com.example.data.model.CommitEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class GetCommitsUseCase(private val repository: FileRepository) {
    operator fun invoke(): Flow<List<CommitEntity>> = repository.commits
}
