package com.example.domain.usecase

import com.example.data.repository.FileRepository

class CleanJunkUseCase(private val repository: FileRepository) {
    suspend operator fun invoke() {
        repository.clearJunkFiles()
    }
}
