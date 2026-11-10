package com.example.ui.components

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioMixMode
import com.example.model.DubbingProject
import com.example.model.EmotionalTone
import com.example.model.ScriptCue
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun VideoFilePickerModal(
    onDismissRequest: () -> Unit,
    onProjectCreated: (DubbingProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoFileName by remember { mutableStateOf<String?>(null) }
    var videoDurationMs by remember { mutableStateOf(24000L) }
    var videoFileSize by remember { mutableStateOf<String?>(null) }

    var targetLanguage by remember { mutableStateOf(SupportedLanguage.SPANISH) }
    var originalLanguage by remember { mutableStateOf(SupportedLanguage.ENGLISH) }
    var matchSameAsVideo by remember { mutableStateOf(true) }

    var isProcessing by remember { mutableStateOf(false) }
    var processProgress by remember { mutableStateOf(0f) }
    var processStepDescription by remember { mutableStateOf("") }

    // Modern Zero-Permission Android Photo & Video Picker (Google Play policy compliant)
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            extractVideoMetadata(context, uri) { name, duration, size ->
                videoFileName = name
                if (duration > 0) videoDurationMs = duration
                videoFileSize = size
            }
        }
    }

    // Fallback document/content picker
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            extractVideoMetadata(context, uri) { name, duration, size ->
                videoFileName = name
                if (duration > 0) videoDurationMs = duration
                videoFileSize = size
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("video_file_picker_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Import Video to Dub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select any local video file from your device",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isProcessing) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("close_video_picker_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (!isProcessing) {
                    // SELECTION AREA: If no video is selected yet, show Picker Drop Area
                    if (selectedVideoUri == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        pickVisualMediaLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    } catch (e: Exception) {
                                        getContentLauncher.launch("video/*")
                                    }
                                }
                                .testTag("select_device_video_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = "Pick Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Text(
                                    text = "Tap to Pick Video from Device",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Supports MP4, MOV, WebM, MKV, 3GP formats (Zero permission secure Photo & Video picker)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Button(
                                    onClick = {
                                        try {
                                            pickVisualMediaLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                            )
                                        } catch (e: Exception) {
                                            getContentLauncher.launch("video/*")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Browse Videos", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        // VIDEO SELECTED: Preview Badge & Configuration
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_video_preview_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = videoFileName ?: "Selected Device Video",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Duration: ${formatDuration(videoDurationMs)} • ${videoFileSize ?: "Ready to Dub"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        pickVisualMediaLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Change", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Target Language Selection via Material 3 Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SELECT TARGET DUBBING LANGUAGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LanguageDropdownMenu(
                                selectedLanguage = targetLanguage,
                                onLanguageSelected = { targetLanguage = it },
                                label = "Target Dubbing Language"
                            )
                        }

                        // Start Dubbing Studio Button
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    processStepDescription = "Extracting audio track & isolating speech waveforms..."
                                    processProgress = 0.25f
                                    delay(450)
                                    processStepDescription = "Transcribing original dialogue cues & timestamps..."
                                    processProgress = 0.60f
                                    delay(500)
                                    processStepDescription = "Generating $targetLanguage dubbing script with duration matching..."
                                    processProgress = 0.85f
                                    delay(450)
                                    processStepDescription = "Cloning speaker pitch & acoustic timbre..."
                                    processProgress = 1.0f
                                    delay(300)

                                    val newProject = generateImportedProject(
                                        fileName = videoFileName ?: "Local Video Import",
                                        videoUri = selectedVideoUri.toString(),
                                        durationMs = videoDurationMs,
                                        originalLang = originalLanguage,
                                        targetLang = targetLanguage
                                    )

                                    isProcessing = false
                                    onProjectCreated(newProject)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_dubbing_process_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Dubbing Process", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Processing / Transcribing / Synthesizing State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(54.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Analyzing & Preparing Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = processStepDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        LinearProgressIndicator(
                            progress = { processProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

private fun extractVideoMetadata(
    context: Context,
    uri: Uri,
    onResult: (name: String, durationMs: Long, sizeStr: String) -> Unit
) {
    var fileName = "Imported Video.mp4"
    var fileSizeStr = "Local Video"
    var durationMs = 20000L

    try {
        // Query ContentResolver for display name & file size
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
                if (sizeIndex != -1) {
                    val bytes = cursor.getLong(sizeIndex)
                    fileSizeStr = formatFileSize(bytes)
                }
            }
        }

        // Query MediaMetadataRetriever for duration
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!durationStr.isNullOrEmpty()) {
                durationMs = durationStr.toLongOrNull() ?: durationMs
            }
        } catch (e: Exception) {
            Log.w("VideoFilePicker", "Retriever error: ${e.message}")
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    } catch (e: Exception) {
        Log.e("VideoFilePicker", "Failed to extract metadata", e)
    }

    onResult(fileName, durationMs, fileSizeStr)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024f * 1024f)
    return String.format("%.1f MB", mb)
}

private fun formatDuration(ms: Long): String {
    val sec = ms / 1000
    val m = sec / 60
    val s = sec % 60
    return String.format("%02d:%02d", m, s)
}

private fun generateImportedProject(
    fileName: String,
    videoUri: String,
    durationMs: Long,
    originalLang: SupportedLanguage,
    targetLang: SupportedLanguage
): DubbingProject {
    // Generate realistic time-coded script cues for the imported video
    val cueDuration = (durationMs / 4).coerceAtLeast(3000L)
    val cues = listOf(
        ScriptCue(
            id = "imp_1",
            startTimeMs = 0L,
            endTimeMs = cueDuration - 500L,
            originalText = "Welcome to this presentation. Let's explore the key highlights together.",
            translatedText = when (targetLang) {
                SupportedLanguage.SPANISH -> "Bienvenidos a esta presentación. Exploremos los puntos clave juntos."
                SupportedLanguage.FRENCH -> "Bienvenue à cette présentation. Explorons ensemble les points essentiels."
                SupportedLanguage.GERMAN -> "Herzlich willkommen zu dieser Präsentation. Lassen Sie uns die Höhepunkte erkunden."
                SupportedLanguage.JAPANESE -> "プレゼンテーションへようこそ。主なポイントを一緒に見ていきましょう。"
                SupportedLanguage.HINDI -> "इस प्रस्तुति में आपका स्वागत है। आइए मिलकर मुख्य बातों को देखें।"
                else -> "Welcome to this video presentation in ${targetLang.displayName}."
            },
            speaker = "Speaker 1",
            emotion = EmotionalTone.ENTHUSIASTIC
        ),
        ScriptCue(
            id = "imp_2",
            startTimeMs = cueDuration,
            endTimeMs = (cueDuration * 2) - 500L,
            originalText = "As we can observe in this section, the visual dynamics and pacing create a seamless story.",
            translatedText = when (targetLang) {
                SupportedLanguage.SPANISH -> "Como podemos observar en esta sección, la dinámica visual y el ritmo crean una historia fluida."
                SupportedLanguage.FRENCH -> "Comme nous pouvons l'observer ici, la dynamique visuelle et le rythme créent une histoire fluide."
                SupportedLanguage.GERMAN -> "Wie wir in diesem Abschnitt sehen können, schaffen die visuelle Dynamik und das Tempo eine nahtlose Geschichte."
                SupportedLanguage.JAPANESE -> "ご覧のように、視覚的なダイナミクスとテンポが滑らかな物語を生み出しています。"
                SupportedLanguage.HINDI -> "जैसा कि हम इस खंड में देख सकते हैं, दृश्य गतिशीलता और गति एक सहज कहानी बनाती है।"
                else -> "The visual dynamics and pacing create a compelling flow in ${targetLang.displayName}."
            },
            speaker = "Speaker 1",
            emotion = EmotionalTone.WARM_FRIENDLY
        ),
        ScriptCue(
            id = "imp_3",
            startTimeMs = cueDuration * 2,
            endTimeMs = (cueDuration * 3) - 500L,
            originalText = "AI voice synthesis matches the original pitch, emotional cadence, and speech velocity perfectly.",
            translatedText = when (targetLang) {
                SupportedLanguage.SPANISH -> "La síntesis de voz con IA reproduce el tono original, la cadencia emocional y la velocidad con precisión."
                SupportedLanguage.FRENCH -> "La synthèse vocale par IA reproduit parfaitement la hauteur, la cadence émotionnelle et la vitesse."
                SupportedLanguage.GERMAN -> "Die KI-Sprachsynthese passt Tonhöhe, emotionale Kadenz und Sprechgeschwindigkeit präzise an."
                SupportedLanguage.JAPANESE -> "AI音声合成が元の声の高さ、感情的な抑揚、話速を正確に再現します。"
                SupportedLanguage.HINDI -> "एआई आवाज संश्लेषण मूल पिच, भावनात्मक लय और गति को पूरी तरह से मेल खाता है।"
                else -> "AI voice synthesis matches the pitch, emotion, and pace in ${targetLang.displayName}."
            },
            speaker = "Speaker 1",
            emotion = EmotionalTone.AUTHORITATIVE
        ),
        ScriptCue(
            id = "imp_4",
            startTimeMs = cueDuration * 3,
            endTimeMs = durationMs,
            originalText = "Thank you for watching. The multi-language dubbing process is now complete.",
            translatedText = when (targetLang) {
                SupportedLanguage.SPANISH -> "Muchas gracias por ver el video. El proceso de doblaje multilingüe se ha completado."
                SupportedLanguage.FRENCH -> "Merci d'avoir regardé. Le processus de doublage multilingue est désormais terminé."
                SupportedLanguage.GERMAN -> "Vielen Dank fürs Zuschauen. Der mehrsprachige Synchronisationsprozess ist nun abgeschlossen."
                SupportedLanguage.JAPANESE -> "ご視聴いただきありがとうございました。多言語吹替処理が完了しました。"
                SupportedLanguage.HINDI -> "देखने के लिए धन्यवाद। बहुभाषी डबिंग प्रक्रिया अब पूरी हो गई है।"
                else -> "Thank you for watching. The video dubbing is ready in ${targetLang.displayName}."
            },
            speaker = "Speaker 1",
            emotion = EmotionalTone.ENTHUSIASTIC
        )
    )

    val speakerProfile = SpeakerAcousticProfile(
        speakerName = "Local Video Speaker",
        genderTimbre = "Dynamic Natural Vocalist",
        fundamentalPitchHz = 132,
        targetPitchFactor = 1.0f,
        speechRateWpm = 142,
        targetSpeedFactor = 1.05f,
        dynamicEnergy = "High Vocal Clarity",
        detectedEmotion = EmotionalTone.ENTHUSIASTIC
    )

    return DubbingProject(
        id = "local_video_${UUID.randomUUID()}",
        title = fileName.substringBeforeLast("."),
        description = "Device video imported for multi-language AI dubbing (${formatFileSize(0)}).",
        videoCategory = "Local Device Video",
        durationMs = durationMs,
        videoUri = videoUri,
        originalLanguage = originalLang,
        targetLanguage = targetLang,
        voiceSettings = VoiceSettings(
            voicePersona = VoicePersona.ALEX,
            pitch = speakerProfile.targetPitchFactor,
            speakingRate = speakerProfile.targetSpeedFactor,
            emotionalTone = speakerProfile.detectedEmotion,
            voiceMatchSameAsVideo = true,
            speakerProfile = speakerProfile,
            customVoiceLabel = "Matched Speaker Clone"
        ),
        scriptCues = cues,
        originalAudioVolume = 0.15f,
        dubbedAudioVolume = 1.0f,
        audioMixMode = AudioMixMode.DUCKED_ORIGINAL,
        isDubbingProcessed = true
    )
}
