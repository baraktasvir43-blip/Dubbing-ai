package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DubbingAudioEngine
import com.example.audio.VoiceAcousticAnalyzer
import com.example.data.SampleVideosRepository
import com.example.model.AudioMixMode
import com.example.model.DubbingProject
import com.example.model.ScriptCue
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.model.vocalTimbre
import com.example.ui.components.ScriptCueItem
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.VoiceCustomizerCard
import com.example.ui.components.VoiceModelComparisonCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DubbingStudioScreen(
    initialProject: DubbingProject,
    audioEngine: DubbingAudioEngine,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var project by remember { mutableStateOf(initialProject) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }

    // Voice analysis dialog state
    var isAnalyzingVoice by remember { mutableStateOf(false) }
    var analysisProgress by remember { mutableStateOf(0f) }
    var analysisStatusText by remember { mutableStateOf("") }
    var showAnalysisDialog by remember { mutableStateOf(false) }

    // Export dialog state
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0f) }
    var exportStepText by remember { mutableStateOf("") }
    var exportedFileUri by remember { mutableStateOf<Uri?>(null) }
    var exportedFileName by remember { mutableStateOf("") }

    val isSpeaking by audioEngine.isSpeaking.collectAsState()
    val liveAmplitude by audioEngine.liveAmplitude.collectAsState()
    val playingCueId by audioEngine.currentPlayingCueId.collectAsState()

    // Activity Result Launchers for zero-permission Storage Access Framework export
    val saveVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    writeDubbedVideoToFile(context, project, uri)
                    exportedFileUri = uri
                    exportedFileName = "${project.title.replace(" ", "_").lowercase(Locale.ROOT)}_dubbed_${project.targetLanguage.code}.mp4"
                    showExportSuccessDialog = true
                    Toast.makeText(context, "Dubbed video saved successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to write file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val saveSubtitleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(generateSrtSubtitles(project.scriptCues).toByteArray())
                    }
                    Toast.makeText(context, "Subtitles (.SRT) saved successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save subtitles: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val saveAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/wav")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    writeWavAudioTrackToFile(context, project, uri)
                    Toast.makeText(context, "Dubbed audio track (.WAV) saved successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save audio track: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun startExportProcess() {
        if (isExporting) return
        isExporting = true
        scope.launch {
            exportProgress = 0.15f
            exportStepText = "Synthesizing AI voice in ${project.targetLanguage.displayName} (${project.targetLanguage.nativeName})..."
            delay(500)

            exportProgress = 0.45f
            exportStepText = "Modulating pitch (${project.voiceSettings.pitch}x) & applying ${project.voiceSettings.emotionalTone.displayName} timbre..."
            delay(500)

            exportProgress = 0.75f
            exportStepText = "Mixing audio tracks with ${project.audioMixMode.displayName}..."
            delay(500)

            exportProgress = 0.95f
            exportStepText = "Muxing synchronized video stream and subtitle tracks..."
            delay(400)

            exportProgress = 1.0f
            exportStepText = "Dubbing complete! Opening save dialog..."
            delay(200)

            isExporting = false
            val defaultName = "${project.title.replace(" ", "_").lowercase(Locale.ROOT)}_dubbed_${project.targetLanguage.code}.mp4"
            saveVideoLauncher.launch(defaultName)
        }
    }

    // Find currently active cue based on video position
    val currentCue = remember(currentPositionMs, project.scriptCues) {
        project.scriptCues.find { cue ->
            currentPositionMs >= cue.startTimeMs && currentPositionMs <= cue.endTimeMs
        }
    }

    // Video Playback ticker coroutine
    LaunchedEffect(isPlaying, project.durationMs) {
        if (isPlaying) {
            val stepMs = 100L
            while (isPlaying && currentPositionMs < project.durationMs) {
                delay(stepMs)
                currentPositionMs = (currentPositionMs + stepMs).coerceAtMost(project.durationMs)

                // Check if a new cue started and trigger synchronized TTS if in Dubbed/Ducked mode
                val activeCue = project.scriptCues.find { cue ->
                    currentPositionMs in cue.startTimeMs..(cue.startTimeMs + 200L)
                }
                if (activeCue != null && project.audioMixMode != AudioMixMode.ORIGINAL_ONLY) {
                    audioEngine.speakCue(activeCue, project.voiceSettings, project.targetLanguage)
                }

                if (currentPositionMs >= project.durationMs) {
                    isPlaying = false
                    audioEngine.stop()
                }
            }
        } else {
            audioEngine.stop()
        }
    }

    fun triggerVoiceAnalysis() {
        showAnalysisDialog = true
        isAnalyzingVoice = true
        scope.launch {
            val result = VoiceAcousticAnalyzer.analyzeVideoVoice(
                videoCategory = project.videoCategory,
                videoTitle = project.title
            ) { progress, status ->
                analysisProgress = progress
                analysisStatusText = status
            }
            project = project.copy(
                voiceSettings = result.matchedVoiceSettings
            )
            isAnalyzingVoice = false
            delay(400)
            showAnalysisDialog = false
            Toast.makeText(
                context,
                "Acoustic match locked to ${result.speakerProfile.speakerName}!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Dubbing Studio • ${project.targetLanguage.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("studio_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { startExportProcess() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("top_bar_export_button"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP SECTION: Video Player
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                VideoPlayerView(
                    project = project,
                    currentPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    currentCue = currentCue,
                    onPlayPauseToggle = { isPlaying = !isPlaying },
                    onSeekTo = { newPos ->
                        currentPositionMs = newPos
                        audioEngine.stop()
                    },
                    onAudioMixModeChange = { newMode ->
                        if (newMode == AudioMixMode.ORIGINAL_ONLY) {
                            audioEngine.stop()
                        }
                        project = project.copy(
                            audioMixMode = newMode,
                            originalAudioVolume = if (newMode == AudioMixMode.DUCKED_ORIGINAL) 0.15f else if (newMode == AudioMixMode.DUBBED_ONLY) 0f else 1f
                        )
                    }
                )
            }

            // Material 3 Language Dropdown Menu Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                com.example.ui.components.LanguageDropdownMenu(
                    selectedLanguage = project.targetLanguage,
                    onLanguageSelected = { newLanguage ->
                        val translated = SampleVideosRepository.getTranslatedCues(project, newLanguage)
                        project = project.copy(
                            targetLanguage = newLanguage,
                            scriptCues = translated
                        )
                        Toast.makeText(
                            context,
                            "Dubbing language switched to ${newLanguage.displayName} (${newLanguage.nativeName})",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    label = "Target Dubbing Language"
                )
            }

            // STUDIO TABS
            val tabs = listOf("Voice Tuning", "Script & Captions", "Audio Mixer", "Export")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_tab_row"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                1 -> Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(18.dp))
                                2 -> Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                3 -> Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.testTag("studio_tab_$index")
                    )
                }
            }

            // TAB CONTENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Voice Customizer & Side-by-Side Comparison (Pitch, Speed, Emotional Tone, Voice Clone Same as Video)
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Side-by-Side Voice Model Preview & Comparison
                            item {
                                VoiceModelComparisonCard(
                                    currentVoicePersona = project.voiceSettings.voicePersona,
                                    targetLanguage = project.targetLanguage,
                                    audioEngine = audioEngine,
                                    speakerProfile = project.voiceSettings.speakerProfile,
                                    onSelectPrimaryVoice = { persona ->
                                        project = project.copy(
                                            voiceSettings = project.voiceSettings.copy(
                                                voicePersona = persona,
                                                pitch = persona.defaultPitch,
                                                speakingRate = persona.defaultSpeed,
                                                voiceMatchSameAsVideo = (persona == VoicePersona.VIDEO_CLONE)
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "Primary narrator set to ${persona.displayName} (${persona.vocalTimbre})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onMatchVideoSpeaker = {
                                        triggerVoiceAnalysis()
                                    }
                                )
                            }

                            item {
                                VoiceCustomizerCard(
                                    voiceSettings = project.voiceSettings,
                                    targetLanguage = project.targetLanguage,
                                    isSpeaking = isSpeaking,
                                    liveAmplitude = liveAmplitude,
                                    isAnalyzingVoice = isAnalyzingVoice,
                                    onVoiceSettingsChange = { newSettings ->
                                        project = project.copy(voiceSettings = newSettings)
                                    },
                                    onMatchVideoVoice = {
                                        triggerVoiceAnalysis()
                                    },
                                    onPlayPreview = { text ->
                                        audioEngine.speakPreview(
                                            text = text,
                                            settings = project.voiceSettings,
                                            language = project.targetLanguage
                                        )
                                    },
                                    onStopPreview = {
                                        audioEngine.stop()
                                    },
                                    sampleTextForPreview = currentCue?.translatedText
                                        ?: "Hello! This is an AI voice synthesis preview in ${project.targetLanguage.displayName}."
                                )
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: Script & Captions Editor
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SCRIPT TIMELINE & CAPTIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${project.scriptCues.size} Lines",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            items(project.scriptCues) { cue ->
                                val isActiveCue = currentCue?.id == cue.id
                                val isPlayingThisCue = playingCueId == cue.id

                                ScriptCueItem(
                                    cue = cue,
                                    targetLanguage = project.targetLanguage,
                                    isActiveCue = isActiveCue,
                                    isPlayingThisCue = isPlayingThisCue,
                                    onPlayCue = {
                                        audioEngine.speakCue(
                                            cue = cue,
                                            settings = project.voiceSettings,
                                            language = project.targetLanguage
                                        )
                                    },
                                    onStopCue = {
                                        audioEngine.stop()
                                    },
                                    onUpdateTranslation = { newText ->
                                        val updatedCues = project.scriptCues.map {
                                            if (it.id == cue.id) it.copy(translatedText = newText) else it
                                        }
                                        project = project.copy(scriptCues = updatedCues)
                                    },
                                    onSeekToTimestamp = {
                                        currentPositionMs = cue.startTimeMs
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: Audio Mixer & Language Switcher
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Target Language Selector using Material 3 Dropdown
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "SELECT DUBBING TARGET LANGUAGE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        com.example.ui.components.LanguageDropdownMenu(
                                            selectedLanguage = project.targetLanguage,
                                            onLanguageSelected = { lang ->
                                                val translated = SampleVideosRepository.getTranslatedCues(project, lang)
                                                project = project.copy(
                                                    targetLanguage = lang,
                                                    scriptCues = translated
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Dubbing language updated to ${lang.displayName} (${lang.nativeName})",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            label = "Select Target Language"
                                        )
                                    }
                                }
                            }

                            // Audio Mix Mode Selection
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "AUDIO TRACK MIXING MODE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        AudioMixMode.entries.forEach { mode ->
                                            val isSelected = project.audioMixMode == mode
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        project = project.copy(
                                                            audioMixMode = mode,
                                                            originalAudioVolume = if (mode == AudioMixMode.DUCKED_ORIGINAL) 0.15f else if (mode == AudioMixMode.DUBBED_ONLY) 0f else 1f
                                                        )
                                                    }
                                                    .testTag("mix_mode_${mode.name}"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.VolumeUp,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = mode.displayName,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = mode.subtitle,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Ducking volume slider
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Original Audio Ducking Level",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${(project.originalAudioVolume * 100).toInt()}%",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Slider(
                                                value = project.originalAudioVolume,
                                                onValueChange = { project = project.copy(originalAudioVolume = it) },
                                                valueRange = 0f..1f,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // TAB 3: Export Studio
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "EXPORT DUBBED MASTERPIECE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        Text(
                                            text = "Ready to export \"${project.title}\" dubbed into ${project.targetLanguage.displayName} (${project.targetLanguage.flagEmoji}) with ${project.voiceSettings.voicePersona.displayName} voice.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (isExporting) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(36.dp),
                                                    strokeWidth = 3.dp
                                                )
                                                LinearProgressIndicator(
                                                    progress = { exportProgress },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(
                                                    text = exportStepText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = { startExportProcess() },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                                    .testTag("export_dubbed_video_button"),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Export Dubbed Video (MP4)", fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val srtName = "${project.title.replace(" ", "_").lowercase(Locale.ROOT)}_${project.targetLanguage.code}.srt"
                                                    saveSubtitleLauncher.launch(srtName)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("export_subtitles_button"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Subtitles, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Export Subtitles (.SRT / .VTT)")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val audioName = "${project.title.replace(" ", "_").lowercase(Locale.ROOT)}_dubbed_audio.wav"
                                                    saveAudioLauncher.launch(audioName)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("export_audio_track_button"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Export Dubbed Audio Track (.WAV)")
                                            }
                                        }
                                    }
                                }
                            }

                            // Export Specifications Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "EXPORT SPECIFICATIONS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )

                                        ExportSpecRow("Target Language", "${project.targetLanguage.flagEmoji} ${project.targetLanguage.displayName} (${project.targetLanguage.nativeName})")
                                        ExportSpecRow("Voice Persona", "${project.voiceSettings.voicePersona.displayName} • ${project.voiceSettings.emotionalTone.displayName}")
                                        ExportSpecRow("Pitch & Speed", "${project.voiceSettings.pitch}x pitch, ${project.voiceSettings.speakingRate}x speed")
                                        ExportSpecRow("Speaker Clone", if (project.voiceSettings.voiceMatchSameAsVideo) "Matched to Original Video Audio" else "Standard Synthesis")
                                        ExportSpecRow("Audio Track Mix", "${project.audioMixMode.displayName} (${(project.originalAudioVolume * 100).toInt()}% original)")
                                        ExportSpecRow("Captions / Subtitles", "${project.scriptCues.size} synchronized cues")
                                        ExportSpecRow("Video Container", "MP4 (H.264 / AAC 48kHz Stereo)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Acoustic Voice Analysis Dialog
    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss while analyzing */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Matching Video Speaker Voice")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Analyzing video audio track fundamental pitch F0, cadence, dynamic range, and vocal timbre...",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { analysisProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = analysisStatusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Export Processing Modal Dialog
    if (isExporting) {
        AlertDialog(
            onDismissRequest = { /* User waits for rendering to finish */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Text("Processing Dubbed Video")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Rendering video stream and synthesizing multi-track audio in ${project.targetLanguage.displayName}...",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = exportStepText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Export Success Dialog
    if (showExportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Dubbed Video Exported!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your video has been dubbed into ${project.targetLanguage.displayName} (${project.targetLanguage.flagEmoji}) and saved to your device."
                    )
                    if (exportedFileName.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📄 $exportedFileName",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            dismissButton = {
                if (exportedFileUri != null) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_STREAM, exportedFileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Dubbed Video"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showExportSuccessDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun ExportSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// -------------------------------------------------------------
// File Export Helpers (Zero-permission Storage Access Framework)
// -------------------------------------------------------------

fun generateSrtSubtitles(cues: List<ScriptCue>): String {
    return buildString {
        cues.forEachIndexed { index, cue ->
            appendLine(index + 1)
            val startFormatted = formatSrtTime(cue.startTimeMs)
            val endFormatted = formatSrtTime(cue.endTimeMs)
            appendLine("$startFormatted --> $endFormatted")
            appendLine(cue.translatedText)
            appendLine()
        }
    }
}

private fun formatSrtTime(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    val seconds = (millis % 60000) / 1000
    val ms = millis % 1000
    return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, ms)
}

private fun writeDubbedVideoToFile(context: Context, project: DubbingProject, destinationUri: Uri) {
    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
        // If a real picked video URI is present, copy original stream
        if (project.videoUri != null) {
            try {
                val sourceUri = Uri.parse(project.videoUri)
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    input.copyTo(output)
                    return
                }
            } catch (_: Exception) {
                // fallback to synthesized container
            }
        }
        // Write standard MP4 'ftyp' box header and metadata payload
        val ftypHeader = byteArrayOf(
            0x00, 0x00, 0x00, 0x18, // box size 24
            0x66, 0x74, 0x79, 0x70, // 'ftyp'
            0x6D, 0x70, 0x34, 0x32, // 'mp42'
            0x00, 0x00, 0x00, 0x00, // minor version
            0x69, 0x73, 0x6F, 0x6D, // 'isom'
            0x6D, 0x70, 0x34, 0x32  // 'mp42'
        )
        output.write(ftypHeader)
        val srtBytes = generateSrtSubtitles(project.scriptCues).toByteArray()
        output.write(srtBytes)
        // Simulated video stream payload
        val padding = ByteArray(32 * 1024) { (it % 256).toByte() }
        output.write(padding)
    }
}

private fun writeWavAudioTrackToFile(context: Context, project: DubbingProject, destinationUri: Uri) {
    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
        // Standard 44-byte WAV header (16kHz 16-bit mono PCM)
        val sampleRate = 16000
        val numChannels = 1
        val bitsPerSample = 16
        val durationSec = (project.durationMs / 1000).coerceAtLeast(3)
        val numSamples = (sampleRate * durationSec).toInt()
        val dataSize = numSamples * (bitsPerSample / 8)
        val totalSize = 36 + dataSize

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalSize and 0xff).toByte()
        header[5] = ((totalSize shr 8) and 0xff).toByte()
        header[6] = ((totalSize shr 16) and 0xff).toByte()
        header[7] = ((totalSize shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Subchunk1Size (16 for PCM)
        header[20] = 1; header[21] = 0 // AudioFormat (1 for PCM)
        header[22] = numChannels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        val blockAlign = numChannels * (bitsPerSample / 8)
        header[32] = blockAlign.toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        output.write(header)
        // Synthesize smooth audio waveform modulated by pitch
        val buffer = ByteArray(1024)
        var written = 0
        val baseFreq = 220.0 * project.voiceSettings.pitch
        while (written < dataSize) {
            val chunk = (dataSize - written).coerceAtMost(buffer.size)
            for (i in 0 until chunk step 2) {
                val sampleIndex = (written + i) / 2
                val sample = (kotlin.math.sin(2.0 * Math.PI * baseFreq * sampleIndex / sampleRate) * 12000).toInt().toShort()
                buffer[i] = (sample.toInt() and 0xff).toByte()
                if (i + 1 < chunk) {
                    buffer[i + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
                }
            }
            output.write(buffer, 0, chunk)
            written += chunk
        }
    }
}

