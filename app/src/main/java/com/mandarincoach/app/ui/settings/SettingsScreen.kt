package com.mandarincoach.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.preferences.UserPreferences
import com.mandarincoach.app.service.TextToSpeechService
import com.mandarincoach.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val tts = remember { TextToSpeechService(context) }

    val savedApiKey by prefs.apiKey.collectAsState(initial = "")
    val savedSpeed by prefs.speechSpeed.collectAsState(initial = 0.85f)
    val savedShowEnglish by prefs.showEnglish.collectAsState(initial = true)

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var speechSpeed by remember(savedSpeed) { mutableFloatStateOf(savedSpeed) }
    var showEnglish by remember(savedShowEnglish) { mutableStateOf(savedShowEnglish) }
    var showApiKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = { Text("设置 Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWarm)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // API Key section
            SettingsSection(title = "Claude API Key") {
                Text(
                    text = "Get your free API key at console.anthropic.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it; saveSuccess = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") },
                    placeholder = { Text("sk-ant-api03-…", color = TextHint) },
                    visualTransformation = if (showApiKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChineseRed,
                        unfocusedBorderColor = SurfaceGray
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                prefs.setApiKey(apiKeyInput.trim())
                                saveSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChineseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Key")
                    }
                    if (saveSuccess) {
                        Text(
                            "✓ Saved",
                            color = ChineseRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Speech speed section
            SettingsSection(title = "Speech Speed · 语速") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐢", fontSize = 20.sp)
                    Slider(
                        value = speechSpeed,
                        onValueChange = { speechSpeed = it },
                        onValueChangeFinished = {
                            scope.launch { prefs.setSpeechSpeed(speechSpeed) }
                        },
                        valueRange = 0.5f..1.5f,
                        steps = 9,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = ChineseRed,
                            activeTrackColor = ChineseRed
                        )
                    )
                    Text("🐇", fontSize = 20.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(speechSpeed * 100).roundToInt()}% speed",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedButton(
                        onClick = { tts.speak("你好，我是你的普通话老师！", speechSpeed) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChineseRed),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Test Voice", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Display section
            SettingsSection(title = "Display · 显示") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Show English translation", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("显示英文翻译", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = showEnglish,
                        onCheckedChange = {
                            showEnglish = it
                            scope.launch { prefs.setShowEnglish(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ChineseRed, checkedTrackColor = ChineseRedLight.copy(alpha = 0.4f))
                    )
                }
            }

            // About section
            SettingsSection(title = "About · 关于") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "普通话练习",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        color = ChineseRed
                    )
                    Text(
                        text = "Mandarin Conversation Coach",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Practice Mandarin Chinese through natural AI conversation. " +
                                "Powered by Claude AI. Conversations adapt to your proficiency " +
                                "level with vocabulary curated for each stage of learning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "加油！Keep going!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChineseRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ChineseRed,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
