package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
import com.example.model.EmotionalTone
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.model.avatarEmoji
import com.example.model.bestFor
import com.example.model.vocalTimbre
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCustomizerCard(
    voiceSettings: VoiceSettings,
    targetLanguage: SupportedLanguage,
    isSpeaking: Boolean,
    liveAmplitude: Float,
    isAnalyzingVoice: Boolean,
    onVoiceSettingsChange: (VoiceSettings) -> Unit,
    onMatchVideoVoice: () -> Unit,
    onPlayPreview: (String) -> Unit,
    onStopPreview: () -> Unit,
    modifier: Modifier = Modifier,
    sampleTextForPreview: String = "Hello! This is a preview of the customized AI dubbing voice."
) {
    var previewText by remember(sampleTextForPreview) {
        mutableStateOf(sampleTextForPreview)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_customizer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Title & Active Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Voice Customization",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Voice Customizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fine-tune tone, pitch & pacing with real-time preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // FEATURE: "Voice Like Same As Video" (Auto Clone / Match Video Speaker)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_match_video_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (voiceSettings.voiceMatchSameAsVideo) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (voiceSettings.voiceMatchSameAsVideo) {
                    borderWidth(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    borderWidth(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Voice Clone",
                                tint = if (voiceSettings.voiceMatchSameAsVideo) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Voice Like Same As Video",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Clone & match the original video speaker's vocal profile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isAnalyzingVoice) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Switch(
                                checked = voiceSettings.voiceMatchSameAsVideo,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        onMatchVideoVoice()
                                    } else {
                                        onVoiceSettingsChange(
                                            voiceSettings.copy(
                                                voiceMatchSameAsVideo = false,
                                                voicePersona = VoicePersona.ALEX
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("voice_match_same_as_video_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    // Speaker profile details when voice matching is active
                    AnimatedVisibility(visible = voiceSettings.voiceMatchSameAsVideo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Matched Speaker Profile: ${voiceSettings.speakerProfile.speakerName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• Timbre: ${voiceSettings.speakerProfile.genderTimbre}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "• Pitch F0: ${voiceSettings.speakerProfile.fundamentalPitchHz} Hz",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• Cadence: ~${voiceSettings.speakerProfile.speechRateWpm} WPM",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "• Emotion: ${voiceSettings.speakerProfile.detectedEmotion.displayName}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: Voice Persona Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "VOICE ACTOR PERSONA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoicePersona.entries.forEach { persona ->
                        val isSelected = voiceSettings.voicePersona == persona
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onVoiceSettingsChange(
                                    voiceSettings.copy(
                                        voicePersona = persona,
                                        pitch = persona.defaultPitch,
                                        speakingRate = persona.defaultSpeed,
                                        voiceMatchSameAsVideo = (persona == VoicePersona.VIDEO_CLONE)
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = persona.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("persona_chip_${persona.id}")
                        )
                    }
                }

                Text(
                    text = "${voiceSettings.voicePersona.avatarEmoji} ${voiceSettings.voicePersona.displayName}: ${voiceSettings.voicePersona.vocalTimbre} • Best for ${voiceSettings.voicePersona.bestFor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // SECTION 2: Pitch Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val effectivePitch = voiceSettings.effectivePitch()
                val pitchDescription = getPitchDescription(effectivePitch)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Pitch",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Voice Pitch",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.2fx • %s", effectivePitch, pitchDescription),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = voiceSettings.pitch,
                    onValueChange = { newPitch ->
                        onVoiceSettingsChange(
                            voiceSettings.copy(
                                pitch = newPitch,
                                voiceMatchSameAsVideo = false
                            )
                        )
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 29, // 0.05 increments
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pitch_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0.5x (Deep Bass)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = "1.0x (Standard)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = "2.0x (High Soprano)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // SECTION 3: Speaking Speed Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val effectiveSpeed = voiceSettings.effectiveSpeakingRate()
                val estimatedWpm = (140 * effectiveSpeed).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Speaking Speed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.2fx (~%d WPM)", effectiveSpeed, estimatedWpm),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Slider(
                    value = voiceSettings.speakingRate,
                    onValueChange = { newSpeed ->
                        onVoiceSettingsChange(
                            voiceSettings.copy(
                                speakingRate = newSpeed,
                                voiceMatchSameAsVideo = false
                            )
                        )
                    },
                    valueRange = 0.5f..2.5f,
                    steps = 39, // 0.05 increments
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speed_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0.5x (Deliberate)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = "1.0x (Natural)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = "2.5x (Rapid Dub)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // SECTION 4: Emotional Tone Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EMOTIONAL TONE & EXPRESSION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${voiceSettings.emotionalTone.iconEmoji} ${voiceSettings.emotionalTone.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmotionalTone.entries.forEach { tone ->
                        val isSelected = voiceSettings.emotionalTone == tone
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onVoiceSettingsChange(
                                    voiceSettings.copy(emotionalTone = tone)
                                )
                            },
                            label = {
                                Text(
                                    text = "${tone.iconEmoji} ${tone.displayName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            modifier = Modifier.testTag("tone_chip_${tone.id}")
                        )
                    }
                }

                Text(
                    text = voiceSettings.emotionalTone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // SECTION 5: REAL-TIME PREVIEW PLAYBACK
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("real_time_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = borderWidth(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Live Voice Synthesis Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isSpeaking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (isSpeaking) "• SPEAKING" else "IDLE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSpeaking) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Editable or selected preview text
                    OutlinedTextField(
                        value = previewText,
                        onValueChange = { previewText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_text_input"),
                        label = { Text("Preview Sentence (${targetLanguage.flagEmoji} ${targetLanguage.displayName})") },
                        maxLines = 2,
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Waveform visualizer
                    AudioWaveformVisualizer(
                        isSpeaking = isSpeaking,
                        amplitude = liveAmplitude,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Preview Controls: Hear Preview & Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isSpeaking) {
                                    onStopPreview()
                                } else {
                                    onPlayPreview(previewText)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hear_voice_preview_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpeaking) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isSpeaking) "Stop" else "Play",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeaking) "Stop Preview" else "Hear Voice Preview",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                onVoiceSettingsChange(
                                    VoiceSettings(
                                        voicePersona = VoicePersona.ALEX,
                                        pitch = 1.0f,
                                        speakingRate = 1.0f,
                                        emotionalTone = EmotionalTone.NEUTRAL
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("reset_voice_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun borderWidth(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

private fun getPitchDescription(pitch: Float): String {
    return when {
        pitch < 0.75f -> "Deep Bass"
        pitch < 0.95f -> "Warm Baritone"
        pitch <= 1.05f -> "Balanced Natural"
        pitch <= 1.25f -> "Clear Tenor / Alto"
        else -> "Bright Soprano"
    }
}
