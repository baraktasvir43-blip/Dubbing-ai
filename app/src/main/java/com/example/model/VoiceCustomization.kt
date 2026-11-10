package com.example.model

enum class VoicePersona(
    val id: String,
    val displayName: String,
    val description: String,
    val gender: String,
    val defaultPitch: Float,
    val defaultSpeed: Float
) {
    ALEX("alex", "Alex", "Warm & balanced studio host", "Male", 1.0f, 1.0f),
    SOPHIA("sophia", "Sophia", "Expressive & articulate narrator", "Female", 1.15f, 1.0f),
    MARCUS("marcus", "Marcus", "Deep, resonant & authoritative", "Male", 0.85f, 0.95f),
    ELENA("elena", "Elena", "Bright, clear & conversational", "Female", 1.25f, 1.05f),
    KENJI("kenji", "Kenji", "Crisp, dynamic & energetic", "Male", 1.05f, 1.1f),
    VIDEO_CLONE("video_clone", "Same as Video", "Acoustic clone of original video speaker", "Matched", 1.0f, 1.0f)
}

enum class EmotionalTone(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String,
    val pitchMod: Float,
    val speedMod: Float
) {
    NEUTRAL("neutral", "Neutral", "🎯", "Balanced, clear, and professional", 0.0f, 0.0f),
    ENTHUSIASTIC("enthusiastic", "Enthusiastic", "✨", "High energy, upbeat, and inspiring", 0.15f, 0.12f),
    WARM_FRIENDLY("warm_friendly", "Warm & Friendly", "😊", "Inviting, empathetic, and gentle", 0.05f, -0.05f),
    DRAMATIC("dramatic", "Dramatic", "🎭", "Deep, serious, and captivating", -0.15f, -0.10f),
    FAST_PACED("fast_paced", "Fast-Paced", "⚡", "Rapid, dynamic, and action-oriented", 0.10f, 0.25f),
    WHISPER_CALM("whisper_calm", "Calm & Relaxed", "🌿", "Soothing, contemplative, and quiet", -0.08f, -0.15f),
    AUTHORITATIVE("authoritative", "Authoritative", "📢", "Commanding, firm, and informative", -0.10f, 0.05f)
}

data class SpeakerAcousticProfile(
    val speakerName: String,
    val genderTimbre: String,
    val fundamentalPitchHz: Int,
    val targetPitchFactor: Float,
    val speechRateWpm: Int,
    val targetSpeedFactor: Float,
    val dynamicEnergy: String,
    val detectedEmotion: EmotionalTone
) {
    companion object {
        fun defaultForTechSpeaker() = SpeakerAcousticProfile(
            speakerName = "David (Tech Presenter)",
            genderTimbre = "Resonant Baritone",
            fundamentalPitchHz = 126,
            targetPitchFactor = 0.92f,
            speechRateWpm = 145,
            targetSpeedFactor = 1.08f,
            dynamicEnergy = "High Dynamic Range",
            detectedEmotion = EmotionalTone.ENTHUSIASTIC
        )

        fun defaultForTravelVlogger() = SpeakerAcousticProfile(
            speakerName = "Maya (Travel Host)",
            genderTimbre = "Warm Mezzo-Soprano",
            fundamentalPitchHz = 210,
            targetPitchFactor = 1.18f,
            speechRateWpm = 135,
            targetSpeedFactor = 1.02f,
            dynamicEnergy = "Expressive & Melodic",
            detectedEmotion = EmotionalTone.WARM_FRIENDLY
        )

        fun defaultForDocumentary() = SpeakerAcousticProfile(
            speakerName = "Julian (Nature Narrator)",
            genderTimbre = "Deep Bass Narrator",
            fundamentalPitchHz = 98,
            targetPitchFactor = 0.82f,
            speechRateWpm = 120,
            targetSpeedFactor = 0.90f,
            dynamicEnergy = "Smooth & Cinematic",
            detectedEmotion = EmotionalTone.DRAMATIC
        )
    }
}

data class VoiceSettings(
    val voicePersona: VoicePersona = VoicePersona.ALEX,
    val pitch: Float = 1.0f,
    val speakingRate: Float = 1.0f,
    val emotionalTone: EmotionalTone = EmotionalTone.NEUTRAL,
    val voiceMatchSameAsVideo: Boolean = false,
    val speakerProfile: SpeakerAcousticProfile = SpeakerAcousticProfile.defaultForTechSpeaker(),
    val customVoiceLabel: String = "AI Synthesizer v3"
) {
    /**
     * Calculates effective pitch taking into account base pitch, emotion modulation,
     * and voice matching.
     */
    fun effectivePitch(): Float {
        val base = if (voiceMatchSameAsVideo) {
            speakerProfile.targetPitchFactor
        } else {
            pitch
        }
        val modified = base + emotionalTone.pitchMod
        return modified.coerceIn(0.5f, 2.0f)
    }

    /**
     * Calculates effective speed taking into account base speed, emotion modulation,
     * and voice matching.
     */
    fun effectiveSpeakingRate(): Float {
        val base = if (voiceMatchSameAsVideo) {
            speakerProfile.targetSpeedFactor
        } else {
            speakingRate
        }
        val modified = base + emotionalTone.speedMod
        return modified.coerceIn(0.5f, 2.5f)
    }
}

val VoicePersona.vocalTimbre: String
    get() = when (this) {
        VoicePersona.ALEX -> "Balanced Natural Baritone"
        VoicePersona.SOPHIA -> "Warm Mezzo-Soprano"
        VoicePersona.MARCUS -> "Deep Cinematic Bass"
        VoicePersona.ELENA -> "Clear Bright Soprano"
        VoicePersona.KENJI -> "Crisp Dynamic Tenor"
        VoicePersona.VIDEO_CLONE -> "Acoustic Speaker Clone"
    }

