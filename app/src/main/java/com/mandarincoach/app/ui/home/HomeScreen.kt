package com.mandarincoach.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLevelSelected: (ProficiencyLevel) -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWarm),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // App title
            Text(
                text = "普通话",
                fontSize = 56.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                color = ChineseRed,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pǔtōnghuà",
                fontSize = 18.sp,
                color = ChineseRedLight,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Mandarin Conversation Coach",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = "Choose your level",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(20.dp))

            // Level cards
            ProficiencyLevel.entries.forEach { level ->
                LevelCard(level = level, onClick = { onLevelSelected(level) })
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "选择后即可开始对话",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Select a level to begin your conversation",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LevelCard(level: ProficiencyLevel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ChineseRed.copy(alpha = levelAlpha(level)))
            )
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${level.emoji} ${level.hanzi}",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = level.pinyin,
                        fontSize = 14.sp,
                        color = PinyinColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = level.englishName,
                    style = MaterialTheme.typography.labelSmall,
                    color = ChineseRed,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                color = TextHint
            )
        }
    }
}

private fun levelAlpha(level: ProficiencyLevel) = when (level) {
    ProficiencyLevel.BEGINNER -> 0.3f
    ProficiencyLevel.INTERMEDIATE -> 0.55f
    ProficiencyLevel.ADVANCED -> 0.8f
    ProficiencyLevel.FLUENT -> 1.0f
}
