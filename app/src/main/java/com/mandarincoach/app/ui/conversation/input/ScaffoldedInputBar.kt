package com.mandarincoach.app.ui.conversation.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandarincoach.app.data.model.ChoiceOption
import com.mandarincoach.app.ui.theme.*

private val TRAILING_ENGLISH = Regex("""[A-Za-z][A-Za-z']*$""")

/**
 * Stage 3 (Scaffolded Production): free typing plus an inline EN→中
 * assist — when the learner's draft ends in an English word, a chip
 * offers to translate just that word and splice it into the draft.
 */
@Composable
fun ScaffoldedInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isListening: Boolean,
    isLoading: Boolean,
    isTranslating: Boolean,
    assistTranslation: ChoiceOption?,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onTranslateWord: (String) -> Unit,
    onAssistConsumed: () -> Unit
) {
    val trailingWord = TRAILING_ENGLISH.find(text.trimEnd())?.value

    // Splice the translated word into the draft in place of the English one
    LaunchedEffect(assistTranslation) {
        val assist = assistTranslation ?: return@LaunchedEffect
        val trimmed = text.trimEnd()
        if (trimmed.endsWith(assist.english)) {
            onTextChange(trimmed.dropLast(assist.english.length) + assist.hanzi)
        }
        onAssistConsumed()
    }

    Column {
        AnimatedVisibility(visible = trailingWord != null || isTranslating) {
            Surface(color = SurfaceWhite) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    AssistChip(
                        onClick = { trailingWord?.let(onTranslateWord) },
                        enabled = !isTranslating,
                        label = {
                            Text(
                                if (isTranslating) "Translating…"
                                else "EN→中  “$trailingWord”",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            if (isTranslating) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ChineseRed)
                            } else {
                                Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp), tint = ChineseRed)
                            }
                        }
                    )
                }
            }
        }
        FreeInputBar(
            text = text,
            onTextChange = onTextChange,
            isListening = isListening,
            isLoading = isLoading,
            onSend = onSend,
            onMicClick = onMicClick
        )
    }
}
