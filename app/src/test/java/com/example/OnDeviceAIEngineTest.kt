package com.example

import com.example.data.ai.OnDeviceAIEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class OnDeviceAIEngineTest {

    @Test
    fun testTextProjectionVectorLengthAndNormalization() {
        val vector = OnDeviceAIEngine.projectTextToVector("Important Invoice PDF")
        
        // Ensure 128-dimensional output
        assertEquals(128, vector.size)

        // Ensure L2 Normalization is exactly 1.0 (with slight float precision tolerance)
        var sumSquares = 0f
        for (v in vector) {
            sumSquares += v * v
        }
        val magnitude = sqrt(sumSquares)
        assertTrue(abs(1.0f - magnitude) < 1e-4f)
    }

    @Test
    fun testImageProjectionVectorLengthAndNormalization() {
        val vector = OnDeviceAIEngine.projectImageToVector("nature_camping_trip_2026.png")

        // Ensure 128-dimensional output
        assertEquals(128, vector.size)

        // Ensure L2 Normalization is exactly 1.0
        var sumSquares = 0f
        for (v in vector) {
            sumSquares += v * v
        }
        val magnitude = sqrt(sumSquares)
        assertTrue(abs(1.0f - magnitude) < 1e-4f)
    }

    @Test
    fun testCosineSimilarityIdenticalVectors() {
        val vec1 = OnDeviceAIEngine.projectTextToVector("Sample Text")
        val vec2 = OnDeviceAIEngine.projectTextToVector("Sample Text")

        val similarity = OnDeviceAIEngine.calculateCosineSimilarity(vec1, vec2)
        // Cosine similarity of identical normalized vectors must be 1.0
        assertTrue(abs(1.0f - similarity) < 1e-4f)
    }

    @Test
    fun testCosineSimilarityDistinctVectors() {
        val vec1 = OnDeviceAIEngine.projectTextToVector("Invoice Statement PDF")
        val vec2 = OnDeviceAIEngine.projectTextToVector("Nature Camping Photo")

        val similarity = OnDeviceAIEngine.calculateCosineSimilarity(vec1, vec2)
        // Since words are completely distinct, the projection seeds are distinct.
        // Let's verify it produces a valid similarity score range [-1, 1].
        assertTrue(similarity in -1.0f..1.0f)
    }
}
