package com.mandarincoach.app.ui.conversation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarincoach.app.data.model.Scenario
import com.mandarincoach.app.ui.theme.ChineseRed
import com.mandarincoach.app.ui.theme.TextSecondary

@Composable
fun ScenarioCompleteDialog(
    scenario: Scenario,
    onBackToMissions: () -> Unit,
    onKeepChatting: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepChatting,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🛂", fontSize = 40.sp)
                Spacer(Modifier.height(4.dp))
                Text("Mission complete!")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "“${scenario.stampTitle}” has been stamped into your Fluency Passport.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = scenario.items.joinToString("   ") { "${it.emoji} ${it.hanzi}" },
                    fontSize = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onBackToMissions) {
                Text("Back to missions", color = ChineseRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepChatting) {
                Text("Keep chatting", color = TextSecondary)
            }
        }
    )
}
