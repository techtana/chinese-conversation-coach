package com.mandarincoach.app.ui.conversation.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.domain.WordBankState
import com.mandarincoach.app.ui.theme.*

/**
 * Stage 2 (Fragment Building): the learner assembles a reply by tapping
 * word chips from a shuffled bank into order. Tap an assembled chip to
 * send it back to the bank.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBankInputBar(
    words: List<String>,
    isLoading: Boolean,
    onSend: (String) -> Unit
) {
    var state by remember(words) { mutableStateOf(WordBankState.fromWords(words)) }

    Surface(shadowElevation = 8.dp, color = SurfaceWhite) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Build your reply · 拼出你的回答",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Assembled reply
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundWarm)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (state.assembled.isEmpty()) {
                    Text(
                        text = "Tap words below…",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.assembled.forEachIndexed { index, word ->
                            WordChip(
                                word = word,
                                container = ChineseRed,
                                content = Color.White,
                                onClick = { state = state.unpick(index) }
                            )
                        }
                    }
                }
            }

            // Word bank
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.bank.forEachIndexed { index, word ->
                    WordChip(
                        word = word,
                        container = SurfaceGray,
                        content = TextPrimary,
                        onClick = { state = state.pick(index) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { state = state.clear() },
                    enabled = state.assembled.isNotEmpty()
                ) {
                    Text("Clear", color = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { onSend(state.assembledText()) },
                    enabled = state.assembled.isNotEmpty() && !isLoading,
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ChineseRed,
                        contentColor = Color.White,
                        disabledContainerColor = SurfaceGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WordChip(
    word: String,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = word,
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif,
            color = content
        )
    }
}
