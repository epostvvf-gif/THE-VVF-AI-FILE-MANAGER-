package com.example

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import com.example.data.local.AppDatabase
import com.example.data.repository.FileRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.usecase.*

class SmartFileApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
        appContainer = AppContainer(this)
    }
}

class AppContainer(private val application: Application) {

    // --- Database ---
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(application)
    }

    // --- DAOs ---
    val fileDao by lazy { database.fileDao() }
    val categoryDao by lazy { database.categoryDao() }
    val secureStateDao by lazy { database.secureStateDao() }
    val chatMessageDao by lazy { database.chatMessageDao() }
    val commitDao by lazy { database.commitDao() }
    val syncQueueDao by lazy { database.syncQueueDao() }
    val embeddingDao by lazy { database.embeddingDao() }
    val vectorMetadataDao by lazy { database.vectorMetadataDao() }
    val searchHistoryDao by lazy { database.searchHistoryDao() }

    // --- Settings Repository ---
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(application)
    }

    // --- AI Services & Engines ---
    val embeddingService by lazy {
        com.example.data.ai.EmbeddingService(settingsRepository)
    }
    val semanticEngine by lazy {
        com.example.data.ai.SemanticEngine(fileDao, embeddingDao, embeddingService)
    }
    val searchService by lazy {
        com.example.data.ai.SearchService(fileDao, embeddingDao, searchHistoryDao, embeddingService)
    }
    val duplicateDetector by lazy {
        com.example.data.ai.DuplicateDetector(fileDao, embeddingDao, semanticEngine)
    }

    // --- Repositories ---
    val fileRepository: FileRepository by lazy {
        FileRepository(application, fileDao, categoryDao, secureStateDao, commitDao, syncQueueDao)
    }

    val geminiRepository: GeminiRepository by lazy {
        GeminiRepository(chatMessageDao, fileDao)
    }

    // --- Use Cases ---
    val populateInitialDataUseCase by lazy { PopulateInitialDataUseCase(fileRepository) }
    val getLocalFilesUseCase by lazy { GetLocalFilesUseCase(fileRepository) }
    val getCategoriesUseCase by lazy { GetCategoriesUseCase(fileRepository) }
    val getSafeFilesUseCase by lazy { GetSafeFilesUseCase(fileRepository) }
    val getJunkFilesUseCase by lazy { GetJunkFilesUseCase(fileRepository) }
    val getDuplicatesUseCase by lazy { GetDuplicatesUseCase(fileRepository) }
    val getSecureStateUseCase by lazy { GetSecureStateUseCase(fileRepository) }
    val deleteFilesUseCase by lazy { DeleteFilesUseCase(fileRepository) }
    val moveToSafeUseCase by lazy { MoveToSafeUseCase(fileRepository) }
    val restoreFromSafeUseCase by lazy { RestoreFromSafeUseCase(fileRepository) }
    val cleanJunkUseCase by lazy { CleanJunkUseCase(fileRepository) }
    val setupPinUseCase by lazy { SetupPinUseCase(fileRepository) }

    val getCloudFilesUseCase by lazy { GetCloudFilesUseCase(fileRepository) }
    val getCommitsUseCase by lazy { GetCommitsUseCase(fileRepository) }
    val createCommitUseCase by lazy { CreateCommitUseCase(fileRepository) }
    val pushCommitsUseCase by lazy { PushCommitsUseCase(fileRepository) }
    val clearCommitHistoryUseCase by lazy { ClearCommitHistoryUseCase(fileRepository) }
    val addCloudFileUseCase by lazy { AddCloudFileUseCase(fileRepository) }
}