val VoicePersona.bestFor: String
    get() = when (this) {
        VoicePersona.ALEX -> "Tutorials, Tech & News"
        VoicePersona.SOPHIA -> "Travel, Vlogs & Lifestyle"
        VoicePersona.MARCUS -> "Documentaries & Cinema"
        VoicePersona.ELENA -> "Stories & Explanations"
        VoicePersona.KENJI -> "Gaming & Fast Action"
        VoicePersona.VIDEO_CLONE -> "Authentic Video Match"
    }

val VoicePersona.avatarEmoji: String
    get() = when (this) {
        VoicePersona.ALEX -> "🎙️"
        VoicePersona.SOPHIA -> "✨"
        VoicePersona.MARCUS -> "🏛️"
        VoicePersona.ELENA -> "🌟"
        VoicePersona.KENJI -> "⚡"
        VoicePersona.VIDEO_CLONE -> "🎞️"
    }

fun VoicePersona.getSamplePhrase(language: SupportedLanguage): String {
    return when (language) {
        SupportedLanguage.SPANISH -> when (this) {
            VoicePersona.ALEX -> "Hola, soy Alex. Mi voz equilibrada y natural es ideal para tutoriales y presentaciones tecnológicas."
            VoicePersona.SOPHIA -> "Hola, soy Sophia. Mi tono cálido y expresivo aporta empatía y dinamismo a tus videos."
            VoicePersona.MARCUS -> "Saludos, soy Marcus. Una voz profunda y autoritaria perfecta para documentales y narraciones."
            VoicePersona.ELENA -> "¡Hola! Soy Elena. Con claridad y brillo conversacional, tus historias cobrarán vida."
            VoicePersona.KENJI -> "¡Hey! Soy Kenji. Energía rápida, dicción precisa y ritmo ágil para contenido dinámico."
            VoicePersona.VIDEO_CLONE -> "Esta es una síntesis acústica clonada directamente del hablante original del video."
        }
        SupportedLanguage.FRENCH -> when (this) {
            VoicePersona.ALEX -> "Bonjour, je suis Alex. Ma voix claire et naturelle convient parfaitement aux explications techniques."
            VoicePersona.SOPHIA -> "Bonjour, je suis Sophia. Une tonalité chaleureuse et captivante pour enrichir vos récits."
            VoicePersona.MARCUS -> "Salutations, je suis Marcus. Une voix grave et résonnante pour les récits documentaires."
            VoicePersona.ELENA -> "Bonjour ! Je suis Elena. Un ton lumineux et engageant pour captiver votre audience."
            VoicePersona.KENJI -> "Salut ! Je suis Kenji. Dynamisme, clarté et rythme soutenu pour vos séquences d'action."
            VoicePersona.VIDEO_CLONE -> "Synthèse vocale synchronisée et clonée d'après l'acoustique originale de la vidéo."
        }
        SupportedLanguage.GERMAN -> when (this) {
            VoicePersona.ALEX -> "Hallo, ich bin Alex. Eine präzise und ausgewogene Stimme für moderne Technik und Anleitungen."
            VoicePersona.SOPHIA -> "Hallo, ich bin Sophia. Mit warmer Ausstrahlung verleihe ich Ihren Videos Lebendigkeit."
            VoicePersona.MARCUS -> "Guten Tag, ich bin Marcus. Tiefe Resonanz und Autorität für Dokumentarfilme und Berichte."
            VoicePersona.ELENA -> "Hallo! Ich bin Elena. Klar und ausdrucksstark, um jede Botschaft sympathisch zu vermitteln."
            VoicePersona.KENJI -> "Hi! Ich bin Kenji. Schnelle Aussprache und kraftvoller Rhythmus für dynamische Sequenzen."
            VoicePersona.VIDEO_CLONE -> "Akustischer Klon der Originalstimme für nahtlose Synchronisation."
        }
        SupportedLanguage.JAPANESE -> when (this) {
            VoicePersona.ALEX -> "こんにちは、アレックスです。バランスの取れた明瞭な声で解説やニュースをお届けします。"
            VoicePersona.SOPHIA -> "こんにちは、ソフィアです。温かく表現力豊かな声で、魅力的なストーリーを彩ります。"
            VoicePersona.MARCUS -> "マーカスです。深く響く落ち着いた声で、ドキュメンタリー映像に最適です。"
            VoicePersona.ELENA -> "エレナです！明るく親しみやすいトーンで、リスナーの心にしっかり届けます。"
            VoicePersona.KENJI -> "ケンジです！シャープで勢いのあるテンポで、エネルギッシュな動画を盛り上げます。"
            VoicePersona.VIDEO_CLONE -> "元の動画の音響特性を抽出したリアルタイム音声クローンモデルです。"
        }
        else -> when (this) {
            VoicePersona.ALEX -> "Hello, I'm Alex. My balanced, natural studio voice is great for tutorials, explainers, and presentations."
            VoicePersona.SOPHIA -> "Hello, I'm Sophia. My warm, expressive delivery brings empathy and clarity to your videos."
            VoicePersona.MARCUS -> "Greetings, I'm Marcus. A deep, resonant bass voice built for documentaries and cinematic narration."
            VoicePersona.ELENA -> "Hi there! I'm Elena. Bright, conversational, and energetic to make any story unforgettable."
            VoicePersona.KENJI -> "Hey! I'm Kenji. Crisp articulation, rapid pacing, and punchy delivery for high-energy content."
            VoicePersona.VIDEO_CLONE -> "This acoustic clone matches the original video speaker's vocal pitch and cadence."
        }
    }
}
