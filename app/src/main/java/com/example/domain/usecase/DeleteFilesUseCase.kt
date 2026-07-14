package com.example.domain.usecase

import com.example.data.repository.FileRepository

class DeleteFilesUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(ids: List<Long>) {
        repository.deleteFiles(ids)
    }
}
