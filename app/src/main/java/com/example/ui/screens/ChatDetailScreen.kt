package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.ChatEntity
import com.example.data.entity.MessageEntity
import com.example.ui.components.AttachmentOptionItem
import com.example.ui.components.MeeChaatAvatar
import com.example.ui.components.MessageStatusTicks
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.BubbleReceivedDark
import com.example.ui.theme.BubbleReceivedLight
import com.example.ui.theme.BubbleSentDark
import com.example.ui.theme.BubbleSentLight
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.theme.WhatsAppTopBarDark
import com.example.ui.viewmodel.MeeChaatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: MeeChaatViewModel,
    modifier: Modifier = Modifier
) {
    val chat: ChatEntity? by viewModel.getChat(chatId).collectAsState(initial = null)
    val messages: List<MessageEntity> by viewModel.getMessages(chatId).collectAsState(initial = emptyList())

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableIntStateOf(0) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageEntity?>(null) }

    val listState = rememberLazyListState()
    val isDark = isSystemInDarkTheme()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Live voice note timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingTimer = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingTimer++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* View Contact */ }
                    ) {
                        chat?.let {
                            MeeChaatAvatar(
                                name = it.name,
                                size = 40.dp,
                                showOnlineBadge = it.onlineStatus == "online",
                                isGroup = it.isGroup
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = it.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = it.onlineStatus,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { chat?.let { viewModel.startCall(it.name, isVideo = true) } },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                    }
                    IconButton(
                        onClick = { chat?.let { viewModel.startCall(it.name, isVideo = false) } },
                        modifier = Modifier.testTag("chat_audio_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View contact") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Media, links, and docs") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Search") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Mute notifications") },
                                onClick = {
                                    chat?.let { viewModel.toggleMute(it) }
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear chat") },
                                onClick = {
                                    viewModel.clearChat(chatId)
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) WhatsAppTopBarDark else WhatsAppGreen
                )
            )
        },
        modifier = modifier.imePadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Authentic Doodle Background
            Image(
                painter = painterResource(id = R.drawable.bg_chat_doodle),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.08f else 0.22f,
                modifier = Modifier.fillMaxSize()
            )

            // Message List
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    // Encryption notice banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF182229) else Color(0xFFFEF6D9),
                                tonalElevation = 1.dp
                            ) {
                                Text(
                                    text = "🔒 Messages and calls are end-to-end encrypted. No one outside of this chat, not even MEECHAAT, can read or listen to them.",
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = if (isDark) Color(0xFFFFD279) else Color(0xFF5E4E00),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Date divider
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1F2C34) else Color(0xFFE1F3FB),
                                tonalElevation = 1.dp
                            ) {
                                Text(
                                    text = "TODAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Messages
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isDark = isDark,
                            onLongClick = { selectedMessageForReaction = msg }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Quick Emoji bar if toggled
                AnimatedVisibility(visible = showEmojiPicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("😀", "😂", "❤️", "🔥", "👍", "🙏", "🎉", "😍", "🥳", "☕").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        inputText += emoji
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Bottom Input Row
                BottomInputRow(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    isRecording = isRecordingVoice,
                    recordingTimer = recordingTimer,
                    showEmojiPicker = showEmojiPicker,
                    onToggleEmoji = { showEmojiPicker = !showEmojiPicker },
                    onAttachmentClick = { showAttachmentSheet = true },
                    onCameraClick = {
                        viewModel.sendMessage(chatId, "📷 Shared photo", type = "IMAGE")
                    },
                    onSend = {
                        viewModel.sendMessage(chatId, inputText, type = "TEXT")
                        inputText = ""
                    },
                    onStartVoice = { isRecordingVoice = true },
                    onCancelVoice = { isRecordingVoice = false },
                    onSendVoice = {
                        val duration = if (recordingTimer > 0) recordingTimer else 3
                        viewModel.sendMessage(
                            chatId,
                            "🎤 Voice message",
                            type = "VOICE",
                            durationSeconds = duration
                        )
                        isRecordingVoice = false
                    }
                )
            }
        }
    }

    // Attachment Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.InsertDriveFile,
                        label = "Document",
                        backgroundColor = Color(0xFF7F66FF),
                        onClick = {
                            viewModel.sendMessage(chatId, "📄 Project_Requirements.pdf", type = "DOCUMENT")
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        backgroundColor = Color(0xFFD33682),
                        onClick = {
                            viewModel.sendMessage(chatId, "📷 Instant photo", type = "IMAGE")
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.PhotoLibrary,
                        label = "Gallery",
                        backgroundColor = Color(0xFFAC44CF),
                        onClick = {
                            viewModel.sendMessage(chatId, "🖼️ Vacation picture", type = "IMAGE")
                            showAttachmentSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.Headphones,
                        label = "Audio",
                        backgroundColor = Color(0xFFFF6F00),
                        onClick = {
                            viewModel.sendMessage(chatId, "🎵 Audio Track 01", type = "VOICE", durationSeconds = 34)
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        backgroundColor = Color(0xFF1BA358),
                        onClick = {
                            viewModel.sendMessage(chatId, "📍 Live Location (Downtown Café)", type = "LOCATION")
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.Poll,
                        label = "Poll",
                        backgroundColor = Color(0xFF00BFA5),
                        onClick = {
                            viewModel.sendMessage(chatId, "📊 Poll: Where should we have dinner tonight?", type = "TEXT")
                            showAttachmentSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Reaction Picker Modal
    selectedMessageForReaction?.let { msg ->
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForReaction = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "React to message",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("❤️", "👍", "😂", "😮", "😢", "🙏", "🔥").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.setReaction(msg.id, emoji)
                                    selectedMessageForReaction = null
                                }
                                .padding(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isDark: Boolean,
    onLongClick: () -> Unit
) {
    val isMe = message.senderId == "me"
    val bubbleColor = if (isMe) {
        if (isDark) BubbleSentDark else BubbleSentLight
    } else {
        if (isDark) BubbleReceivedDark else BubbleReceivedLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMe) 12.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
                // Voice Message UI
                if (message.type == "VOICE") {
                    VoiceMessagePlayer(durationSeconds = message.durationSeconds)
                } else if (message.type == "IMAGE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Photo",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = message.text, fontSize = 14.sp)
                } else if (message.type == "DOCUMENT") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Document",
                            tint = Color(0xFF7F66FF),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.text,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (message.type == "LOCATION") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = message.text, fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = message.text,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Timestamp and Check status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusTicks(status = message.status, size = 13.dp)
                    }
                }
            }
        }

        // Message Reaction Badge
        message.reaction?.let { reaction ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(start = 2.dp, top = 12.dp)
            ) {
                Text(
                    text = reaction,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceMessagePlayer(durationSeconds: Int) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.3f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WhatsAppGreen)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    thumbColor = WhatsAppGreen,
                    activeTrackColor = WhatsAppGreenLight
                ),
                modifier = Modifier.height(18.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPlaying) "0:0${(durationSeconds * progress).toInt()}" else "0:${if (durationSeconds < 10) "0$durationSeconds" else "$durationSeconds"}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "1x",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppGreen
                )
            }
        }
    }
}

