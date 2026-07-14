package com.example.domain.usecase

import com.example.data.model.SecureStateEntity
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class GetSecureStateUseCase(private val repository: FileRepository) {
    operator fun invoke(): Flow<SecureStateEntity?> = repository.secureState
}
