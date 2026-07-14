package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.viewmodel.AiViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun AiAssistantScreen(
    viewModel: AiViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val isHighThinking by viewModel.isHighThinkingMode.collectAsState()
    val isExpanded by viewModel.isSetupPanelExpanded.collectAsState()
    val apiKeyInput by viewModel.apiKeyInput.collectAsState()
    val validationMessage by viewModel.validationMessage.collectAsState()
    
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var showPassword by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll chat to the last item whenever messages list updates
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

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
                        text = "AI Storage Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Optimize folders conceptually with Gemini",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Delete Sweep to Clear History
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear Chat History",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // --- Hero Banner ---
            Image(
                painter = painterResource(id = R.drawable.img_ai_banner_1783686677471),
                contentDescription = "AI Assistant Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Collapsible Setup Panel ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSetupPanel() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gemini AI Core Configuration",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (apiKey.isNullOrBlank()) "API Key Pending Configuration" else "AI Active & Configured",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (apiKey.isNullOrBlank()) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Toggle"
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // API Key Input
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { viewModel.apiKeyInput.value = it },
                                label = { Text("Gemini API Key") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_api_key_input"),
                                placeholder = { Text("AIzaSy...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle Visibility"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // High-thinking mode switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "High-Thinking Mode",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Uses Gemini 3.1 Pro preview for deeper query reasoning and file layout suggestions.",
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isHighThinking,
                                    onCheckedChange = { viewModel.toggleHighThinkingMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("high_thinking_toggle")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Feedback / validation message
                            if (validationMessage != null) {
                                Text(
                                    text = validationMessage!!,
                                    color = if (validationMessage!!.contains("Error") || validationMessage!!.contains("Please")) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Actions
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (!apiKey.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { viewModel.removeApiKey() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Remove Key")
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Button(
                                    onClick = { viewModel.applyApiKey(apiKeyInput) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("apply_key_button")
                                ) {
                                    Text("Apply Key")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Chat Messages Pane ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message = message)
                    }

                    if (isAiLoading) {
                        item {
                            AiLoadingBubble()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Input Field and Send Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Ask about files, duplicates, optimizer...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            viewModel.sendMessage(chatInput)
                            chatInput = ""
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(48.dp)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send prompt",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// --- Chat Bubble Composable ---
@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(containerColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = message.text,
                    color = contentColor,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// --- AI Loading Bubble ---
@Composable
fun AiLoadingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI is thinking and typing...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
