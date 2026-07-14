package com.example.domain.usecase

import com.example.data.model.FileEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class GetJunkFilesUseCase(private val repository: FileRepository) {
    operator fun invoke(): Flow<List<FileEntity>> = repository.junkFiles
}
