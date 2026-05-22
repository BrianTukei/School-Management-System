package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import com.example.ui.SchoolViewModel.ChatMessage
import androidx.compose.ui.text.withStyle

@Composable
fun AiAssistantScreen(viewModel: SchoolViewModel) {
    val chatHistory by viewModel.aiChatHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.aiIsLoading.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll to bottom when new entries arrive
    LaunchedEffect(chatHistory.size, isLoading) {
        if (chatHistory.isNotEmpty() || isLoading) {
            val lastIdx = if (isLoading) chatHistory.size else chatHistory.size - 1
            if (lastIdx >= 0) {
                listState.animateScrollToItem(lastIdx)
            }
        }
    }

    val suggestions = listOf(
        "Current total fees remaining?",
        "How many students are registered?",
        "Summary of faculty?",
        "Who is on leave today?",
        "Calculate average academic score?",
        "List all classes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Copilot Intro Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI Senior Copilot System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Ask any question statistics, trends, directories, or balances.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                if (chatHistory.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAiChat() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Dialog space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatHistory.isEmpty()) {
                // Empty state landing layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Pearl Junior School AI Copilot",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ask me anything about current registers, accounting, grades, faculty or attendance data. I am fully integrated into every module of our school software.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chatHistory) { msg ->
                        ChatBubble(message = msg)
                    }
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Copilot is analyzing details...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // suggestions shelfs
        if (!isLoading) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(suggestions) { sugg ->
                    SuggestionActionChip(text = sugg, onClick = {
                        viewModel.sendPromptToAI(sugg)
                    })
                }
            }
        }

        // Input frame
        Surface(
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything about Pearl Junior...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isLoading) {
                                viewModel.sendPromptToAI(inputText.trim())
                                inputText = ""
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            viewModel.sendPromptToAI(inputText.trim())
                            inputText = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.isUser) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                } else {
                    MarkdownText(text = message.message)
                }
            }
        }
    }
}

@Composable
fun SuggestionActionChip(text: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MarkdownText(text: String) {
    val annotatedString = remember(text) {
        androidx.compose.ui.text.buildAnnotatedString {
            var lastMatchIndex = 0
            val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
            
            boldPattern.findAll(text).forEach { match ->
                // Content before match
                if (match.range.first > lastMatchIndex) {
                    append(text.substring(lastMatchIndex, match.range.first))
                }
                // Bold match
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                lastMatchIndex = match.range.last + 1
            }
            if (lastMatchIndex < text.length) {
                append(text.substring(lastMatchIndex))
            }
        }
    }
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
