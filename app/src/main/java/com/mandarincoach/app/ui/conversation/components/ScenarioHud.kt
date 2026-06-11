package com.mandarincoach.app.ui.conversation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.model.Scenario
import com.mandarincoach.app.data.model.ScenarioItem
import com.mandarincoach.app.ui.theme.*

private val MOOD_EMOJI = mapOf(
    "happy" to "😊",
    "neutral" to "🙂",
    "confused" to "😕",
    "annoyed" to "😒",
    "impressed" to "🤩"
)

/**
 * Slim strip under the hint bar while a scenario is active: mission
 * title, the character's current mood, and the inventory earned so far.
 */
@Composable
fun ScenarioHud(
    scenario: Scenario,
    mood: String?,
    earnedItems: List<ScenarioItem>
) {
    Surface(color = SurfaceWhite, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${scenario.emoji} ${scenario.title} · ${scenario.hanziTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            mood?.let {
                Text(text = MOOD_EMOJI[it] ?: "🙂", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
            }
            scenario.items.forEach { item ->
                val earned = earnedItems.any { it.id == item.id }
                AnimatedVisibility(visible = true, enter = scaleIn()) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (earned) GoldAccent.copy(alpha = 0.25f) else SurfaceGray)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (earned) "${item.emoji} ${item.hanzi}" else "❔",
                            fontSize = 13.sp,
                            color = if (earned) TextPrimary else TextHint
                        )
                    }
                }
            }
        }
    }
}
