package com.example.domain.usecase

import com.example.data.model.CommitEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class CreateCommitUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(message: String, affectedFiles: List<String>) {
        repository.logCommit(message, affectedFiles)
    }
}

class PushCommitsUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(commits: List<CommitEntity>) {
        val unpushed = commits.filter { !it.isPushed }
        if (unpushed.isNotEmpty()) {
            repository.markCommitsAsPushed(unpushed.map { it.id })
        }
    }
}

class ClearCommitHistoryUseCase(private val repository: FileRepository) {
    suspend operator fun invoke() {
        repository.clearAllCommits()
    }
}
