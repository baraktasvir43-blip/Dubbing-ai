package com.example.audio

import com.example.model.EmotionalTone
import com.example.model.SpeakerAcousticProfile
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import kotlinx.coroutines.delay

object VoiceAcousticAnalyzer {

    data class AnalysisResult(
        val speakerProfile: SpeakerAcousticProfile,
        val recommendedPersona: VoicePersona,
        val matchedVoiceSettings: VoiceSettings,
        val matchConfidence: Float, // e.g. 0.94 (94%)
        val frequencySpectrum: List<Float>
    )

    /**
     * Simulates scanning and analyzing the audio track of a video to extract
     * the speaker's vocal acoustics (fundamental pitch F0, cadence, timbre, and emotion).
     */
    suspend fun analyzeVideoVoice(
        videoCategory: String,
        videoTitle: String,
        onProgress: (Float, String) -> Unit
    ): AnalysisResult {
        onProgress(0.15f, "Isolating primary vocal track from background noise...")
        delay(350)
        onProgress(0.40f, "Computing fundamental frequency (F0) & formant resonances...")
        delay(400)
        onProgress(0.70f, "Analyzing prosody, cadence, and emotional tone...")
        delay(350)
        onProgress(0.95f, "Synthesizing matching acoustic clone profile...")
        delay(250)

        val profile: SpeakerAcousticProfile = when {
            videoCategory.contains("Tech", ignoreCase = true) || videoTitle.contains("Quantum", ignoreCase = true) -> {
                SpeakerAcousticProfile(
                    speakerName = "David (Keynote Presenter)",
                    genderTimbre = "Resonant Baritone",
                    fundamentalPitchHz = 124,
                    targetPitchFactor = 0.94f,
                    speechRateWpm = 148,
                    targetSpeedFactor = 1.08f,
                    dynamicEnergy = "High Professional Clarity",
                    detectedEmotion = EmotionalTone.ENTHUSIASTIC
                )
            }
            videoCategory.contains("Travel", ignoreCase = true) || videoTitle.contains("Kyoto", ignoreCase = true) -> {
                SpeakerAcousticProfile(
                    speakerName = "Maya (Travel Vlogger)",
                    genderTimbre = "Warm Mezzo-Soprano",
                    fundamentalPitchHz = 215,
                    targetPitchFactor = 1.18f,
                    speechRateWpm = 138,
                    targetSpeedFactor = 1.04f,
                    dynamicEnergy = "Vibrant & Story-driven",
                    detectedEmotion = EmotionalTone.WARM_FRIENDLY
                )
            }
            videoCategory.contains("Doc", ignoreCase = true) || videoTitle.contains("Patagonia", ignoreCase = true) -> {
                SpeakerAcousticProfile(
                    speakerName = "Julian (Nature Narrator)",
                    genderTimbre = "Deep Bass Narrator",
                    fundamentalPitchHz = 96,
                    targetPitchFactor = 0.82f,
                    speechRateWpm = 118,
                    targetSpeedFactor = 0.92f,
                    dynamicEnergy = "Cinematic & Contemplative",
                    detectedEmotion = EmotionalTone.DRAMATIC
                )
            }
            videoCategory.contains("Cook", ignoreCase = true) || videoTitle.contains("Pasta", ignoreCase = true) -> {
                SpeakerAcousticProfile(
                    speakerName = "Chef Marco (Culinary Master)",
                    genderTimbre = "Dynamic Tenor",
                    fundamentalPitchHz = 142,
                    targetPitchFactor = 1.05f,
                    speechRateWpm = 152,
                    targetSpeedFactor = 1.12f,
                    dynamicEnergy = "Passionate & Fast-Paced",
                    detectedEmotion = EmotionalTone.FAST_PACED
                )
            }
            else -> {
                SpeakerAcousticProfile(
                    speakerName = "Original Video Speaker",
                    genderTimbre = "Balanced Natural Vocal",
                    fundamentalPitchHz = 135,
                    targetPitchFactor = 1.0f,
                    speechRateWpm = 140,
                    targetSpeedFactor = 1.0f,
                    dynamicEnergy = "Standard Dynamic Range",
                    detectedEmotion = EmotionalTone.NEUTRAL
                )
            }
        }

        val persona = when (profile.genderTimbre) {
            "Deep Bass Narrator" -> VoicePersona.MARCUS
            "Warm Mezzo-Soprano" -> VoicePersona.SOPHIA
            "Resonant Baritone" -> VoicePersona.ALEX
            else -> VoicePersona.VIDEO_CLONE
        }

        val settings = VoiceSettings(
            voicePersona = persona,
            pitch = profile.targetPitchFactor,
            speakingRate = profile.targetSpeedFactor,
            emotionalTone = profile.detectedEmotion,
            voiceMatchSameAsVideo = true,
            speakerProfile = profile,
            customVoiceLabel = "Video Voice Clone (${profile.speakerName})"
        )

        val spectrum = listOf(
            0.25f, 0.45f, 0.82f, 0.95f, 0.72f, 0.60f, 0.48f, 0.35f,
            0.55f, 0.88f, 0.92f, 0.65f, 0.50f, 0.40f, 0.30f, 0.20f
        )

        return AnalysisResult(
            speakerProfile = profile,
            recommendedPersona = persona,
            matchedVoiceSettings = settings,
            matchConfidence = 0.94f,
            frequencySpectrum = spectrum
        )
    }
}
