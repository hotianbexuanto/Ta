package com.hotian.ta.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.hotian.ta.data.Message
import com.hotian.ta.data.MessageType
import com.hotian.ta.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToGroupList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val currentGroupId by viewModel.currentGroupId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(messages.size) {
        println("ChatScreen: messages size changed to ${messages.size}")
        if (messages.isNotEmpty() && !isSearching) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 调试：监控currentGroupId变化
    LaunchedEffect(currentGroupId) {
        println("ChatScreen: currentGroupId changed to $currentGroupId")
    }

    val currentGroup = groups.find { it.id == currentGroupId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchMessages(it) },
                            placeholder = { Text("搜索消息...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(currentGroup?.name ?: "Ta - 本地记录")
                    }
                },
                navigationIcon = {
                    if (showSearchBar) {
                        IconButton(onClick = {
                            showSearchBar = false
                            viewModel.clearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭搜索",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateToGroupList) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "群组列表",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    EnhancedMessageItem(
                        message = message,
                        viewModel = viewModel
                    )
                }
            }

            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "选择的图片",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(Icons.Default.Close, "移除图片")
                        }
                    }
                }
            }

            EnhancedMessageInputField(
                value = inputText,
                onValueChange = { inputText = it },
                hasImage = selectedImageUri != null,
                onSend = {
                    if (selectedImageUri != null) {
                        viewModel.sendMessage(
                            content = inputText.ifBlank { "图片" },
                            type = MessageType.IMAGE,
                            attachmentUri = selectedImageUri
                        )
                        selectedImageUri = null
                    } else if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                    }
                    inputText = ""
                },
                onImageClick = { imagePickerLauncher.launch("image/*") }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedMessageItem(
    message: Message,
    viewModel: ChatViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (message.type == MessageType.IMAGE.name && message.attachmentUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(message.attachmentUri)),
                        contentDescription = "消息图片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (message.content.isNotBlank() && message.content != "图片") {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (message.isEdited) {
                        Text(
                            text = "已编辑",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (message.type == MessageType.TEXT.name) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            showEditDialog = true
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showEditDialog) {
        EditMessageDialog(
            message = message,
            onDismiss = { showEditDialog = false },
            onConfirm = { newContent ->
                viewModel.editMessage(message, newContent)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除消息") },
            text = { Text("确定要删除这条消息吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage(message.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun EditMessageDialog(
    message: Message,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(message.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑消息") },
        text = {
            TextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(editedText) },
                enabled = editedText.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EnhancedMessageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hasImage: Boolean,
    onSend: () -> Unit,
    onImageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onImageClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "选择图片",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            placeholder = { Text("输入消息...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() || hasImage
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "发送",
                tint = if (value.isNotBlank() || hasImage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
