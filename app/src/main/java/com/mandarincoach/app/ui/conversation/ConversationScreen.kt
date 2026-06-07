package com.mandarincoach.app.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandarincoach.app.data.model.DictionaryEntry
import com.mandarincoach.app.data.model.Message
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.data.repository.DictionaryRepository
import com.mandarincoach.app.service.SpeechRecognitionService
import com.mandarincoach.app.service.TextToSpeechService
import kotlinx.coroutines.flow.first
import com.mandarincoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Add ExperimentalLayoutApi here
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tts = remember { TextToSpeechService(context) }
    val stt = remember { SpeechRecognitionService() }
    val isListening by stt.isListening.collectAsStateWithLifecycle()
    val recognizedText by stt.recognizedText.collectAsStateWithLifecycle()
    val sttError by stt.error.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Dictionary popup state
    var dictEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDictSheet by remember { mutableStateOf(false) }

    val sttLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        stt.handleActivityResult(result.resultCode, result.data)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            sttLauncher.launch(stt.createRecognizerIntent())
        }
    }

    LaunchedEffect(level) { 
        viewModel.initialize(level)
        
        // Check if profile is empty and guide the user
        val prefs = UserPreferences(context)
        val name = prefs.userName.first()
        if (name.isBlank()) {
            val result = snackbarHostState.showSnackbar(
                message = "Personalize your coach in Settings!",
                actionLabel = "Go",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onSettingsClick()
            }
        }
    }

    LaunchedEffect(recognizedText) {
        recognizedText?.let { text ->
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                stt.clearRecognizedText()
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.messages) {
        val last = uiState.messages.lastOrNull()
        if (last != null && !last.isUser && last.hanzi.isNotBlank()) {
            tts.speak(last.hanzi, uiState.speechSpeed)
        }
    }

    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${level.emoji} ${level.hanzi}  ${level.englishName}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(text = level.pinyin, style = MaterialTheme.typography.bodySmall, color = PinyinColor)
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
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        sttLauncher.launch(stt.createRecognizerIntent())
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Error banner
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let { err ->
                    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = sttError != null) {
                sttError?.let {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp, 8.dp))
                    }
                    LaunchedEffect(it) { kotlinx.coroutines.delay(3000); stt.clearError() }
                }
            }

            // Hint strip
            Surface(color = SurfaceGray, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tap any Chinese character to see its definition  •  点击汉字查看释义",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageRow(
                        message = message,
                        showEnglish = uiState.showEnglish,
                        speechSpeed = uiState.speechSpeed,
                        onSpeak = { tts.speak(message.hanzi, uiState.speechSpeed) },
                        onSpeakSlow = { tts.speak(message.hanzi, uiState.speechSpeed * 0.6f) },
                        onWordTap = { entry ->
                            dictEntry = entry
                            showDictSheet = true
                        }
                    )
                }
                if (uiState.isLoading) {
                    item { TypingIndicator() }
                }
            }
        }
    }

    // Dictionary bottom sheet
    if (showDictSheet && dictEntry != null) {
        ModalBottomSheet(
            onDismissRequest = { showDictSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DictionarySheet(entry = dictEntry!!, onDismiss = { showDictSheet = false })
        }
    }
}

// ── Message row ───────────────────────────────────────────────────────────────

@Composable
private fun MessageRow(
    message: Message,
    showEnglish: Boolean,
    speechSpeed: Float,
    onSpeak: () -> Unit,
    onSpeakSlow: () -> Unit,
    onWordTap: (DictionaryEntry) -> Unit
) {
    if (message.isUser) {
        UserMessageRow(message)
    } else {
        AiMessageRow(
            message = message,
            showEnglish = showEnglish,
            onSpeak = onSpeak,
            onSpeakSlow = onSpeakSlow,
            onWordTap = onWordTap
        )
    }
}

@Composable
private fun AiMessageRow(
    message: Message,
    showEnglish: Boolean,
    onSpeak: () -> Unit,
    onSpeakSlow: () -> Unit,
    onWordTap: (DictionaryEntry) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ChineseRed),
                contentAlignment = Alignment.Center
            ) {
                Text("明", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tappable Hanzi text
                TappableHanziText(
                    hanzi = message.hanzi,
                    onWordTap = onWordTap
                )

                if (message.pinyin.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = EnglishColor,
                        fontStyle = FontStyle.Italic
                    )
                }

                if (message.tip != null) {
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

                // Action buttons
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MessageIconButton(icon = Icons.Default.VolumeUp, label = "Normal speed", onClick = onSpeak)
                    MessageIconButton(icon = Icons.Default.Speed, label = "Slow speed", onClick = onSpeakSlow)
                }
            }
        }
    }
}

