package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.FileEntity
import com.example.viewmodel.FileViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFileManagerScreen(
    viewModel: FileViewModel,
    modifier: Modifier = Modifier
) {
    val localFiles by viewModel.localFilesState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedIds by viewModel.selectedFileIds.collectAsState()
    val isMultiSelect by viewModel.isMultiSelectMode.collectAsState()

    var showSafeFolderDialog by remember { mutableStateOf(false) }

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
                        text = "Local Storage",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage and clean your storage space",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Safe Folder Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SortMenu(viewModel = viewModel)
                    Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showSafeFolderDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .testTag("safe_folder_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Safe Folder",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // --- Hero Banner ---
            Image(
                painter = painterResource(id = R.drawable.img_local_banner_1783686640266),
                contentDescription = "Local Storage Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Multi-select Toolbar ---
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
                            text = "${selectedIds.size} files selected",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row {
                            TextButton(onClick = { viewModel.selectAllFiles() }) {
                                Text("Select All", color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { viewModel.moveSelectedToSafe() },
                                modifier = Modifier.testTag("move_to_safe_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Move to Safe",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteSelectedFiles() },
                                modifier = Modifier.testTag("delete_selected_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // --- Search Field ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_search_input"),
                placeholder = { Text("Search files by name...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
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

            // --- Categories chips ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectedCategory.value = null },
                        label = { Text("All Files") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = { viewModel.selectedCategory.value = cat.id },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat.iconName),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Dynamic Scanner & Cleaner Panels ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { StorageDashboard(files = localFiles) }

                // Duplicate Scanner Card
                item { DuplicateScannerCard(viewModel = viewModel) }

                // Junk Cleaner Card
                item { JunkCleanerCard(viewModel = viewModel) }

                // File List Header
                item {
                    Text(
                        text = "File Catalog",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // File List
                if (localFiles.isEmpty()) {
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
                                    text = "No local files found matching criteria.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(localFiles, key = { it.id }) { file ->
                        LocalFileItem(
                            file = file,
                            isSelected = selectedIds.contains(file.id),
                            isMultiSelectMode = isMultiSelect,
                            onToggleSelection = { viewModel.toggleFileSelection(file.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Secure Safe Folder Dialog ---
    if (showSafeFolderDialog) {
        SecureSafeFolderDialog(
            viewModel = viewModel,
            onDismiss = {
                showSafeFolderDialog = false
                viewModel.lockSecureFolder()
                viewModel.resetPinSetup()
            }
        )
    }
}

// --- Duplicate Scanner Card ---
@Composable
fun DuplicateScannerCard(viewModel: FileViewModel) {
    val isScanning by viewModel.isScanningDuplicates.collectAsState()
    val progress by viewModel.scanProgress.collectAsState()
    val completed by viewModel.scanCompleted.collectAsState()
    val duplicates by viewModel.duplicatesState.collectAsState()
    val exactDups by viewModel.exactDuplicatesState.collectAsState()
    val nearDups by viewModel.nearDuplicatesState.collectAsState()
    val semanticDups by viewModel.semanticDuplicatesState.collectAsState()

    var showResultList by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Duplicate scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Duplicate File Scanner",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Zero-false-positive scan via byte-level SHA-256 verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                if (!isScanning && !completed) {
                    Button(
                        onClick = { viewModel.scanForDuplicates() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("scan_duplicates_button")
                    ) {
                        Text("Scan", fontSize = 12.sp)
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
                        color = MaterialTheme.colorScheme.primary
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (duplicates.isEmpty()) "No duplicates found!" else "Found ${duplicates.size} duplicate files!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (duplicates.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (duplicates.isNotEmpty()) {
                            val totalSize = duplicates.sumOf { it.size }
                            Text(
                                text = "Wasting ${formatSize(totalSize)} of storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        if (duplicates.isNotEmpty()) {
                            TextButton(onClick = { showResultList = !showResultList }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (showResultList) "Hide Details" else "View Details", fontSize = 12.sp)
                                    Icon(
                                        imageVector = if (showResultList) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.scanForDuplicates() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Rescan", fontSize = 12.sp)
                        }
                    }
                }

                // Show details lists
                AnimatedVisibility(visible = showResultList && (exactDups.isNotEmpty() || nearDups.isNotEmpty() || semanticDups.isNotEmpty())) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        // 1. Exact Duplicates (SHA-256)
                        if (exactDups.isNotEmpty()) {
                            Text(
                                text = "Exact Duplicates (SHA-256)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            exactDups.forEachIndexed { index, group ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Duplicate Group #${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        group.forEach { dup ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = dup.name,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        text = "${formatSize(dup.size)} • ${dup.path}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteDuplicate(dup) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete Copy",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Near Duplicates (Perceptual Signature)
                        if (nearDups.isNotEmpty()) {
                            Text(
                                text = "Near Duplicates (Perceptual Hash)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            nearDups.forEachIndexed { index, group ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Similar Media Group #${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        group.forEach { dup ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = dup.name,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        text = "${formatSize(dup.size)} • ${dup.path}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteDuplicate(dup) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete Copy",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Semantic Duplicates (Concept/Embeddings)
                        if (semanticDups.isNotEmpty()) {
                            Text(
                                text = "Semantic Duplicates (Embeddings)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            semanticDups.forEachIndexed { index, group ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Concept Match Group #${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        group.forEach { dup ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = dup.name,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        text = "${formatSize(dup.size)} • ${dup.path}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteDuplicate(dup) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete Copy",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Junk Cleaner Card ---
@Composable
fun JunkCleanerCard(viewModel: FileViewModel) {
    val isCleaning by viewModel.isCleaningJunk.collectAsState()
    val isCleanedSuccess by viewModel.junkCleanedSuccess.collectAsState()
    val junkFiles by viewModel.junkFilesState.collectAsState()

    val totalJunkSize = junkFiles.sumOf { it.size }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Junk cleaner",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Storage Optimizer & Junk Cleaner",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (totalJunkSize > 0) "System cache and logs waste space" else "Your system is clean and optimized",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isCleaning && !isCleanedSuccess && totalJunkSize > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatSize(totalJunkSize),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cleanable cache/log files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.cleanJunk() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("clean_junk_button")
                    ) {
                        Text("Clean Now")
                    }
                }
            }

            if (isCleaning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cleaning temporary systems and residual junk logs...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isCleanedSuccess) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Cleaned",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Storage optimized successfully!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    TextButton(onClick = { viewModel.resetJunkCleanState() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

// --- Local File Item ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalFileItem(
    file: FileEntity,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelection: () -> Unit
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
        // Selector Checkbox
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
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(file.categoryId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                text = "${formatSize(file.size)} • ${file.mimeType.substringAfter("/")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// --- Secure Safe Folder Dialog ---
@Composable
fun SecureSafeFolderDialog(
    viewModel: FileViewModel,
    onDismiss: () -> Unit
) {
    val secureState by viewModel.secureState.collectAsState()
    val isUnlocked by viewModel.secureFolderUnlocked.collectAsState()
    val pinSetupStep by viewModel.pinSetupStep.collectAsState()
    val firstPin by viewModel.firstEnteredPin.collectAsState()
    val error by viewModel.pinError.collectAsState()
    val safeFiles by viewModel.safeFilesState.collectAsState()

    var pinInput by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Secure PIN Safe",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Case 1: PIN NOT SET UP YET (setup state)
                if (secureState?.pin == null) {
                    Text(
                        text = "Secure Folder Setup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (pinSetupStep == 1) "Enter a new 4-digit PIN to secure your private files:"
                               else "Re-enter the 4-digit PIN to confirm setup:",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Enter 4-digit PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_pin_input")
                    )

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.submitPinSetup(pinInput)
                            pinInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_setup_pin_button")
                    ) {
                        Text("Continue")
                    }
                }
                // Case 2: PIN IS SETUP BUT LOCKED
                else if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Folder Locked",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Enter your secure PIN to access private files:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Enter PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_pin_input")
                    )

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.verifyPinToUnlock(pinInput)
                            pinInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_pin_button")
                    ) {
                        Text("Unlock Folder")
                    }
                }
                // Case 3: UNLOCKED - Show secure files!
                else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = "Unlocked",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safe Folder Unlocked",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        Button(
                            onClick = { viewModel.lockSecureFolder() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lock Safe", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (safeFiles.isEmpty()) {
                        Text(
                            text = "No private files locked yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(safeFiles) { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = formatSize(file.size),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { viewModel.restoreSelectedFromSafe(listOf(file.id)) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FolderSpecial,
                                                contentDescription = "Restore File",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteFileById(file.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete Permanently",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Icon Selector ---
fun getCategoryIcon(name: String): ImageVector {
    return when (name.uppercase()) {
        "IMAGES" -> Icons.Rounded.Image
        "VIDEOS" -> Icons.Rounded.Videocam
        "DOCUMENTS" -> Icons.Rounded.Description
        "MUSIC" -> Icons.Rounded.Audiotrack
        "APKS" -> Icons.Rounded.Android
        "JUNK" -> Icons.Rounded.Delete
        else -> Icons.Rounded.Folder
    }
}

// --- Format Size Helper ---
fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
