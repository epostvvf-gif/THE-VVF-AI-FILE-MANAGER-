package com.example.data.ai

/**
 * Interface defining local on-device vector projections and semantic computations.
 * Part of the VVF Smart File Manager Ultra production-grade zero-cloud AI architecture.
 */
interface OnDeviceAIEngineInterface {
    /**
     * Projects a text query or document metadata/content into a stable 128-dimensional L2-normalized vector.
     */
    fun projectTextToVector(text: String): FloatArray

    /**
     * Projects image visual characteristics (e.g., from contours or color histograms) into a stable 128-D vector.
     */
    fun projectImageToVector(fileName: String): FloatArray

    /**
     * Calculates the Cosine Similarity between two 128-dimensional normalized vectors.
     * Returns a score between -1.0 and 1.0. A score of >= 0.65 qualifies as a high-confidence semantic match.
     */
    fun calculateCosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float
}
