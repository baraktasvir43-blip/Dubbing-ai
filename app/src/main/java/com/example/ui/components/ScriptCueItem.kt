package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ScriptCue
import com.example.model.SupportedLanguage

@Composable
fun ScriptCueItem(
    cue: ScriptCue,
    targetLanguage: SupportedLanguage,
    isActiveCue: Boolean,
    isPlayingThisCue: Boolean,
    onPlayCue: () -> Unit,
    onStopCue: () -> Unit,
    onUpdateTranslation: (String) -> Unit,
    onSeekToTimestamp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember(cue.translatedText) { mutableStateOf(cue.translatedText) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isPlayingThisCue -> MaterialTheme.colorScheme.primary
            isActiveCue -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        label = "cue_border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("script_cue_card_${cue.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveCue || isPlayingThisCue) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActiveCue || isPlayingThisCue) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Time code badge + Speaker + Emotion tag + Play Cue Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Clickable timecode badge
                    Surface(
                        onClick = onSeekToTimestamp,
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.testTag("cue_timecode_${cue.id}")
                    ) {
                        Text(
                            text = cue.formatTimestamp(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${cue.emotion.iconEmoji} ${cue.speaker}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save Edit" else "Edit Line",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isPlayingThisCue) onStopCue() else onPlayCue()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayingThisCue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            .testTag("play_cue_button_${cue.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThisCue) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingThisCue) "Stop" else "Play Cue",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Original Speech Reference
            Text(
                text = "\"${cue.originalText}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dubbed Translation (Editable or Display)
            if (isEditing) {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = {
                        editedText = it
                        onUpdateTranslation(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_cue_field_${cue.id}"),
                    label = { Text("Dubbed line in ${targetLanguage.displayName}") },
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = targetLanguage.flagEmoji,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = cue.translatedText.ifBlank { cue.originalText },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
