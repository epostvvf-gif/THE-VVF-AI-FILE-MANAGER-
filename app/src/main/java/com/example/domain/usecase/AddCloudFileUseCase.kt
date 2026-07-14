package com.example.domain.usecase

import com.example.data.model.FileEntity
import com.example.data.repository.FileRepository

class AddCloudFileUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(file: FileEntity): Long {
        return repository.addFile(file)
    }
}
