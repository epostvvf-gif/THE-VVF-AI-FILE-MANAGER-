package com.example.domain.usecase

import com.example.data.model.SecureStateEntity
import com.example.data.repository.FileRepository
import com.example.util.CryptoHelper

class SetupPinUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(pin: String) {
        val hashedPin = CryptoHelper.hashPin(pin)
        repository.setSecureState(
            SecureStateEntity(
                pin = hashedPin,
                isLocked = true,
                hint = "Created successfully"
            )
        )
    }
}