@Composable
private fun TappableHanziText(
    hanzi: String,
    onWordTap: (DictionaryEntry) -> Unit
) {
    // Render each character as a tappable span using flow layout
    val charGroups = remember(hanzi) { segmentHanzi(hanzi) }

    // We render using a custom flow-wrap layout via wrapping Row logic
    // For simplicity, use annotated text approach with character-level clicks
    var highlightedIndex by remember { mutableIntStateOf(-1) }

    Column {
        // Build rows of characters (wrap at screen width naturally)
        var currentRowChars = mutableListOf<Pair<Int, String>>() // index + segment
        val rows = mutableListOf<List<Pair<Int, String>>>()
        charGroups.forEach { (idx, seg) ->
            currentRowChars.add(idx to seg)
        }
        rows.add(currentRowChars)

        // Render as a single wrapping line using BoxWithConstraints + Flow
        HanziCharacterFlow(
            segments = charGroups,
            highlightedIndex = highlightedIndex,
            onSegmentTap = { segIndex, segText ->
                highlightedIndex = segIndex
                val entry = DictionaryRepository.lookupWithContext(hanzi, segIndex)
                    ?: DictionaryRepository.lookup(segText)
                if (entry != null) onWordTap(entry)
                else {
                    // Show a "not found" entry
                    onWordTap(
                        DictionaryEntry(
                            hanzi = segText,
                            pinyin = "—",
                            definition = "Not found in dictionary. Try tapping the full word.",
                            hskLevel = 0
                        )
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HanziCharacterFlow(
    segments: List<Pair<Int, String>>,
    highlightedIndex: Int,
    onSegmentTap: (Int, String) -> Unit
) {
    // Use a wrapping FlowRow-like approach with Compose
    // We'll use a custom layout with wrapping Row
    FlowRow(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        segments.forEach { (index, seg) ->
            val isPunct = seg.all { !it.isLetterOrDigit() && it.code < 0x4E00 }
            val isHighlighted = index == highlightedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isHighlighted) ChineseRed.copy(alpha = 0.12f)
                        else Color.Transparent
                    )
                    .then(
                        if (!isPunct) Modifier.clickable { onSegmentTap(index, seg) }
                        else Modifier
                    )
                    .padding(horizontal = 1.dp, vertical = 1.dp)
            ) {
                Text(
                    text = seg,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = if (isHighlighted) ChineseRed else TextPrimary,
                    lineHeight = 32.sp
                )
            }
        }
    }
}

private fun segmentHanzi(hanzi: String): List<Pair<Int, String>> {
    // Break into individual characters, preserving position index for dictionary lookup
    val result = mutableListOf<Pair<Int, String>>()
    hanzi.forEachIndexed { index, ch -> result.add(index to ch.toString()) }
    return result
}

@Composable
private fun UserMessageRow(message: Message) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Pill bubble
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(UserPillColor)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.hanzi,
                fontSize = 17.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        // User action buttons (right-aligned)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            UserActionButton(icon = Icons.Default.Edit, label = "Edit")
            UserActionButton(icon = Icons.Default.Translate, label = "Translate")
            UserActionButton(icon = Icons.Default.PlayArrow, label = "Play")
        }
    }
}

@Composable
private fun MessageIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = SurfaceGray,
            contentColor = TextSecondary
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun UserActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit = {}) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = SurfaceGray,
            contentColor = TextSecondary
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
    }
}

// ── Typing indicator ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(ChineseRed),
            contentAlignment = Alignment.Center
        ) {
            Text("明", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.Serif)
        }
        Spacer(Modifier.width(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.5f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, delayMillis = index * 160, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ), label = "s$index"
                )
                Box(
                    modifier = Modifier.size(10.dp).scale(scale)
                        .clip(CircleShape).background(ChineseRedLight)
                )
            }
        }
    }
}

// ── Dictionary bottom sheet ───────────────────────────────────────────────────

@Composable
private fun DictionarySheet(entry: DictionaryEntry, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape)
                .background(TextHint).align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            // Large character display
            Text(
                text = entry.hanzi,
                fontSize = 56.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                color = TextPrimary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                if (entry.hskLevel > 0) {
                    Surface(
                        color = ChineseRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "HSK ${entry.hskLevel}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                if (entry.partOfSpeech.isNotBlank()) {
                    Text(
                        text = entry.partOfSpeech,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // Pinyin
        Text(
            text = entry.pinyin,
            fontSize = 22.sp,
            color = PinyinColor,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(12.dp))

        HorizontalDivider(color = SurfaceGray)
        Spacer(Modifier.height(12.dp))

        // Definition
        Text(
            text = "Definition",
            style = MaterialTheme.typography.labelSmall,
            color = ChineseRed,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.definition,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )

        // Examples
        if (entry.examples.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Examples · 例句",
                style = MaterialTheme.typography.labelSmall,
                color = ChineseRed,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            entry.examples.forEach { (hanziEx, englishEx) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = hanziEx,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = englishEx,
                        style = MaterialTheme.typography.bodySmall,
                        color = EnglishColor,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

// ── Input bar ─────────────────────────────────────────────────────────────────

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isListening: Boolean,
    isLoading: Boolean,
    onSend: () -> Unit,
    onMicClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = SurfaceWhite) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop" else "Speak",
                    modifier = Modifier.size(22.dp)
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (isListening) "Listening…" else "Type in Chinese or English…",
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
                )
            )

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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
