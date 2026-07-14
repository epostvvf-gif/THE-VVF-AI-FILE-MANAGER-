package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryEntity
import com.example.data.model.FileEntity
import com.example.data.model.SecureStateEntity
import com.example.domain.usecase.*
import com.example.util.CryptoHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FileViewModel(
    private val populateInitialDataUseCase: PopulateInitialDataUseCase,
    private val getLocalFilesUseCase: GetLocalFilesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSafeFilesUseCase: GetSafeFilesUseCase,
    private val getJunkFilesUseCase: GetJunkFilesUseCase,
    private val getDuplicatesUseCase: GetDuplicatesUseCase,
    private val getSecureStateUseCase: GetSecureStateUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val moveToSafeUseCase: MoveToSafeUseCase,
    private val restoreFromSafeUseCase: RestoreFromSafeUseCase,
    private val cleanJunkUseCase: CleanJunkUseCase,
    private val setupPinUseCase: SetupPinUseCase,
    private val duplicateDetector: com.example.data.ai.DuplicateDetector,
    private val searchService: com.example.data.ai.SearchService
) : ViewModel() {

    init {
        viewModelScope.launch {
            populateInitialDataUseCase()
        }
    }

    // --- Search & Filter State ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null) // null means All
    val currentSortOption = MutableStateFlow(com.example.domain.usecase.SortOption.NAME)

    // --- Multi-select State ---
    val selectedFileIds = MutableStateFlow<Set<Long>>(emptySet())
    val isMultiSelectMode = MutableStateFlow(false)

    // --- Local Files flow ---
    val localFilesState: StateFlow<List<FileEntity>> = getLocalFilesUseCase(searchQuery, selectedCategory, currentSortOption)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Categories flow ---
    val categoriesState: StateFlow<List<CategoryEntity>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Safe Files flow ---
    val safeFilesState: StateFlow<List<FileEntity>> = getSafeFilesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Junk Files flow ---
    val junkFilesState: StateFlow<List<FileEntity>> = getJunkFilesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Duplicates flow ---
    val exactDuplicatesState = MutableStateFlow<List<List<FileEntity>>>(emptyList())
    val nearDuplicatesState = MutableStateFlow<List<List<FileEntity>>>(emptyList())
    val semanticDuplicatesState = MutableStateFlow<List<List<FileEntity>>>(emptyList())

    val duplicatesState = MutableStateFlow<List<FileEntity>>(emptyList())

    // --- Secure PIN Folder State ---
    val secureState: StateFlow<SecureStateEntity?> = getSecureStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // Temp PIN flow during setup
    val firstEnteredPin = MutableStateFlow("")
    val pinSetupStep = MutableStateFlow(1) // 1 = Enter PIN, 2 = Confirm PIN, 3 = Setup Complete
    val secureFolderUnlocked = MutableStateFlow(false)
    val pinError = MutableStateFlow<String?>(null)

    // --- Junk Cleaner UI State ---
    val isCleaningJunk = MutableStateFlow(false)
    val junkCleanedSuccess = MutableStateFlow(false)

    // --- Duplicate Scanner UI State ---
    val isScanningDuplicates = MutableStateFlow(false)
    val scanProgress = MutableStateFlow(0f)
    val scanCompleted = MutableStateFlow(false)

    // --- Multi-select actions ---
    fun toggleFileSelection(fileId: Long) {
        val current = selectedFileIds.value
        if (current.contains(fileId)) {
            selectedFileIds.value = current - fileId
            if (selectedFileIds.value.isEmpty()) {
                isMultiSelectMode.value = false
            }
        } else {
            selectedFileIds.value = current + fileId
            isMultiSelectMode.value = true
        }
    }

    fun selectAllFiles() {
        val currentFiles = localFilesState.value
        selectedFileIds.value = currentFiles.map { it.id }.toSet()
        isMultiSelectMode.value = true
    }

    fun clearSelection() {
        selectedFileIds.value = emptySet()
        isMultiSelectMode.value = false
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            val idsToDelete = selectedFileIds.value.toList()
            deleteFilesUseCase(idsToDelete)
            clearSelection()
        }
    }

    fun moveSelectedToSafe() {
        viewModelScope.launch {
            val idsToMove = selectedFileIds.value.toList()
            moveToSafeUseCase(idsToMove)
            clearSelection()
        }
    }

    // --- Junk Cleaner Actions ---
    fun cleanJunk() {
        viewModelScope.launch {
            isCleaningJunk.value = true
            junkCleanedSuccess.value = false
            cleanJunkUseCase()
            isCleaningJunk.value = false
            junkCleanedSuccess.value = true
        }
    }

    fun resetJunkCleanState() {
        junkCleanedSuccess.value = false
    }

    // --- Duplicate Scanner Actions ---
    fun scanForDuplicates() {
        viewModelScope.launch {
            isScanningDuplicates.value = true
            scanProgress.value = 0f
            scanCompleted.value = false

            try {
                val results = duplicateDetector.detectDuplicates { progress ->
                    scanProgress.value = progress
                }
                exactDuplicatesState.value = results.exactDuplicates
                nearDuplicatesState.value = results.nearDuplicates
                semanticDuplicatesState.value = results.semanticDuplicates

                // Flatten all unique duplicates for backward compatibility
                duplicatesState.value = (results.exactDuplicates.flatten() + 
                                         results.nearDuplicates.flatten() + 
                                         results.semanticDuplicates.flatten()).distinctBy { it.id }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            scanProgress.value = 1.0f
            isScanningDuplicates.value = false
            scanCompleted.value = true
        }
    }

    fun deleteDuplicate(file: FileEntity) {
        viewModelScope.launch {
            deleteFilesUseCase(listOf(file.id))
        }
    }

    fun deleteFileById(id: Long) {
        viewModelScope.launch {
            deleteFilesUseCase(listOf(id))
        }
    }

    // --- Secure Safe Folder PIN Actions ---
    fun submitPinSetup(enteredPin: String) {
        val currentStep = pinSetupStep.value
        if (currentStep == 1) {
            if (enteredPin.length != 4) {
                pinError.value = "PIN must be exactly 4 digits."
                return
            }
            firstEnteredPin.value = enteredPin
            pinSetupStep.value = 2
            pinError.value = null
        } else if (currentStep == 2) {
            if (enteredPin != firstEnteredPin.value) {
                pinError.value = "PINs do not match. Try again."
                pinSetupStep.value = 1
                firstEnteredPin.value = ""
                return
            }
            // Save PIN
            viewModelScope.launch {
                setupPinUseCase(enteredPin)
                pinSetupStep.value = 3
                secureFolderUnlocked.value = true
                pinError.value = null
            }
        }
    }

    fun verifyPinToUnlock(enteredPin: String) {
        val currentState = secureState.value
        if (currentState != null && CryptoHelper.verifyPin(enteredPin, currentState.pin)) {
            secureFolderUnlocked.value = true
            pinError.value = null
        } else {
            pinError.value = "Incorrect PIN. Please try again."
        }
    }

    fun lockSecureFolder() {
        secureFolderUnlocked.value = false
        pinError.value = null
    }

    fun restoreSelectedFromSafe(ids: List<Long>) {
        viewModelScope.launch {
            restoreFromSafeUseCase(ids)
        }
    }



    fun resetPinSetup() {
        pinSetupStep.value = 1
        firstEnteredPin.value = ""
        pinError.value = null
    }

    fun setSortOption(option: com.example.domain.usecase.SortOption) {
        currentSortOption.value = option
    }
}
