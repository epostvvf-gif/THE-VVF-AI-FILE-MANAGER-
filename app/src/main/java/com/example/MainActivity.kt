package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.FileRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.VVFSmartFileManagerUltraTheme
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CloudViewModel
import com.example.viewmodel.FileViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var fileViewModel: FileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (com.example.BuildConfig.DEBUG) {
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch the centralized appContainer
        val appContainer = (application as SmartFileApplication).appContainer

        // Initialize ViewModels with ViewModelProvider.Factory using appContainer
        fileViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FileViewModel(
                    appContainer.populateInitialDataUseCase,
                    appContainer.getLocalFilesUseCase,
                    appContainer.getCategoriesUseCase,
                    appContainer.getSafeFilesUseCase,
                    appContainer.getJunkFilesUseCase,
                    appContainer.getDuplicatesUseCase,
                    appContainer.getSecureStateUseCase,
                    appContainer.deleteFilesUseCase,
                    appContainer.moveToSafeUseCase,
                    appContainer.restoreFromSafeUseCase,
                    appContainer.cleanJunkUseCase,
                    appContainer.setupPinUseCase,
                    appContainer.duplicateDetector,
                    appContainer.searchService
                ) as T
            }
        })[FileViewModel::class.java]

        val cloudViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CloudViewModel(
                    appContainer.getCloudFilesUseCase,
                    appContainer.getCommitsUseCase,
                    appContainer.createCommitUseCase,
                    appContainer.pushCommitsUseCase,
                    appContainer.clearCommitHistoryUseCase,
                    appContainer.deleteFilesUseCase,
                    appContainer.addCloudFileUseCase,
                    appContainer.fileRepository,
                    appContainer.semanticEngine
                ) as T
            }
        })[CloudViewModel::class.java]

        val aiViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiViewModel(
                    appContainer.geminiRepository,
                    appContainer.settingsRepository
                ) as T
            }
        })[AiViewModel::class.java]

        val authViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(applicationContext) as T
            }
        })[AuthViewModel::class.java]

        // Set Screen content
        setContent {
            val darkModeSetting by authViewModel.darkModeSetting.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (darkModeSetting) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            VVFSmartFileManagerUltraTheme(darkTheme = useDarkTheme) {
                val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

                if (currentUser == null) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            // Successful authentication triggers navigation to main screen
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MainScreen(
                        fileViewModel = fileViewModel,
                        cloudViewModel = cloudViewModel,
                        aiViewModel = aiViewModel,
                        authViewModel = authViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Zero out in-memory session state and auto-lock the secure folder on background transition / screen off
        if (::fileViewModel.isInitialized) {
            fileViewModel.lockSecureFolder()
        }
    }
}
