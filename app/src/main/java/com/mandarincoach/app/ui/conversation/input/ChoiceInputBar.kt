package com.mandarincoach.app.ui.conversation.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.model.ChoiceOption
import com.mandarincoach.app.ui.theme.*

/**
 * Stage 1 (Passive Input): the learner replies by tapping one of the
 * curated options the coach provided.
 */
@Composable
fun ChoiceInputBar(
    choices: List<ChoiceOption>,
    showEnglish: Boolean,
    isLoading: Boolean,
    onChoiceSelected: (ChoiceOption) -> Unit,
    onSpeakChoice: (ChoiceOption) -> Unit
) {
    Surface(shadowElevation = 8.dp, color = SurfaceWhite) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tap your reply · 选择你的回答",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint,
                modifier = Modifier.padding(start = 4.dp)
            )
            choices.forEach { choice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isLoading) { onChoiceSelected(choice) },
                    colors = CardDefaults.cardColors(containerColor = BackgroundWarm),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = choice.hanzi,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            if (choice.pinyin.isNotBlank()) {
                                Text(
                                    text = choice.pinyin,
                                    fontSize = 13.sp,
                                    color = PinyinColor,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            if (showEnglish && choice.english.isNotBlank()) {
                                Text(
                                    text = choice.english,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EnglishColor,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                        IconButton(onClick = { onSpeakChoice(choice) }) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