@Composable
fun BottomInputRow(
    text: String,
    onTextChanged: (String) -> Unit,
    isRecording: Boolean,
    recordingTimer: Int,
    showEmojiPicker: Boolean,
    onToggleEmoji: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSend: () -> Unit,
    onStartVoice: () -> Unit,
    onCancelVoice: () -> Unit,
    onSendVoice: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            // Live Voice Recording State
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Recording",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "0:${if (recordingTimer < 10) "0$recordingTimer" else "$recordingTimer"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "< Slide to cancel",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancel",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable(onClick = onCancelVoice)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Voice Send Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreenLight)
                    .clickable(onClick = onSendVoice)
                    .testTag("send_voice_note_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send voice note",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            // Normal Text Input State
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleEmoji) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Emoji",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = text,
                        onValueChange = onTextChanged,
                        placeholder = { Text("Message", fontSize = 15.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    IconButton(onClick = onAttachmentClick) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (text.isEmpty()) {
                        IconButton(onClick = onCameraClick) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Dynamic Action Button (Send Arrow or Mic)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreenLight)
                    .clickable {
                        if (text.isNotBlank()) {
                            onSend()
                        } else {
                            onStartVoice()
                        }
                    }
                    .testTag(if (text.isNotBlank()) "send_text_button" else "mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (text.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = if (text.isNotBlank()) "Send" else "Record Voice Note",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
