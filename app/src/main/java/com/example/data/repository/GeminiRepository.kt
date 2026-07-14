package com.example.data.repository

import com.example.data.api.Content
import com.example.data.api.GeminiApi
import com.example.data.api.GeminiRequest
import com.example.data.api.Part
import com.example.data.local.ChatMessageDao
import com.example.data.model.ChatMessageEntity
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GeminiRepository(
    private val chatMessageDao: ChatMessageDao,
    private val fileDao: com.example.data.local.FileDao
) {

    private val moshi = Moshi.Builder().build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApi::class.java)

    val chatMessagesFlow: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessagesFlow()

    suspend fun insertMessage(text: String, sender: String) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(ChatMessageEntity(text = text, sender = sender))
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAllMessages()
    }

    private suspend fun generateOfflineResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val query = prompt.lowercase()
        val allFiles = fileDao.getLocalFiles()
        val junkFiles = fileDao.getJunkFiles()
        val duplicates = fileDao.getDuplicates()
        
        when {
            query.contains("duplicate") || query.contains("copy") || query.contains("दोहरा") || query.contains("डुप") -> {
                if (duplicates.isEmpty()) {
                    "[Offline Mode] No duplicate files were found in your local storage. Your storage is perfectly optimized!"
                } else {
                    val groupCount = duplicates.groupBy { it.duplicateGroupId }.size
                    "[Offline Mode] Found ${duplicates.size} duplicate files across $groupCount groups in your local storage. You can free up storage by deleting them in the Local File tab."
                }
            }
            query.contains("junk") || query.contains("clean") || query.contains("कचरा") || query.contains("साफ") -> {
                if (junkFiles.isEmpty()) {
                    "[Offline Mode] No junk files found. Your device storage is completely clean!"
                } else {
                    val totalSize = junkFiles.sumOf { it.size }
                    val formattedSize = java.text.DecimalFormat("#.##").format(totalSize / (1024.0 * 1024.0))
                    "[Offline Mode] You have ${junkFiles.size} junk/cache files occupying $formattedSize MB. Head to the 'Junk Scanner' section in the Local File tab to safely clean them!"
                }
            }
            query.contains("image") || query.contains("photo") || query.contains("चित्र") || query.contains("फोटो") -> {
                val images = allFiles.filter { it.categoryId == "IMAGES" }
                if (images.isEmpty()) {
                    "[Offline Mode] You don't have any indexed photos/images in your storage yet."
                } else {
                    "[Offline Mode] You have ${images.size} indexed photos, including: ${images.take(3).joinToString { it.name }}. You can view and manage them by clicking the 'Images' category button."
                }
            }
            query.contains("size") || query.contains("large") || query.contains("storage") || query.contains("बड़ा") || query.contains("स्टोरेज") -> {
                val largest = allFiles.maxByOrNull { it.size }
                if (largest != null) {
                    val formattedSize = java.text.DecimalFormat("#.##").format(largest.size / (1024.0 * 1024.0))
                    "[Offline Mode] Your largest file is '${largest.name}' ($formattedSize MB). Sorting files by size in the main tab will help you locate other space-consuming files."
                } else {
                    "[Offline Mode] No files indexed yet. Your local storage is empty."
                }
            }
            else -> {
                "[Offline Mode] Hello! I am your Offline File Manager Assistant. It looks like you haven't configured your Gemini API Key in settings or are currently offline.\n\n" +
                "You can still ask me local storage questions like:\n" +
                "- 'How many duplicate files do I have?'\n" +
                "- 'How much space can I free by cleaning junk?'\n" +
                "- 'What are my largest files?'\n\n" +
                "Once you add your API key, I will unlock full generative intelligence to answer complex general queries!"
            }
        }
    }

    suspend fun generateResponse(
        prompt: String,
        apiKey: String,
        isHighThinkingMode: Boolean,
        history: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE" || apiKey.contains("placeholder", ignoreCase = true)) {
            return@withContext generateOfflineResponse(prompt)
        }
        try {
            // Select model: high-thinking mode uses gemini-3.1-pro-preview for complex reasoning,
            // otherwise gemini-3.5-flash for speed and efficiency.
            val model = if (isHighThinkingMode) "gemini-3.1-pro-preview" else "gemini-3.5-flash"

            // Construct conversation contents with history
            val contents = mutableListOf<Content>()
            
            // Limit history to last 10 messages for token usage and latency
            val boundedHistory = history.takeLast(10)
            for (msg in boundedHistory) {
                contents.add(
                    Content(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(Part(text = msg.text))
                    )
                )
            }
            // Add current message
            contents.add(Content(role = "user", parts = listOf(Part(text = prompt))))

            val systemInstruction = Content(
                parts = listOf(
                    Part(
                        text = "You are an intelligent file system assistant for 'Smart File & Cloud Manager'. " +
                                "Help users manage their storage, find duplicates, understand file sizes, " +
                                "and structure their digital life. Speak politely, and be helpful."
                    )
                )
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction
            )

            val response = api.generateContent(model = model, apiKey = apiKey, request = request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            candidateText ?: "No response from AI."
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local offline analysis instead of just throwing error!
            val offlineAns = generateOfflineResponse(prompt)
            "[Network Fallback] I encountered a connection issue, but here is my local analysis:\n\n$offlineAns"
        }
    }
}
