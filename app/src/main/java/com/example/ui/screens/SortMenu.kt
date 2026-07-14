package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.domain.usecase.SortOption
import com.example.viewmodel.FileViewModel

@Composable
fun SortMenu(viewModel: FileViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sort by Name") },
                onClick = {
                    viewModel.setSortOption(SortOption.NAME)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Semantic Relevance") },
                onClick = {
                    viewModel.setSortOption(SortOption.SEMANTIC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Date Modified") },
                onClick = {
                    viewModel.setSortOption(SortOption.DATE)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Size") },
                onClick = {
                    viewModel.setSortOption(SortOption.SIZE)
                    expanded = false
                }
            )
        }
    }
}
