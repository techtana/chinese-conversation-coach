package com.mandarincoach.app.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandarincoach.app.data.model.Message
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.service.SpeechRecognitionService
import com.mandarincoach.app.service.TextToSpeechService
import com.mandarincoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    level: ProficiencyLevel,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ConversationViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val tts = remember { TextToSpeechService(context) }
    val stt = remember { SpeechRecognitionService(context) }
    val isSpeaking by tts.isSpeaking.collectAsStateWithLifecycle()
    val isListening by stt.isListening.collectAsStateWithLifecycle()
    val recognizedText by stt.recognizedText.collectAsStateWithLifecycle()
    val sttError by stt.error.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) stt.startListening()
    }

    // Initialize conversation
    LaunchedEffect(level) { viewModel.initialize(level) }

    // Handle speech recognition result
    LaunchedEffect(recognizedText) {
        recognizedText?.let { text ->
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                stt.clearRecognizedText()
            }
        }
    }

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Auto-speak AI responses
    LaunchedEffect(uiState.messages) {
        val last = uiState.messages.lastOrNull()
        if (last != null && !last.isUser && last.hanzi.isNotBlank()) {
            tts.speak(last.hanzi, uiState.speechSpeed)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
            stt.destroy()
        }
    }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${level.emoji} ${level.hanzi} · ${level.englishName}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = level.pinyin,
                            style = MaterialTheme.typography.bodySmall,
                            color = PinyinColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "New conversation", tint = TextSecondary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWarm)
            )
        },
        bottomBar = {
            InputBar(
                text = inputText,
                onTextChange = { inputText = it },
                isListening = isListening,
                isLoading = uiState.isLoading,
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        focusManager.clearFocus()
                    }
                },
                onMicClick = {
                    focusManager.clearFocus()
                    if (isListening) {
                        stt.stopListening()
                    } else {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) stt.startListening()
                        else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Error banner
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // STT error
            AnimatedVisibility(visible = sttError != null) {
                sttError?.let {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp, 8.dp)
                        )
                    }
                    LaunchedEffect(it) { kotlinx.coroutines.delay(3000); stt.clearError() }
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        showEnglish = uiState.showEnglish,
                        onSpeak = { tts.speak(message.hanzi, uiState.speechSpeed) }
                    )
                }

                if (uiState.isLoading) {
                    item { TypingIndicator() }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    showEnglish: Boolean,
    onSpeak: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            // Coach avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ChineseRed),
                contentAlignment = Alignment.Center
            ) {
                Text("明", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Serif)
            }
            Spacer(Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) UserBubble else AiBubble
            ),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 20.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.isUser) {
                    Text(
                        text = message.hanzi,
                        style = MaterialTheme.typography.bodyLarge,
                        color = UserBubbleText
                    )
                } else {
                    // Hanzi
                    Text(
                        text = message.hanzi,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        lineHeight = 32.sp
                    )

                    if (message.pinyin.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = message.pinyin,
                            fontSize = 14.sp,
                            color = PinyinColor,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        )
                    }

                    if (showEnglish && message.english.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message.english,
                            style = MaterialTheme.typography.bodySmall,
                            color = EnglishColor,
                            lineHeight = 18.sp
                        )
                    }

                    if (message.tip != null) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = SurfaceGray)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Text("💡 ", fontSize = 13.sp)
                            Text(
                                text = message.tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = TipColor,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                tint = TextHint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (message.isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ChineseRed),
            contentAlignment = Alignment.Center
        ) {
            Text("明", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Serif)
        }
        Spacer(Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = AiBubble),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, delayMillis = index * 150, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ), label = "scale$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(ChineseRedLight)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isListening: Boolean,
    isLoading: Boolean,
    onSend: () -> Unit,
    onMicClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mic button
            val micScale by animateFloatAsState(
                targetValue = if (isListening) 1.15f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "micScale"
            )
            FilledIconButton(
                onClick = onMicClick,
                modifier = Modifier.size(48.dp).scale(micScale),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening) ListeningRed else SurfaceGray,
                    contentColor = if (isListening) Color.White else TextSecondary
                )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Speak",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (isListening) "Listening… 听着呢…" else "Type or speak…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHint
                    )
                },
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChineseRed,
                    unfocusedBorderColor = SurfaceGray,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = BackgroundWarm
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // Send button
            FilledIconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ChineseRed,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
