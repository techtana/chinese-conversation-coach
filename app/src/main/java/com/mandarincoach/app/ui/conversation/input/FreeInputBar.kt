package com.mandarincoach.app.ui.conversation.input

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mandarincoach.app.ui.theme.*

@Composable
fun FreeInputBar(
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
