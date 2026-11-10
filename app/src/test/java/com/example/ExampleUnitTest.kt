package com.example

import com.example.audio.VoiceAcousticAnalyzer
import com.example.data.SampleVideosRepository
import com.example.model.EmotionalTone
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.model.avatarEmoji
import com.example.model.bestFor
import com.example.model.getSamplePhrase
import com.example.model.vocalTimbre
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVoiceSettingsEffectivePitchAndSpeed() {
        val settings = VoiceSettings(
            pitch = 1.0f,
            speakingRate = 1.0f,
            emotionalTone = EmotionalTone.ENTHUSIASTIC,
            voiceMatchSameAsVideo = false
        )

        // Enthusiastic has pitchMod = 0.15f, speedMod = 0.12f
        assertEquals(1.15f, settings.effectivePitch(), 0.001f)
        assertEquals(1.12f, settings.effectiveSpeakingRate(), 0.001f)
    }

    @Test
    fun testVoiceSettingsWithVoiceMatchSameAsVideo() {
        val speaker = SpeakerAcousticProfile.defaultForDocumentary()
        val settings = VoiceSettings(
            pitch = 1.0f,
            speakingRate = 1.0f,
            emotionalTone = EmotionalTone.NEUTRAL,
            voiceMatchSameAsVideo = true,
            speakerProfile = speaker
        )

        // When voiceMatchSameAsVideo is true, base pitch/speed comes from speaker profile
        assertEquals(speaker.targetPitchFactor, settings.effectivePitch(), 0.001f)
        assertEquals(speaker.targetSpeedFactor, settings.effectiveSpeakingRate(), 0.001f)
    }

    @Test
    fun testVoiceAcousticAnalyzerMatchesVideoSpeaker() = runBlocking {
        val result = VoiceAcousticAnalyzer.analyzeVideoVoice(
            videoCategory = "Tech & Science",
            videoTitle = "Quantum Horizons"
        ) { _, _ -> }

        assertNotNull(result.speakerProfile)
        assertTrue(result.matchConfidence >= 0.90f)
        assertTrue(result.matchedVoiceSettings.voiceMatchSameAsVideo)
        assertEquals(VoicePersona.ALEX, result.recommendedPersona)
    }

    @Test
    fun testSampleVideosRepositoryTranslations() {
        val projects = SampleVideosRepository.getSampleProjects()
        assertTrue(projects.isNotEmpty())

        val techProject = projects.first()
        val spanishCues = SampleVideosRepository.getTranslatedCues(techProject, SupportedLanguage.SPANISH)
        assertEquals(techProject.scriptCues.size, spanishCues.size)
        assertTrue(spanishCues.first().translatedText.contains("Bienvenidos"))
    }

    @Test
    fun testLanguageSearchFiltering() {
        val allLanguages = SupportedLanguage.entries
        assertTrue(allLanguages.size >= 25)

        // Search by English display name "jap"
        val japaneseResults = allLanguages.filter {
            it.displayName.contains("jap", ignoreCase = true) ||
            it.nativeName.contains("jap", ignoreCase = true) ||
            it.code.equals("jap", ignoreCase = true)
        }
        assertTrue(japaneseResults.contains(SupportedLanguage.JAPANESE))

        // Search by Native script "Español"
        val spanishResults = allLanguages.filter {
            it.displayName.contains("Español", ignoreCase = true) ||
            it.nativeName.contains("Español", ignoreCase = true) ||
            it.code.equals("Español", ignoreCase = true)
        }
        assertTrue(spanishResults.contains(SupportedLanguage.SPANISH))

        // Search by ISO Code "de"
        val germanResults = allLanguages.filter {
            it.displayName.contains("de", ignoreCase = true) ||
            it.nativeName.contains("de", ignoreCase = true) ||
            it.code.equals("de", ignoreCase = true)
        }
        assertTrue(germanResults.contains(SupportedLanguage.GERMAN))
    }

    @Test
    fun testSrtSubtitleGeneration() {
        val sampleProject = SampleVideosRepository.getSampleProjects().first()
        val srtOutput = com.example.ui.screens.generateSrtSubtitles(sampleProject.scriptCues)
        assertTrue(srtOutput.contains("1"))
        assertTrue(srtOutput.contains("00:00:00,000 --> 00:00:04,500"))
        assertTrue(srtOutput.contains(sampleProject.scriptCues.first().translatedText))
    }

    @Test
    fun testVoicePersonaAcousticMetadataAndSamplePhrases() {
        val personas = VoicePersona.entries
        assertTrue(personas.size >= 5)

        personas.forEach { persona ->
            assertTrue(persona.vocalTimbre.isNotEmpty())
            assertTrue(persona.bestFor.isNotEmpty())
            assertTrue(persona.avatarEmoji.isNotEmpty())

            // Test sample phrase generation across different languages
            val spanishPhrase = persona.getSamplePhrase(SupportedLanguage.SPANISH)
            val englishPhrase = persona.getSamplePhrase(SupportedLanguage.ENGLISH)
            val japanesePhrase = persona.getSamplePhrase(SupportedLanguage.JAPANESE)

            assertTrue(spanishPhrase.isNotEmpty())
            assertTrue(englishPhrase.isNotEmpty())
            assertTrue(japanesePhrase.isNotEmpty())
        }
    }

    @Test
    fun testVoicePersonaComparisonDelta() {
        val alex = VoicePersona.ALEX
        val sophia = VoicePersona.SOPHIA
        val marcus = VoicePersona.MARCUS

        // Sophia has higher pitch than Alex
        assertTrue(sophia.defaultPitch > alex.defaultPitch)
        // Marcus has lower pitch (deep bass) than Alex
        assertTrue(marcus.defaultPitch < alex.defaultPitch)

        val pitchDiff = kotlin.math.round(((sophia.defaultPitch - alex.defaultPitch) * 100)).toInt()
        assertEquals(15, pitchDiff) // +15%
    }
}
