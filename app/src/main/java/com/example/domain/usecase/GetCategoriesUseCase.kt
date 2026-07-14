package com.example.domain.usecase

import com.example.data.model.CategoryEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repository: FileRepository) {
    operator fun invoke(): Flow<List<CategoryEntity>> = repository.categories
}
