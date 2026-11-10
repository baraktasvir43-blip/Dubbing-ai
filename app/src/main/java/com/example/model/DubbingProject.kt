package com.example.model

data class ScriptCue(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val originalText: String,
    var translatedText: String,
    val speaker: String = "Speaker 1",
    val emotion: EmotionalTone = EmotionalTone.NEUTRAL,
    var pitchOverride: Float? = null,
    var speedOverride: Float? = null
) {
    val durationMs: Long get() = endTimeMs - startTimeMs

    fun formatTimestamp(): String {
        val startSec = startTimeMs / 1000
        val startMin = startSec / 60
        val startSecRem = startSec % 60

        val endSec = endTimeMs / 1000
        val endMin = endSec / 60
        val endSecRem = endSec % 60

        return String.format("%02d:%02d - %02d:%02d", startMin, startSecRem, endMin, endSecRem)
    }
}

enum class AudioMixMode(val displayName: String, val subtitle: String) {
    DUCKED_ORIGINAL("Ducked Mix (15% Original)", "Broadcast style: original audio softly in background, clear dubbed voice"),
    DUBBED_ONLY("Dubbed Voice Only", "100% AI synthesized voice, original audio completely muted"),
    ORIGINAL_ONLY("Original Audio Only", "Original video audio track with translated subtitle captions"),
    SPLIT_STEREO("Dual Audio Compare", "Original voice in left ear, dubbed translation in right ear")
}

data class DubbingProject(
    val id: String,
    val title: String,
    val description: String,
    val videoCategory: String,
    val durationMs: Long,
    val videoUri: String? = null,
    val originalLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    var targetLanguage: SupportedLanguage = SupportedLanguage.SPANISH,
    var voiceSettings: VoiceSettings = VoiceSettings(),
    var scriptCues: List<ScriptCue> = emptyList(),
    var originalAudioVolume: Float = 0.18f,
    var dubbedAudioVolume: Float = 1.0f,
    var audioMixMode: AudioMixMode = AudioMixMode.DUCKED_ORIGINAL,
    var isDubbingProcessed: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)
