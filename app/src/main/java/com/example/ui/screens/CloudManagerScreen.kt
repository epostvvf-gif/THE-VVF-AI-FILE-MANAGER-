package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommitEntity
import com.example.data.model.FileEntity
import com.example.viewmodel.CloudViewModel
import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudManagerScreen(
    viewModel: CloudViewModel,
    modifier: Modifier = Modifier
) {
    val cloudFiles by viewModel.cloudFilesState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()
    val searchQuery by viewModel.cloudSearchQuery.collectAsState()
    val selectedIds by viewModel.selectedCloudFileIds.collectAsState()
    val isMultiSelect by viewModel.isCloudMultiSelectMode.collectAsState()
    val addAccountError by viewModel.addAccountError.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showSimulateUploadDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Welcome / Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google Drive Simulation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Analyze and manage your synchronized clouds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Cloud Status Icon
                IconButton(
                    onClick = { /* Check Status */ },
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudQueue,
                        contentDescription = "Cloud Synced",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // --- Hero Banner ---
            Image(
                painter = painterResource(id = R.drawable.img_cloud_banner_1783686660280),
                contentDescription = "Cloud Storage Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Multi-select Cloud Toolbar ---
            AnimatedVisibility(visible = isMultiSelect) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedIds.size} items selected",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row {
                            TextButton(onClick = { viewModel.selectAllCloudFiles() }) {
                                Text("Select All", color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { viewModel.deleteSelectedCloudFiles() },
                                modifier = Modifier.testTag("cloud_delete_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete from Cloud",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = { viewModel.clearCloudSelection() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cancel selection",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // --- Multi-Account Switcher & Adder ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(accounts) { email ->
                        val isSelected = email == activeAccount
                        val containerColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        InputChip(
                            selected = isSelected,
                            onClick = { viewModel.selectAccount(email) },
                            label = { Text(email, fontSize = 12.sp) },
                            trailingIcon = {
                                if (accounts.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.logoutAccount(email) },
                                        modifier = Modifier.size(16.dp).testTag("logout_$email")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                                            contentDescription = "Logout",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = containerColor,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { showAddAccountDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                        .testTag("add_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Cloud Account",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Search Field ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.cloudSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloud_search_input"),
                placeholder = { Text("Search files in synchronized cloud...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.cloudSearchQuery.value = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Semantic Scan Card & Lists ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Semantic Scanner
                item { SemanticScannerCard(viewModel = viewModel) }

                // Persisted Sync Queue Panel (Step 6 Improvement)
                item { CloudSyncQueuePanel(viewModel = viewModel) }

                // GitHub Git Sync & Commit History Panel
                item { GitHubSyncPanel(viewModel = viewModel) }

                // Actions header & upload simulator button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cloud Backups",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        TextButton(
                            onClick = { showSimulateUploadDialog = true },
                            modifier = Modifier.testTag("upload_simulation_button")
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Upload")
                        }
                    }
                }

                // Cloud File List
                if (cloudFiles.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No files found in this cloud archive.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(cloudFiles, key = { it.id }) { file ->
                        CloudFileItem(
                            file = file,
                            isSelected = selectedIds.contains(file.id),
                            isMultiSelectMode = isMultiSelect,
                            onToggleSelection = { viewModel.toggleCloudFileSelection(file.id) },
                            sizeFormatter = { viewModel.formatFileSize(it) }
                        )
                    }
                }
            }
        }
    }

    // --- Dialog to Add Account ---
    if (showAddAccountDialog) {
        var newEmailInput by remember { mutableStateOf("") }
        var inputError by remember { mutableStateOf<String?>(null) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Google Cloud Drive",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Synchronize an additional GDrive profile into your consolidated view.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = {
                            newEmailInput = it
                            inputError = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        label = { Text("Google Email Account") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_email_input"),
                        isError = inputError != null || addAccountError != null
                    )

                    val dispError = inputError ?: addAccountError
                    if (dispError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dispError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddAccountDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newEmailInput.isBlank()) {
                                    inputError = "Email field cannot be empty."
                                } else {
                                    viewModel.addAccount(newEmailInput)
                                    if (addAccountError == null) {
                                        showAddAccountDialog = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("submit_add_account_button")
                        ) {
                            Text("Add Account")
                        }
                    }
                }
            }
        }
    }

    // --- Dialog to Simulate File Upload ---
    if (showSimulateUploadDialog) {
        var uploadName by remember { mutableStateOf("") }
        var uploadSizeString by remember { mutableStateOf("") }
        var uploadCategory by remember { mutableStateOf("DOCUMENTS") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showSimulateUploadDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Simulate Cloud Upload",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uploadName,
                        onValueChange = { uploadName = it },
                        label = { Text("File Name (e.g., Pitch_Deck.pdf)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uploadSizeString,
                        onValueChange = { uploadSizeString = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("File Size (in KB)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_size_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("File Category:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in listOf("IMAGES", "VIDEOS", "DOCUMENTS", "MUSIC")) {
                            val isSelected = cat == uploadCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { uploadCategory = cat },
                                label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSimulateUploadDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (uploadName.isNotBlank()) {
                                    val sizeKB = uploadSizeString.toLongOrNull() ?: 100
                                    viewModel.addCloudFile(
                                        name = uploadName,
                                        sizeBytes = sizeKB * 1024,
                                        mimeType = if (uploadName.endsWith(".jpg") || uploadName.endsWith(".png")) "image/png" else "application/pdf",
                                        path = "Google Drive/My Drive/${uploadName}",
                                        categoryId = uploadCategory
                                    )
                                    showSimulateUploadDialog = false
                                }
                            },
                            modifier = Modifier.testTag("submit_upload_button")
                        ) {
                            Text("Upload")
                        }
                    }
                }
            }
        }
    }
}

// --- Semantic Scan Card ---
@Composable
fun SemanticScannerCard(viewModel: CloudViewModel) {
    val isScanning by viewModel.isSemanticScanning.collectAsState()
    val progress by viewModel.semanticScanProgress.collectAsState()
    val completed by viewModel.semanticScanCompleted.collectAsState()
    val matchPercentage by viewModel.semanticMatchPercentage.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Semantic scan",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Cloud Semantic Scanner",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Scan files for contextual duplication across clouds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                if (!isScanning && !completed) {
                    Button(
                        onClick = { viewModel.runSemanticScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("semantic_scan_button")
                    ) {
                        Text("Analyze", fontSize = 12.sp)
                    }
                }
            }

            // Scanning progress
            if (isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scan complete results
            if (completed) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$matchPercentage%",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cross-Cloud Semantic Overlap: $matchPercentage%",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "AI suggests archiving identical reports and unused duplicates in secondary GDrive spaces.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(onClick = { viewModel.resetSemanticScan() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear analysis")
                        }
                    }
                }
            }
        }
    }
}

