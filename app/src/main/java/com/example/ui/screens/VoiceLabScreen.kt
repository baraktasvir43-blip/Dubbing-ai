package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DubbingAudioEngine
import com.example.model.EmotionalTone
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.model.vocalTimbre
import com.example.ui.components.VoiceCustomizerCard
import com.example.ui.components.VoiceModelComparisonCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLabScreen(
    audioEngine: DubbingAudioEngine,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var selectedLanguage by remember { mutableStateOf(SupportedLanguage.SPANISH) }
    var voiceSettings by remember {
        mutableStateOf(
            VoiceSettings(
                voicePersona = VoicePersona.ALEX,
                pitch = 1.0f,
                speakingRate = 1.0f,
                emotionalTone = EmotionalTone.ENTHUSIASTIC
            )
        )
    }

    val isSpeaking by audioEngine.isSpeaking.collectAsState()
    val liveAmplitude by audioEngine.liveAmplitude.collectAsState()

    val samplePhrases = listOf(
        "Welcome to the artificial intelligence voice synthesis laboratory.",
        "El doblaje inteligente adapta el tono emocional y la sincronización labial.",
        "Le doublage vidéo de haute précision transforme la narration globale.",
        "Die künstliche Intelligenz passt die Sprechgeschwindigkeit perfekt an.",
        "量子コンピューティングとAIによる高精度な音声合成の世界へようこそ。"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Voice Customizer Lab",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time AI voice synthesis tuning & acoustic cloning",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("voice_lab_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language selector dropdown
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SELECT SYNTHESIS LANGUAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        com.example.ui.components.LanguageDropdownMenu(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { selectedLanguage = it },
                            label = "Test Language for AI Voice Synthesis"
                        )
                    }
                }
            }

            // Speaker Acoustic Preset Matchers ("Voice Like Same As Video")
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "VIDEO SPEAKER CLONE PRESETS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = voiceSettings.voiceMatchSameAsVideo && voiceSettings.speakerProfile.speakerName.contains("David"),
                                onClick = {
                                    val profile = SpeakerAcousticProfile.defaultForTechSpeaker()
                                    voiceSettings = voiceSettings.copy(
                                        voicePersona = VoicePersona.ALEX,
                                        pitch = profile.targetPitchFactor,
                                        speakingRate = profile.targetSpeedFactor,
                                        emotionalTone = profile.detectedEmotion,
                                        voiceMatchSameAsVideo = true,
                                        speakerProfile = profile
                                    )
                                },
                                label = { Text("Keynote Baritone") }
                            )

                            FilterChip(
                                selected = voiceSettings.voiceMatchSameAsVideo && voiceSettings.speakerProfile.speakerName.contains("Maya"),
                                onClick = {
                                    val profile = SpeakerAcousticProfile.defaultForTravelVlogger()
                                    voiceSettings = voiceSettings.copy(
                                        voicePersona = VoicePersona.SOPHIA,
                                        pitch = profile.targetPitchFactor,
                                        speakingRate = profile.targetSpeedFactor,
                                        emotionalTone = profile.detectedEmotion,
                                        voiceMatchSameAsVideo = true,
                                        speakerProfile = profile
                                    )
                                },
                                label = { Text("Warm Vlogger") }
                            )

                            FilterChip(
                                selected = voiceSettings.voiceMatchSameAsVideo && voiceSettings.speakerProfile.speakerName.contains("Julian"),
                                onClick = {
                                    val profile = SpeakerAcousticProfile.defaultForDocumentary()
                                    voiceSettings = voiceSettings.copy(
                                        voicePersona = VoicePersona.MARCUS,
                                        pitch = profile.targetPitchFactor,
                                        speakingRate = profile.targetSpeedFactor,
                                        emotionalTone = profile.detectedEmotion,
                                        voiceMatchSameAsVideo = true,
                                        speakerProfile = profile
                                    )
                                },
                                label = { Text("Cinematic Bass") }
                            )
                        }
                    }
                }
            }

            // Side-by-Side Voice Model Preview & Comparison
            item {
                VoiceModelComparisonCard(
                    currentVoicePersona = voiceSettings.voicePersona,
                    targetLanguage = selectedLanguage,
                    audioEngine = audioEngine,
                    speakerProfile = voiceSettings.speakerProfile,
                    onSelectPrimaryVoice = { persona ->
                        voiceSettings = voiceSettings.copy(
                            voicePersona = persona,
                            pitch = persona.defaultPitch,
                            speakingRate = persona.defaultSpeed,
                            voiceMatchSameAsVideo = (persona == VoicePersona.VIDEO_CLONE)
                        )
                    },
                    onMatchVideoSpeaker = {
                        val profile = SpeakerAcousticProfile.defaultForTechSpeaker()
                        voiceSettings = voiceSettings.copy(
                            voiceMatchSameAsVideo = true,
                            speakerProfile = profile,
                            pitch = profile.targetPitchFactor,
                            speakingRate = profile.targetSpeedFactor,
                            emotionalTone = profile.detectedEmotion
                        )
                    }
                )
            }

            // The Core Customizer Card
            item {
                VoiceCustomizerCard(
                    voiceSettings = voiceSettings,
                    targetLanguage = selectedLanguage,
                    isSpeaking = isSpeaking,
                    liveAmplitude = liveAmplitude,
                    isAnalyzingVoice = false,
                    onVoiceSettingsChange = { voiceSettings = it },
                    onMatchVideoVoice = {
                        val profile = SpeakerAcousticProfile.defaultForTechSpeaker()
                        voiceSettings = voiceSettings.copy(
                            voiceMatchSameAsVideo = true,
                            speakerProfile = profile,
                            pitch = profile.targetPitchFactor,
                            speakingRate = profile.targetSpeedFactor,
                            emotionalTone = profile.detectedEmotion
                        )
                    },
                    onPlayPreview = { text ->
                        audioEngine.speakPreview(
                            text = text,
                            settings = voiceSettings,
                            language = selectedLanguage
                        )
                    },
                    onStopPreview = {
                        audioEngine.stop()
                    },
                    sampleTextForPreview = when (selectedLanguage) {
                        SupportedLanguage.SPANISH -> "Hola, esta es una prueba en tiempo real del motor de síntesis de voz inteligente."
                        SupportedLanguage.FRENCH -> "Bonjour, ceci est un test de synthèse vocale adaptative en temps réel."
                        SupportedLanguage.GERMAN -> "Hallo, dies ist eine Echtzeit-Vorschau der synchronisierten KI-Stimme."
                        SupportedLanguage.JAPANESE -> "こんにちは。これは音声合成エンジンのリアルタイムプレビューです。"
                        SupportedLanguage.HINDI -> "नमस्ते, यह वास्तविक समय में आवाज संश्लेषण का पूर्वावलोकन है।"
                        else -> "Hello, this is a real-time preview of the AI voice synthesis customization engine."
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
