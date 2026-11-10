package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DubbingAudioEngine
import com.example.model.EmotionalTone
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.model.avatarEmoji
import com.example.model.bestFor
import com.example.model.getSamplePhrase
import com.example.model.vocalTimbre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Side-by-side voice model preview component allowing creators to listen to short
 * audio samples of different AI voice models side by side before selecting the primary voice.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceModelComparisonCard(
    currentVoicePersona: VoicePersona,
    targetLanguage: SupportedLanguage,
    audioEngine: DubbingAudioEngine,
    onSelectPrimaryVoice: (VoicePersona) -> Unit,
    modifier: Modifier = Modifier,
    speakerProfile: SpeakerAcousticProfile? = null,
    onMatchVideoSpeaker: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val isSpeaking by audioEngine.isSpeaking.collectAsState()
    val liveAmplitude by audioEngine.liveAmplitude.collectAsState()

    // 0: Dual Side-by-Side Compare (A vs B), 1: All Models Grid
    var compareModeTabIndex by remember { mutableIntStateOf(0) }

    // Dual compare selection
    var modelA by remember { mutableStateOf(VoicePersona.ALEX) }
    var modelB by remember { mutableStateOf(VoicePersona.SOPHIA) }

    // Which voice persona is currently being auditioned
    var auditioningPersona by remember { mutableStateOf<VoicePersona?>(null) }
    var isComparingSequential by remember { mutableStateOf(false) }

    // Sample phrase state
    var selectedPhraseCategory by remember { mutableStateOf("Intro") }
    var customAuditionPhrase by remember(targetLanguage, selectedPhraseCategory) {
        mutableStateOf(
            when (selectedPhraseCategory) {
                "Action" -> when (targetLanguage) {
                    SupportedLanguage.SPANISH -> "¡Observen los datos de telemetría ahora mismo! ¡La reacción superó el noventa por ciento!"
                    SupportedLanguage.FRENCH -> "Regardez les données de télémétrie maintenant ! Le seuil a dépassé quatre-vingt-dix pour cent !"
                    SupportedLanguage.GERMAN -> "Schau dir die Telemetriedaten jetzt an! Die Reaktionsschwelle hat neunzig Prozent überschritten!"
                    SupportedLanguage.JAPANESE -> "テレメトリーデータを確認してください！反応閾値が90パーセントを突破しました！"
                    else -> "Look at the telemetry data right now! The reaction threshold has breached ninety percent!"
                }
                "Cinematic" -> when (targetLanguage) {
                    SupportedLanguage.SPANISH -> "En el corazón del bosque milenario, el tiempo se detuvo mientras la naturaleza revelaba sus secretos."
                    SupportedLanguage.FRENCH -> "Au cœur de la forêt millénaire, le temps s'est arrêté alors que la nature révélait ses secrets."
                    SupportedLanguage.GERMAN -> "Im Herzen des uralten Waldes stand die Zeit still, während die Natur ihre Geheimnisse enthüllte."
                    SupportedLanguage.JAPANESE -> "原生林の深部で、時が止まり、自然がその深遠な秘密を明かしました。"
                    else -> "In the heart of the ancient forest, time stood still as nature revealed its deepest secrets."
                }
                else -> when (targetLanguage) {
                    SupportedLanguage.SPANISH -> "Bienvenidos a esta demostración. Hoy estamos explorando el futuro de la síntesis de voz con IA."
                    SupportedLanguage.FRENCH -> "Bienvenue dans cette démonstration. Nous explorons aujourd'hui l'avenir du doublage par IA."
                    SupportedLanguage.GERMAN -> "Willkommen zu dieser Demonstration. Heute erkunden wir die Zukunft der KI-Sprachsynthese."
                    SupportedLanguage.JAPANESE -> "このデモへようこそ。今日はAI音声合成による高精度なダビングの未来を探究します。"
                    else -> "Welcome to this demonstration. Today we are exploring the future of AI-powered video dubbing."
                }
            }
        )
    }

    // Reset auditioning persona when audio engine stops
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking && !isComparingSequential) {
            auditioningPersona = null
        }
    }

    fun playAudition(persona: VoicePersona, onComplete: (() -> Unit)? = null) {
        auditioningPersona = persona
        val previewSettings = VoiceSettings(
            voicePersona = persona,
            pitch = persona.defaultPitch,
            speakingRate = persona.defaultSpeed,
            emotionalTone = EmotionalTone.NEUTRAL,
            voiceMatchSameAsVideo = (persona == VoicePersona.VIDEO_CLONE),
            speakerProfile = speakerProfile ?: SpeakerAcousticProfile.defaultForTechSpeaker()
        )
        audioEngine.speakPreview(
            text = customAuditionPhrase,
            settings = previewSettings,
            language = targetLanguage,
            onDone = {
                if (!isComparingSequential) {
                    auditioningPersona = null
                }
                onComplete?.invoke()
            }
        )
    }

    fun playDualSequence() {
        if (isSpeaking) {
            audioEngine.stop()
            isComparingSequential = false
            auditioningPersona = null
            return
        }

        isComparingSequential = true
        scope.launch {
            // Play Model A first
            playAudition(modelA) {
                scope.launch {
                    delay(400)
                    // Then play Model B
                    playAudition(modelB) {
                        isComparingSequential = false
                        auditioningPersona = null
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_model_comparison_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Voice Comparison",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Side-by-Side Voice Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "A / B COMPARE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Audition & compare voice models before setting primary narrator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mode Selector Tabs (Dual Compare vs All Models)
            TabRow(
                selectedTabIndex = compareModeTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("compare_mode_tab_row"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = compareModeTabIndex == 0,
                    onClick = { compareModeTabIndex = 0 },
                    text = {
                        Text(
                            text = "Dual Compare (A vs B)",
                            fontWeight = if (compareModeTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("dual_compare_tab")
                )
                Tab(
                    selected = compareModeTabIndex == 1,
                    onClick = { compareModeTabIndex = 1 },
                    text = {
                        Text(
                            text = "All Models Showcase",
                            fontWeight = if (compareModeTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("all_models_tab")
                )
            }

            // Audition Sample Sentence & Quick Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDITION PHRASE (${targetLanguage.flagEmoji} ${targetLanguage.displayName})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (isSpeaking) "🎙️ PLAYING" else "IDLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category Chips for quickly switching phrase styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Intro", "Cinematic", "Action").forEach { category ->
                        FilterChip(
                            selected = selectedPhraseCategory == category,
                            onClick = { selectedPhraseCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = customAuditionPhrase,
                    onValueChange = { customAuditionPhrase = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audition_phrase_input"),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // TAB CONTENT
            if (compareModeTabIndex == 0) {
                // ==========================================
                // VIEW 1: DUAL SIDE-BY-SIDE COMPARE (A vs B)
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Sequential Compare Action Bar
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Head-to-Head Comparison",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${modelA.displayName} vs ${modelB.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { playDualSequence() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isComparingSequential) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("compare_both_sequential_button")
                            ) {
                                Icon(
                                    imageVector = if (isComparingSequential) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isComparingSequential) "Stop" else "Play A then B",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Side-by-Side Dual Columns (Model A & Model B)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // COLUMN A: Voice Model A
                        DualVoiceColumn(
                            slotLabel = "MODEL A",
                            selectedPersona = modelA,
                            allPersonas = VoicePersona.entries,
                            onPersonaChanged = { modelA = it },
                            isPrimary = currentVoicePersona == modelA,
                            isCurrentlyAuditioning = auditioningPersona == modelA,
                            liveAmplitude = if (auditioningPersona == modelA) liveAmplitude else 0f,
                            onPlaySample = {
                                if (auditioningPersona == modelA && isSpeaking) {
                                    audioEngine.stop()
                                    auditioningPersona = null
                                } else {
                                    playAudition(modelA)
                                }
                            },
                            onSelectAsPrimary = {
                                onSelectPrimaryVoice(modelA)
                            },
                            modifier = Modifier.weight(1f),
                            testTagPrefix = "dual_model_a"
                        )

                        // COLUMN B: Voice Model B
                        DualVoiceColumn(
                            slotLabel = "MODEL B",
                            selectedPersona = modelB,
                            allPersonas = VoicePersona.entries,
                            onPersonaChanged = { modelB = it },
                            isPrimary = currentVoicePersona == modelB,
                            isCurrentlyAuditioning = auditioningPersona == modelB,
                            liveAmplitude = if (auditioningPersona == modelB) liveAmplitude else 0f,
                            onPlaySample = {
                                if (auditioningPersona == modelB && isSpeaking) {
                                    audioEngine.stop()
                                    auditioningPersona = null
                                } else {
                                    playAudition(modelB)
                                }
                            },
                            onSelectAsPrimary = {
                                onSelectPrimaryVoice(modelB)
                            },
                            modifier = Modifier.weight(1f),
                            testTagPrefix = "dual_model_b"
                        )
                    }

                    // Pitch & Timbre Delta Comparison Summary
                    val pitchDiff = ((modelB.defaultPitch - modelA.defaultPitch) * 100).toInt()
                    val speedDiff = ((modelB.defaultSpeed - modelA.defaultSpeed) * 100).toInt()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚖️ Contrast: ${modelA.displayName} (${modelA.vocalTimbre}) vs ${modelB.displayName} (${modelB.vocalTimbre})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    pitchDiff > 0 -> "+$pitchDiff% higher pitch"
                                    pitchDiff < 0 -> "${-pitchDiff}% deeper pitch"
                                    else -> "Identical baseline pitch"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // VIEW 2: ALL MODELS SIDE-BY-SIDE SHOWCASE
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SWIPE TO PREVIEW & COMPARE ALL VOICES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        VoicePersona.entries.forEach { persona ->
                            val isPrimary = currentVoicePersona == persona
                            val isThisAuditioning = auditioningPersona == persona && isSpeaking

                            ModelCardItem(
                                persona = persona,
                                isPrimary = isPrimary,
                                isAuditioning = isThisAuditioning,
                                liveAmplitude = if (isThisAuditioning) liveAmplitude else 0f,
                                onPlay = {
                                    if (isThisAuditioning) {
                                        audioEngine.stop()
                                        auditioningPersona = null
                                    } else {
                                        playAudition(persona)
                                    }
                                },
                                onSelectAsPrimary = {
                                    onSelectPrimaryVoice(persona)
                                },
                                modifier = Modifier
                                    .width(220.dp)
                                    .testTag("model_card_${persona.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual column for the Dual A vs B comparison view.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DualVoiceColumn(
    slotLabel: String,
    selectedPersona: VoicePersona,
    allPersonas: List<VoicePersona>,
    onPersonaChanged: (VoicePersona) -> Unit,
    isPrimary: Boolean,
    isCurrentlyAuditioning: Boolean,
    liveAmplitude: Float,
    onPlaySample: () -> Unit,
    onSelectAsPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "dual_model"
) {
    val borderColor by animateColorAsState(
        targetValue = if (isPrimary) {
            MaterialTheme.colorScheme.primary
        } else if (isCurrentlyAuditioning) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(if (isPrimary || isCurrentlyAuditioning) 2.dp else 1.dp, borderColor),
        modifier = modifier.testTag("${testTagPrefix}_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Slot Label & Current Primary Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isPrimary) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "PRIMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Persona Selector Dropdown / Chips in a compact flow row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allPersonas.forEach { persona ->
                    val isChipSelected = persona == selectedPersona
                    FilterChip(
                        selected = isChipSelected,
                        onClick = { onPersonaChanged(persona) },
                        label = {
                            Text(
                                text = persona.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Persona Profile Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedPersona.avatarEmoji,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = selectedPersona.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedPersona.vocalTimbre,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Pitch & Rate badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pitch: ${selectedPersona.defaultPitch}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rate: ${selectedPersona.defaultSpeed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mini visualizer bar when speaking
            AudioWaveformVisualizer(
                isSpeaking = isCurrentlyAuditioning,
                amplitude = liveAmplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            )

            // Listen sample button
            Button(
                onClick = onPlaySample,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("${testTagPrefix}_listen_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentlyAuditioning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isCurrentlyAuditioning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCurrentlyAuditioning) "Stop Sample" else "Listen Sample",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Select as Primary Button
            if (!isPrimary) {
                OutlinedButton(
                    onClick = onSelectAsPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("${testTagPrefix}_select_primary_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Use for Dubbing",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Selected Primary",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual card item in the All Models Showcase row.
 */
@Composable
private fun ModelCardItem(
    persona: VoicePersona,
    isPrimary: Boolean,
    isAuditioning: Boolean,
    liveAmplitude: Float,
    onPlay: () -> Unit,
    onSelectAsPrimary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isPrimary) {
            MaterialTheme.colorScheme.primary
        } else if (isAuditioning) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(200),
        label = "itemBorder"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(if (isPrimary || isAuditioning) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Avatar & Primary Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAuditioning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = persona.avatarEmoji,
                        fontSize = 22.sp
                    )
                }

                if (isPrimary) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Name & Vocal Timbre
            Column {
                Text(
                    text = persona.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = persona.vocalTimbre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Best for tag
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = persona.bestFor,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Pitch & Speed Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pitch: ${persona.defaultPitch}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Speed: ${persona.defaultSpeed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mini Waveform Visualizer
            AudioWaveformVisualizer(
                isSpeaking = isAuditioning,
                amplitude = liveAmplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            // Play Sample Button
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("audition_voice_${persona.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAuditioning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isAuditioning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAuditioning) "Stop Sample" else "Listen Sample",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Select as Primary Button
            if (!isPrimary) {
                OutlinedButton(
                    onClick = onSelectAsPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("select_primary_${persona.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Set as Primary",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Selected Primary",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
