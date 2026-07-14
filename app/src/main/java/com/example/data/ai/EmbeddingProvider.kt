package com.example.data.ai

/**
 * Interface defining the contract for generating text embeddings.
 * Supports both cloud-based Gemini Embeddings and local on-device fallbacks.
 */
interface EmbeddingProvider {
    suspend fun getEmbedding(text: String): FloatArray
}
