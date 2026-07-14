package com.example.data.ai

import com.example.data.repository.SettingsRepository
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class EmbeddingPart(val text: String)

@JsonClass(generateAdapter = true)
data class EmbeddingContent(val parts: List<EmbeddingPart>)

@JsonClass(generateAdapter = true)
data class EmbeddingRequest(val content: EmbeddingContent)

@JsonClass(generateAdapter = true)
data class EmbeddingValues(val values: List<Float>)

@JsonClass(generateAdapter = true)
data class EmbeddingResponse(val embedding: EmbeddingValues)

class EmbeddingService(private val settingsRepository: SettingsRepository) : EmbeddingProvider {

    private val moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter(EmbeddingRequest::class.java)
    private val responseAdapter = moshi.adapter(EmbeddingResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun getEmbedding(text: String): FloatArray = withContext(Dispatchers.IO) {
        val savedKey = settingsRepository.apiKeyFlow.firstOrNull()
        val apiKey = if (!savedKey.isNullOrBlank()) {
            savedKey
        } else {
            val configKey = com.example.BuildConfig.GEMINI_API_KEY
            if (!configKey.isNullOrBlank() && configKey != "MY_GEMINI_API_KEY") configKey else null
        }

        if (apiKey.isNullOrBlank()) {
            return@withContext OnDeviceAIEngine.projectTextToVector(text)
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2-preview:embedContent?key=$apiKey"
        val requestBodyObj = EmbeddingRequest(EmbeddingContent(listOf(EmbeddingPart(text))))
        val jsonRequest = jsonAdapter.toJson(requestBodyObj)

        val request = Request.Builder()
            .url(requestUrl)
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unsuccessful embedding call: ${response.code}")
                }
                val bodyString = response.body?.string() ?: throw IOException("Empty response")
                val responseObj = responseAdapter.fromJson(bodyString)
                val values = responseObj?.embedding?.values
                if (values != null && values.isNotEmpty()) {
                    return@withContext values.toFloatArray()
                }
                throw IOException("Empty embedding values")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            OnDeviceAIEngine.projectTextToVector(text)
        }
    }
}
