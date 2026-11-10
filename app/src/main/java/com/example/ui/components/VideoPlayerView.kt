package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioMixMode
import com.example.model.DubbingProject
import com.example.model.ScriptCue
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VideoPlayerView(
    project: DubbingProject,
    currentPositionMs: Long,
    isPlaying: Boolean,
    currentCue: ScriptCue?,
    onPlayPauseToggle: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onAudioMixModeChange: ((AudioMixMode) -> Unit)? = null
) {
    var showControls by remember { mutableStateOf(true) }
    var showDualSubtitles by remember { mutableStateOf(true) }
    var showAudioTrackOverlay by remember { mutableStateOf(false) }

    // Quick HUD message notification when switching audio tracks
    var hudTrackNotification by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hudTrackNotification) {
        if (hudTrackNotification != null) {
            delay(1800)
            hudTrackNotification = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (showAudioTrackOverlay) {
                    showAudioTrackOverlay = false
                } else {
                    showControls = !showControls
                }
            }
            .testTag("video_player_box")
    ) {
        // Real Video Playback or Procedural Frame Renderer
        if (!project.videoUri.isNullOrBlank()) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        try {
                            setVideoURI(android.net.Uri.parse(project.videoUri))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                // Mute original audio if Dubbed Only is active
                                if (project.audioMixMode == AudioMixMode.DUBBED_ONLY) {
                                    mp.setVolume(0f, 0f)
                                } else if (project.audioMixMode == AudioMixMode.DUCKED_ORIGINAL) {
                                    mp.setVolume(project.originalAudioVolume, project.originalAudioVolume)
                                } else {
                                    mp.setVolume(1f, 1f)
                                }
                                if (isPlaying) start()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("VideoPlayerView", "VideoView setVideoURI failed", e)
                        }
                    }
                },
                update = { videoView ->
                    try {
                        if (isPlaying) {
                            if (!videoView.isPlaying) {
                                videoView.seekTo(currentPositionMs.toInt())
                                videoView.start()
                            }
                        } else {
                            if (videoView.isPlaying) {
                                videoView.pause()
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("VideoPlayerView", "Error controlling video view", e)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            VideoFrameRenderer(
                category = project.videoCategory,
                positionMs = currentPositionMs,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Overlay: Project Category & Interactive Audio Track Switcher Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                    Text(
                        text = project.videoCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Interactive Clickable Audio Track Overlay Pill
            Surface(
                onClick = { showAudioTrackOverlay = !showAudioTrackOverlay },
                shape = RoundedCornerShape(20.dp),
                color = when (project.audioMixMode) {
                    AudioMixMode.DUCKED_ORIGINAL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f)
                    AudioMixMode.DUBBED_ONLY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.90f)
                    AudioMixMode.ORIGINAL_ONLY -> Color.Black.copy(alpha = 0.75f)
                    AudioMixMode.SPLIT_STEREO -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.90f)
                },
                modifier = Modifier.testTag("audio_mix_mode_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (project.audioMixMode) {
                            AudioMixMode.ORIGINAL_ONLY -> Icons.Default.VolumeUp
                            AudioMixMode.DUBBED_ONLY -> Icons.Default.Mic
                            else -> Icons.Default.GraphicEq
                        },
                        contentDescription = "Switch Audio Track",
                        tint = when (project.audioMixMode) {
                            AudioMixMode.ORIGINAL_ONLY -> Color.White
                            AudioMixMode.DUBBED_ONLY -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when (project.audioMixMode) {
                            AudioMixMode.DUCKED_ORIGINAL -> "Ducked Mix (15%)"
                            AudioMixMode.DUBBED_ONLY -> "Dubbed (${project.targetLanguage.code.uppercase()})"
                            AudioMixMode.ORIGINAL_ONLY -> "Original (${project.originalLanguage.code.uppercase()})"
                            AudioMixMode.SPLIT_STEREO -> "Dual Stereo"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (project.audioMixMode) {
                            AudioMixMode.ORIGINAL_ONLY -> Color.White
                            AudioMixMode.DUBBED_ONLY -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Settings",
                        tint = when (project.audioMixMode) {
                            AudioMixMode.ORIGINAL_ONLY -> Color.White.copy(alpha = 0.7f)
                            AudioMixMode.DUBBED_ONLY -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Center HUD Track Switch Notification
        AnimatedVisibility(
            visible = hudTrackNotification != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("audio_track_hud_notification")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = hudTrackNotification.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Center / Bottom: Subtitle Caption Overlay
        if (currentCue != null && !showAudioTrackOverlay) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = if (showControls) 80.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Translated Dubbed Subtitle (Primary Highlight)
                if (project.audioMixMode != AudioMixMode.ORIGINAL_ONLY || showDualSubtitles) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.82f)
                    ) {
                        Text(
                            text = currentCue.translatedText.ifBlank { currentCue.originalText },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("active_dubbed_subtitle")
                        )
                    }
                }

                // Original Speech Subtitle (Secondary Reference)
                if (showDualSubtitles) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = "${project.originalLanguage.flagEmoji} \"${currentCue.originalText}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2E8F0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // ON-SCREEN VIDEO CONTROLS OVERLAY
        AnimatedVisibility(
            visible = showControls && !showAudioTrackOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.40f))
            ) {
                // Center Play/Pause & Skip Buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (currentPositionMs - 5000L).coerceAtLeast(0L)
                            onSeekTo(newPos)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("skip_backward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay5,
                            contentDescription = "Rewind 5s",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("video_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (currentPositionMs + 5000L).coerceAtMost(project.durationMs)
                            onSeekTo(newPos)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("skip_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Forward 5s",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // BOTTOM CONTROLS: Quick Audio Track Switcher Bar + Timeline Scrubber
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Quick Audio Track Selector Segmented Chips directly on player
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.70f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Track 1: Original Audio
                                QuickAudioTrackButton(
                                    label = "Original Audio",
                                    icon = Icons.Default.VolumeUp,
                                    flag = project.originalLanguage.flagEmoji,
                                    isSelected = project.audioMixMode == AudioMixMode.ORIGINAL_ONLY,
                                    onClick = {
                                        onAudioMixModeChange?.invoke(AudioMixMode.ORIGINAL_ONLY)
                                        hudTrackNotification = "🔊 Switched to: Original Audio (${project.originalLanguage.displayName})"
                                    },
                                    testTag = "track_button_original"
                                )

                                // Track 2: Dubbed Audio
                                QuickAudioTrackButton(
                                    label = "Dubbed Voice",
                                    icon = Icons.Default.Mic,
                                    flag = project.targetLanguage.flagEmoji,
                                    isSelected = project.audioMixMode == AudioMixMode.DUBBED_ONLY,
                                    onClick = {
                                        onAudioMixModeChange?.invoke(AudioMixMode.DUBBED_ONLY)
                                        hudTrackNotification = "🎙️ Switched to: Dubbed Voice (${project.targetLanguage.displayName})"
                                    },
                                    testTag = "track_button_dubbed"
                                )

                                // Track 3: Broadcast Ducked Mix
                                QuickAudioTrackButton(
                                    label = "Ducked Mix",
                                    icon = Icons.Default.GraphicEq,
                                    flag = "🎚️",
                                    isSelected = project.audioMixMode == AudioMixMode.DUCKED_ORIGINAL,
                                    onClick = {
                                        onAudioMixModeChange?.invoke(AudioMixMode.DUCKED_ORIGINAL)
                                        hudTrackNotification = "🎚️ Switched to: Broadcast Ducked Mix (15% Original)"
                                    },
                                    testTag = "track_button_ducked"
                                )
                            }
                        }
                    }

                    // Scrubber Timeline & Audio Settings Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        Slider(
                            value = currentPositionMs.toFloat(),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..project.durationMs.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("video_timeline_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        Text(
                            text = formatTime(project.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        // Audio Track Switcher Dialog Opener
                        IconButton(
                            onClick = { showAudioTrackOverlay = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("open_audio_track_overlay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Audio Tracks",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Subtitle Toggle
                        IconButton(
                            onClick = { showDualSubtitles = !showDualSubtitles },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "Toggle Subtitles",
                                tint = if (showDualSubtitles) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // DEDICATED ON-SCREEN AUDIO TRACK SWITCHER OVERLAY (MODAL OVER VIDEO)
        AnimatedVisibility(
            visible = showAudioTrackOverlay,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(14.dp)
                    .testTag("audio_track_switcher_overlay")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
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
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Select Audio Track",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showAudioTrackOverlay = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Track Options List
                    AudioTrackOptionItem(
                        title = "Original Audio (${project.originalLanguage.displayName})",
                        subtitle = "100% original speaker soundtrack & ambient audio",
                        flagEmoji = project.originalLanguage.flagEmoji,
                        icon = Icons.Default.VolumeUp,
                        isSelected = project.audioMixMode == AudioMixMode.ORIGINAL_ONLY,
                        onClick = {
                            onAudioMixModeChange?.invoke(AudioMixMode.ORIGINAL_ONLY)
                            hudTrackNotification = "🔊 Switched to: Original Audio"
                            showAudioTrackOverlay = false
                        }
                    )

                    AudioTrackOptionItem(
                        title = "Dubbed AI Audio (${project.targetLanguage.displayName})",
                        subtitle = "100% synthesized ${project.voiceSettings.voicePersona.displayName} voice",
                        flagEmoji = project.targetLanguage.flagEmoji,
                        icon = Icons.Default.Mic,
                        isSelected = project.audioMixMode == AudioMixMode.DUBBED_ONLY,
                        onClick = {
                            onAudioMixModeChange?.invoke(AudioMixMode.DUBBED_ONLY)
                            hudTrackNotification = "🎙️ Switched to: Dubbed Audio (${project.targetLanguage.displayName})"
                            showAudioTrackOverlay = false
                        }
                    )

                    AudioTrackOptionItem(
                        title = "Broadcast Ducked Mix (15% Original)",
                        subtitle = "Original audio lowered in background with clear dubbed voice",
                        flagEmoji = "🎚️",
                        icon = Icons.Default.GraphicEq,
                        isSelected = project.audioMixMode == AudioMixMode.DUCKED_ORIGINAL,
                        onClick = {
                            onAudioMixModeChange?.invoke(AudioMixMode.DUCKED_ORIGINAL)
                            hudTrackNotification = "🎚️ Switched to: Ducked Mix"
                            showAudioTrackOverlay = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAudioTrackButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = flag, fontSize = 12.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun AudioTrackOptionItem(
    title: String,
    subtitle: String,
    flagEmoji: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = flagEmoji, fontSize = 18.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active Track",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Procedural graphical video frame renderer that creates visual movement matching
 * the theme of the video (Quantum Tech, Travel Bamboo, Patagonia Glaciers, Culinary).
 */
@Composable
private fun VideoFrameRenderer(
    category: String,
    positionMs: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFactor = (positionMs / 1000f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        when {
            category.contains("Tech", ignoreCase = true) -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF020617)),
                        center = Offset(width * 0.5f, height * 0.5f),
                        radius = width * 0.8f
                    )
                )

                for (ring in 1..4) {
                    val angle = (timeFactor * (ring * 0.5f))
                    val radius = (height * 0.22f * ring)
                    val cx = width * 0.5f + cos(angle) * (15f * ring)
                    val cy = height * 0.5f + sin(angle) * (15f * ring)

                    drawCircle(
                        color = Color(0xFF6366F1).copy(alpha = 0.25f / ring),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                val gridStep = 45f
                for (x in 0..(width / gridStep).toInt()) {
                    val px = x * gridStep
                    val waveY = height * 0.5f + sin(timeFactor * 2f + x * 0.4f) * (height * 0.25f)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                        radius = 3.5f,
                        center = Offset(px, waveY)
                    )
                }
            }
            category.contains("Travel", ignoreCase = true) -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF022C22))
                    )
                )
                for (b in 0..12) {
                    val bx = (b * width / 12f) + sin(timeFactor + b.toFloat()) * 4f
                    drawLine(
                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                        start = Offset(bx, 0f),
                        end = Offset(bx + 8f, height),
                        strokeWidth = 14f
                    )
                }
            }
            category.contains("Nature", ignoreCase = true) || category.contains("Doc", ignoreCase = true) -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF082F49))
                    )
                )
                for (w in 0..6) {
                    val y = height * (0.2f + w * 0.12f)
                    val offset = (timeFactor * 120f + w * 50f) % width
                    drawLine(
                        color = Color(0xFFBAE6FD).copy(alpha = 0.4f),
                        start = Offset(offset - 80f, y),
                        end = Offset(offset + 80f, y),
                        strokeWidth = 3f
                    )
                }
            }
            else -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7C2D12), Color(0xFF451A03), Color(0xFF1C1917)),
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