// --- Cloud File Item ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CloudFileItem(
    file: FileEntity,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelection: () -> Unit,
    sizeFormatter: (Long) -> String
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else Color.Transparent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onToggleSelection,
                onLongClick = onToggleSelection
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(file.categoryId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${sizeFormatter(file.size)} • ${file.path}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GitHubSyncPanel(
    viewModel: CloudViewModel,
    modifier: Modifier = Modifier
) {
    val commits by viewModel.commitsState.collectAsState()
    val uncommittedChangesCount by viewModel.uncommittedChangesCount.collectAsState()
    val isCommitting by viewModel.isCommitting.collectAsState()

    var commitMessageInput by remember { mutableStateOf("") }
    var showAllCommits by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Git",
                        color = MaterialTheme.colorScheme.background,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "epostvvf-gif/THE-VVF-AI-FILE-MANAGER-",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "main",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uncommittedChangesCount > 0) "$uncommittedChangesCount changes staged" else "Up to date with remote",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uncommittedChangesCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uncommittedChangesCount > 0) {
                OutlinedTextField(
                    value = commitMessageInput,
                    onValueChange = { commitMessageInput = it },
                    label = { Text("Commit Message") },
                    placeholder = { Text("Describe what changed (e.g. Added vacation pics)") },
                    modifier = Modifier.fillMaxWidth().testTag("commit_message_input"),
                    singleLine = true,
                    enabled = !isCommitting,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (commitMessageInput.isNotBlank()) {
                                viewModel.createGitCommit(
                                    message = commitMessageInput,
                                    affectedFiles = listOf("Nature_Camping_Photo.jpg", "Travel_Docs.pdf")
                                )
                                commitMessageInput = ""
                            }
                        },
                        enabled = commitMessageInput.isNotBlank() && !isCommitting,
                        modifier = Modifier.testTag("commit_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isCommitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Staging...")
                        } else {
                            Text("Commit changes")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Sync success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Local workspace is fully tracked!",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "All local creations & modifications have been committed to history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Commit History logs",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    val hasUnpushed = commits.any { !it.isPushed }
                    if (hasUnpushed) {
                        TextButton(onClick = { viewModel.pushCommitsToGitHub() }) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push")
                        }
                    }
                    TextButton(onClick = { viewModel.clearCommitHistory() }) {
                        Text("Reset logs", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (commits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No commits in history. Make changes or reset logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val displayCommits = if (showAllCommits) commits else commits.take(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayCommits.forEach { commit ->
                        CommitListItem(commit = commit)
                    }

                    if (commits.size > 3) {
                        TextButton(
                            onClick = { showAllCommits = !showAllCommits },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(if (showAllCommits) "Show Less" else "Show All (${commits.size})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudSyncQueuePanel(
    viewModel: CloudViewModel,
    modifier: Modifier = Modifier
) {
    val syncQueue by viewModel.syncQueueState.collectAsState()
    val isProcessing by viewModel.isProcessingSync.collectAsState()
    val syncMsg by viewModel.syncMessage.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Sync Queue",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cloud Sync Queue",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Room DB persisted cloud operations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                if (syncQueue.isNotEmpty() && !isProcessing) {
                    Button(
                        onClick = { viewModel.processSyncQueue() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("process_sync_queue_button")
                    ) {
                        Text("Sync Now", fontSize = 12.sp)
                    }
                }
            }

            if (syncMsg != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = syncMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (syncQueue.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending synchronization actions. Local and cloud files are perfectly matched!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pending Actions (${syncQueue.size}):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    syncQueue.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.actionType == "ADD") Icons.Rounded.Add else Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = if (item.actionType == "ADD") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (item.actionType == "ADD") "Upload File Action [ID: ${item.fileId}]"
                                       else "Delete File Action [ID: ${item.fileId}]",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = android.text.format.DateUtils.getRelativeTimeSpanString(
                                    item.timestamp,
                                    System.currentTimeMillis(),
                                    android.text.format.DateUtils.SECOND_IN_MILLIS
                                ).toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommitListItem(commit: CommitEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = commit.commitHash,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = commit.author,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (commit.isPushed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (commit.isPushed) "Pushed" else "Local only",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (commit.isPushed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = commit.message,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Files: ${commit.filesAffected}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = android.text.format.DateUtils.getRelativeTimeSpanString(
                    commit.timestamp,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
                ).toString(),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
