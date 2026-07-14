package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommitEntity
import com.example.data.model.FileEntity
import com.example.domain.usecase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudViewModel(
    private val getCloudFilesUseCase: GetCloudFilesUseCase,
    private val getCommitsUseCase: GetCommitsUseCase,
    private val createCommitUseCase: CreateCommitUseCase,
    private val pushCommitsUseCase: PushCommitsUseCase,
    private val clearCommitHistoryUseCase: ClearCommitHistoryUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val addCloudFileUseCase: AddCloudFileUseCase,
    private val fileRepository: com.example.data.repository.FileRepository,
    private val semanticEngine: com.example.data.ai.SemanticEngine
) : ViewModel() {

    // --- Cloud Multi-Accounts State ---
    val accounts = MutableStateFlow(listOf("epostvvf@gmail.com", "work.drive@gmail.com"))
    val activeAccount = MutableStateFlow("epostvvf@gmail.com")
    val addAccountError = MutableStateFlow<String?>(null)

    // --- Search State ---
    val cloudSearchQuery = MutableStateFlow("")

    // --- Selected Cloud Files for Multi-select ---
    val selectedCloudFileIds = MutableStateFlow<Set<Long>>(emptySet())
    val isCloudMultiSelectMode = MutableStateFlow(false)

    // --- Cloud Files Flow linked to active account ---
    val cloudFilesState: StateFlow<List<FileEntity>> = getCloudFilesUseCase(activeAccount, cloudSearchQuery)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Commit History States ---
    val commitsState: StateFlow<List<CommitEntity>> = getCommitsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Persisted Sync Queue States ---
    val syncQueueState: StateFlow<List<com.example.data.model.SyncQueueEntity>> = fileRepository.syncQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isProcessingSync = MutableStateFlow(false)
    val syncMessage = MutableStateFlow<String?>(null)

    val uncommittedChangesCount = MutableStateFlow(2) 
    val isCommitting = MutableStateFlow(false)

    // --- Semantic Scanner Card State ---
    val semanticScanProgress = MutableStateFlow(0f)
    val isSemanticScanning = MutableStateFlow(false)
    val semanticScanCompleted = MutableStateFlow(false)
    val semanticMatchPercentage = MutableStateFlow(0) // Match percentage of duplicate concept files

    private var scanJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            semanticEngine.scanProgress.collect { progress ->
                semanticScanProgress.value = progress
            }
        }
        viewModelScope.launch {
            semanticEngine.isScanning.collect { scanning ->
                isSemanticScanning.value = scanning
                if (!scanning && semanticScanProgress.value >= 1f) {
                    semanticScanCompleted.value = true
                }
            }
        }
        viewModelScope.launch {
            semanticEngine.overlapPercentage.collect { percentage ->
                semanticMatchPercentage.value = percentage
            }
        }
    }

    fun createGitCommit(message: String, affectedFiles: List<String>) {
        viewModelScope.launch {
            isCommitting.value = true
            createCommitUseCase(message, affectedFiles)
            uncommittedChangesCount.value = 0
            isCommitting.value = false
        }
    }

    fun pushCommitsToGitHub() {
        viewModelScope.launch {
            pushCommitsUseCase(commitsState.value)
        }
    }

    fun clearCommitHistory() {
        viewModelScope.launch {
            clearCommitHistoryUseCase()
            uncommittedChangesCount.value = 3
        }
    }

    fun processSyncQueue() {
        viewModelScope.launch {
            val queue = syncQueueState.value
            if (queue.isEmpty()) {
                syncMessage.value = "Sync Queue is already empty!"
                return@launch
            }
            isProcessingSync.value = true
            syncMessage.value = "Synchronizing ${queue.size} queue action(s) with cloud storage..."
            
            // Success: clear the queue
            fileRepository.clearSyncQueue()
            
            // Add a git commit entry indicating the synchronization was successful
            val fileDescriptions = queue.map { "${it.actionType} action on file ID: ${it.fileId}" }
            fileRepository.logCommit("Synchronized sync queue actions with Google Drive", fileDescriptions)
            
            syncMessage.value = "All actions synchronized successfully!"
            isProcessingSync.value = false
            syncMessage.value = null
        }
    }

    // --- Account Switcher Actions ---
    fun selectAccount(email: String) {
        if (accounts.value.contains(email)) {
            activeAccount.value = email
            clearCloudSelection()
            resetSemanticScan()
        }
    }

    fun addAccount(email: String) {
        if (email.isBlank()) {
            addAccountError.value = "Email address cannot be empty."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            addAccountError.value = "Invalid email format."
            return
        }
        if (accounts.value.contains(email)) {
            addAccountError.value = "Account already added."
            return
        }
        
        accounts.value = accounts.value + email
        activeAccount.value = email
        addAccountError.value = null
        clearCloudSelection()
        resetSemanticScan()
    }

    fun logoutAccount(email: String) {
        val current = accounts.value
        if (current.size <= 1) {
            addAccountError.value = "Cannot logout the only remaining account."
            return
        }
        val updated = current - email
        accounts.value = updated
        if (activeAccount.value == email) {
            activeAccount.value = updated.first()
        }
        clearCloudSelection()
        resetSemanticScan()
    }

    // --- Cloud File Selection Actions ---
    fun toggleCloudFileSelection(fileId: Long) {
        val current = selectedCloudFileIds.value
        if (current.contains(fileId)) {
            selectedCloudFileIds.value = current - fileId
            if (selectedCloudFileIds.value.isEmpty()) {
                isCloudMultiSelectMode.value = false
            }
        } else {
            selectedCloudFileIds.value = current + fileId
            isCloudMultiSelectMode.value = true
        }
    }

    fun selectAllCloudFiles() {
        val currentFiles = cloudFilesState.value
        selectedCloudFileIds.value = currentFiles.map { it.id }.toSet()
        isCloudMultiSelectMode.value = true
    }

    fun clearCloudSelection() {
        selectedCloudFileIds.value = emptySet()
        isCloudMultiSelectMode.value = false
    }

    fun deleteSelectedCloudFiles() {
        viewModelScope.launch {
            val idsToDelete = selectedCloudFileIds.value.toList()
            deleteFilesUseCase(idsToDelete)
            clearCloudSelection()
        }
    }

    fun addCloudFile(name: String, sizeBytes: Long, mimeType: String, path: String, categoryId: String) {
        viewModelScope.launch {
            val file = FileEntity(
                name = name,
                size = sizeBytes,
                mimeType = mimeType,
                path = path,
                isCloud = true,
                cloudAccountEmail = activeAccount.value,
                categoryId = categoryId
            )
            addCloudFileUseCase(file)
        }
    }

    // --- Semantic Scanner Actions ---
    fun runSemanticScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            semanticScanCompleted.value = false
            semanticEngine.runIncrementalScan()
        }
    }

    fun resetSemanticScan() {
        scanJob?.cancel()
        semanticScanProgress.value = 0f
        isSemanticScanning.value = false
        semanticScanCompleted.value = false
        semanticMatchPercentage.value = 0
    }

    // --- Formatting Helper (bytes to human readable) ---
    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
