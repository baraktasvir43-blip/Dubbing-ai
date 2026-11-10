package com.example.data

import com.example.model.AudioMixMode
import com.example.model.DubbingProject
import com.example.model.EmotionalTone
import com.example.model.ScriptCue
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings

object SampleVideosRepository {

    fun getSampleProjects(): List<DubbingProject> {
        return listOf(
            createTechProject(),
            createTravelProject(),
            createDocumentaryProject(),
            createCulinaryProject()
        )
    }

    private fun createTechProject(): DubbingProject {
        val cues = listOf(
            ScriptCue(
                id = "tech_1",
                startTimeMs = 0L,
                endTimeMs = 4500L,
                originalText = "Welcome everyone. Today we are unlocking a completely new paradigm in computing.",
                translatedText = "Bienvenidos a todos. Hoy estamos revelando un paradigma completamente nuevo en la computación.",
                speaker = "David",
                emotion = EmotionalTone.ENTHUSIASTIC
            ),
            ScriptCue(
                id = "tech_2",
                startTimeMs = 4800L,
                endTimeMs = 9500L,
                originalText = "By harnessing quantum entanglement, operations that once took millennia now run in seconds.",
                translatedText = "Al aprovechar el entrelazamiento cuántico, operaciones que antes tardaban milenios ahora se ejecutan en segundos.",
                speaker = "David",
                emotion = EmotionalTone.AUTHORITATIVE
            ),
            ScriptCue(
                id = "tech_3",
                startTimeMs = 9800L,
                endTimeMs = 15200L,
                originalText = "Notice the qubit coherence graph on the screen. The stability matrix has improved five-fold.",
                translatedText = "Observen el gráfico de coherencia de cúbits en pantalla. La matriz de estabilidad ha mejorado cinco veces.",
                speaker = "David",
                emotion = EmotionalTone.NEUTRAL
            ),
            ScriptCue(
                id = "tech_4",
                startTimeMs = 15600L,
                endTimeMs = 21000L,
                originalText = "This breakthrough will transform medical drug discovery, climate simulations, and cryptography.",
                translatedText = "Este avance transformará el descubrimiento de fármacos médicos, las simulaciones climáticas y la criptografía.",
                speaker = "David",
                emotion = EmotionalTone.ENTHUSIASTIC
            ),
            ScriptCue(
                id = "tech_5",
                startTimeMs = 21400L,
                endTimeMs = 26500L,
                originalText = "The era of practical quantum supremacy is no longer a distant theoretical dream. It is here today.",
                translatedText = "La era de la supremacía cuántica práctica ya no es un sueño teórico lejano. Está aquí hoy.",
                speaker = "David",
                emotion = EmotionalTone.ENTHUSIASTIC
            )
        )

        val speaker = SpeakerAcousticProfile.defaultForTechSpeaker()
        val voiceSettings = VoiceSettings(
            voicePersona = VoicePersona.ALEX,
            pitch = speaker.targetPitchFactor,
            speakingRate = speaker.targetSpeedFactor,
            emotionalTone = EmotionalTone.ENTHUSIASTIC,
            voiceMatchSameAsVideo = true,
            speakerProfile = speaker,
            customVoiceLabel = "David Clone (Keynote Baritone)"
        )

        return DubbingProject(
            id = "proj_quantum_tech",
            title = "Quantum Horizons: The Future of Computing",
            description = "Keynote presentation detailing breakthrough quantum coherence and algorithmic acceleration.",
            videoCategory = "Tech & Science",
            durationMs = 27000L,
            originalLanguage = SupportedLanguage.ENGLISH,
            targetLanguage = SupportedLanguage.SPANISH,
            voiceSettings = voiceSettings,
            scriptCues = cues,
            originalAudioVolume = 0.15f,
            dubbedAudioVolume = 1.0f,
            audioMixMode = AudioMixMode.DUCKED_ORIGINAL,
            isDubbingProcessed = true
        )
    }

    private fun createTravelProject(): DubbingProject {
        val cues = listOf(
            ScriptCue(
                id = "travel_1",
                startTimeMs = 0L,
                endTimeMs = 4200L,
                originalText = "Stepping into the Arashiyama bamboo grove feels like crossing into another world entirely.",
                translatedText = "Entrer dans la bambouseraie d'Arashiyama, c'est comme pénétrer dans un monde totalement différent.",
                speaker = "Maya",
                emotion = EmotionalTone.WARM_FRIENDLY
            ),
            ScriptCue(
                id = "travel_2",
                startTimeMs = 4500L,
                endTimeMs = 9200L,
                originalText = "The wind rustling through the towering stalks creates a melody the Japanese call 'soundscape of tranquility'.",
                translatedText = "Le vent qui bruisse à travers les hautes tiges crée une mélodie que les Japonais appellent un paysage sonore de tranquillité.",
                speaker = "Maya",
                emotion = EmotionalTone.WHISPER_CALM
            ),
            ScriptCue(
                id = "travel_3",
                startTimeMs = 9500L,
                endTimeMs = 15000L,
                originalText = "Just around this winding stone path lies a centuries-old tea master's sanctuary.",
                translatedText = "Juste au bout de ce sentier de pierre sinueux se trouve le sanctuaire d'un maître de thé séculaire.",
                speaker = "Maya",
                emotion = EmotionalTone.WARM_FRIENDLY
            ),
            ScriptCue(
                id = "travel_4",
                startTimeMs = 15300L,
                endTimeMs = 20500L,
                originalText = "Let's savor a bowl of fresh ceremonial matcha while listening to the garden water fountain.",
                translatedText = "Savourons un bol de matcha cérémoniel frais tout en écoutant la fontaine d'eau du jardin.",
                speaker = "Maya",
                emotion = EmotionalTone.ENTHUSIASTIC
            )
        )

        val speaker = SpeakerAcousticProfile.defaultForTravelVlogger()
        val voiceSettings = VoiceSettings(
            voicePersona = VoicePersona.SOPHIA,
            pitch = speaker.targetPitchFactor,
            speakingRate = speaker.targetSpeedFactor,
            emotionalTone = EmotionalTone.WARM_FRIENDLY,
            voiceMatchSameAsVideo = true,
            speakerProfile = speaker,
            customVoiceLabel = "Maya Clone (Warm Vlogger)"
        )

        return DubbingProject(
            id = "proj_kyoto_travel",
            title = "Kyoto Zen: The Hidden Bamboo Pathways",
            description = "Immersive travel vlog exploring morning tranquility in historic Arashiyama, Kyoto.",
            videoCategory = "Travel & Culture",
            durationMs = 21000L,
            originalLanguage = SupportedLanguage.ENGLISH,
            targetLanguage = SupportedLanguage.FRENCH,
            voiceSettings = voiceSettings,
            scriptCues = cues,
            originalAudioVolume = 0.20f,
            dubbedAudioVolume = 1.0f,
            audioMixMode = AudioMixMode.DUCKED_ORIGINAL,
            isDubbingProcessed = true
        )
    }

    private fun createDocumentaryProject(): DubbingProject {
        val cues = listOf(
            ScriptCue(
                id = "doc_1",
                startTimeMs = 0L,
                endTimeMs = 5000L,
                originalText = "At the southern tip of the Andes, colossal walls of blue glacial ice meet the wild sea.",
                translatedText = "Am südlichen Ende der Anden treffen kolossale Wände aus blauem Gletschereis auf das wilde Meer.",
                speaker = "Julian",
                emotion = EmotionalTone.DRAMATIC
            ),
            ScriptCue(
                id = "doc_2",
                startTimeMs = 5400L,
                endTimeMs = 11200L,
                originalText = "Fierce Antarctic winds sweep across the fjords at over one hundred kilometers per hour.",
                translatedText = "Heftige antarktische Winde fegen mit über einhundert Kilometern pro Stunde über die Fjorde.",
                speaker = "Julian",
                emotion = EmotionalTone.DRAMATIC
            ),
            ScriptCue(
                id = "doc_3",
                startTimeMs = 11600L,
                endTimeMs = 17500L,
                originalText = "Yet in this unforgiving wilderness, Guanacos and Andean condors reign with effortless grace.",
                translatedText = "Dennoch herrschen Guanakos und Andenkondore in dieser unerbittlichen Wildnis mit müheloser Anmut.",
                speaker = "Julian",
                emotion = EmotionalTone.AUTHORITATIVE
            ),
            ScriptCue(
                id = "doc_4",
                startTimeMs = 18000L,
                endTimeMs = 23800L,
                originalText = "This is Patagonia: the untouched frontier where earth and climate write their epic story.",
                translatedText = "Das ist Patagonien: die unberührte Grenze, an der Erde und Klima ihre epische Geschichte schreiben.",
                speaker = "Julian",
                emotion = EmotionalTone.DRAMATIC
            )
        )

        val speaker = SpeakerAcousticProfile.defaultForDocumentary()
        val voiceSettings = VoiceSettings(
            voicePersona = VoicePersona.MARCUS,
            pitch = speaker.targetPitchFactor,
            speakingRate = speaker.targetSpeedFactor,
            emotionalTone = EmotionalTone.DRAMATIC,
            voiceMatchSameAsVideo = true,
            speakerProfile = speaker,
            customVoiceLabel = "Julian Clone (Cinematic Bass)"
        )

        return DubbingProject(
            id = "proj_patagonia_doc",
            title = "Patagonia: Kingdom of Ice & Wind",
            description = "Nature documentary capturing the breathtaking glaciers and wildlife of Southern Patagonia.",
            videoCategory = "Nature Documentary",
            durationMs = 24500L,
            originalLanguage = SupportedLanguage.ENGLISH,
            targetLanguage = SupportedLanguage.GERMAN,
            voiceSettings = voiceSettings,
            scriptCues = cues,
            originalAudioVolume = 0.22f,
            dubbedAudioVolume = 1.0f,
            audioMixMode = AudioMixMode.DUCKED_ORIGINAL,
            isDubbingProcessed = true
        )
    }

    private fun createCulinaryProject(): DubbingProject {
        val cues = listOf(
            ScriptCue(
                id = "cul_1",
                startTimeMs = 0L,
                endTimeMs = 4600L,
                originalText = "The secret to real Bolognese ragù is slow patience and gentle caramelization of the soffritto.",
                translatedText = "Il segreto del vero ragù alla bolognese è la lenta pazienza e la dolce caramellizzazione del soffritto.",
                speaker = "Marco",
                emotion = EmotionalTone.WARM_FRIENDLY
            ),
            ScriptCue(
                id = "cul_2",
                startTimeMs = 4900L,
                endTimeMs = 10000L,
                originalText = "When kneading the pasta dough, feel the elasticity until it is silky smooth like velvet.",
                translatedText = "Quando impasti la pasta, senti l'elasticità finché non diventa liscia e setosa come il velluto.",
                speaker = "Marco",
                emotion = EmotionalTone.ENTHUSIASTIC
            ),
            ScriptCue(
                id = "cul_3",
                startTimeMs = 10400L,
                endTimeMs = 15800L,
                originalText = "Roll it thin by hand, fold loosely, and cut into classic eight-millimeter ribbons of tagliatelle.",
                translatedText = "Stendila sottile a mano, piegala delicatamente e taglia in classiche strisce di tagliatelle da otto millimetri.",
                speaker = "Marco",
                emotion = EmotionalTone.FAST_PACED
            )
        )

        return DubbingProject(
            id = "proj_pasta_culinary",
            title = "Mastering Handmade Tagliatelle",
            description = "Artisanal culinary lesson on traditional pasta making and authentic ragù sauce.",
            videoCategory = "Culinary & Cooking",
            durationMs = 16500L,
            originalLanguage = SupportedLanguage.ENGLISH,
            targetLanguage = SupportedLanguage.ITALIAN,
            voiceSettings = VoiceSettings(
                voicePersona = VoicePersona.KENJI,
                pitch = 1.05f,
                speakingRate = 1.10f,
                emotionalTone = EmotionalTone.WARM_FRIENDLY
            ),
            scriptCues = cues,
            originalAudioVolume = 0.15f,
            dubbedAudioVolume = 1.0f,
            audioMixMode = AudioMixMode.DUCKED_ORIGINAL,
            isDubbingProcessed = true
        )
    }

    /**
     * Translates cues for a project into a target language with natural phrasing and timing adjustments.
     */
    fun getTranslatedCues(
        project: DubbingProject,
        targetLang: SupportedLanguage
    ): List<ScriptCue> {
        val translations = getTranslationDictionary()
        return project.scriptCues.map { cue ->
            val dictForLang = translations[targetLang] ?: emptyMap()
            val translated = dictForLang[cue.id] ?: generateSimulatedTranslation(cue.originalText, targetLang)
            cue.copy(translatedText = translated)
        }
    }

    private fun generateSimulatedTranslation(original: String, targetLang: SupportedLanguage): String {
        return when (targetLang) {
            SupportedLanguage.SPANISH -> "[$original en español adaptado para doblaje con sincronización labial]"
            SupportedLanguage.FRENCH -> "[$original en français adapté pour le doublage avec synchronisation]"
            SupportedLanguage.GERMAN -> "[$original auf Deutsch synchronisiert mit präziser Lippenpassung]"
            SupportedLanguage.JAPANESE -> "[$original 日本語吹替版、リップシンク最適化]"
            SupportedLanguage.HINDI -> "[$original हिंदी डबिंग, सटीक आवाज़ और समय अनुकूलन]"
            SupportedLanguage.MANDARIN -> "[$original 中文配音，音画同步]"
            SupportedLanguage.PORTUGUESE -> "[$original em português com sincronização de voz]"
            SupportedLanguage.ITALIAN -> "[$original in italiano doppiato con sincronizzazione labiale]"
            SupportedLanguage.KOREAN -> "[$original 한국어 더빙, 입모양 싱크 최적화]"
            SupportedLanguage.ARABIC -> "[$original مدبلج بالعربية مع مطابقة الشفاه]"
            SupportedLanguage.RUSSIAN -> "[$original русский дубляж с точной синхронизацией]"
            SupportedLanguage.ENGLISH -> original
            else -> "[$original in ${targetLang.displayName} (${targetLang.nativeName}) dubbed with lip-sync]"
        }
    }

    private fun getTranslationDictionary(): Map<SupportedLanguage, Map<String, String>> {
        return mapOf(
            SupportedLanguage.SPANISH to mapOf(
                "tech_1" to "Bienvenidos a todos. Hoy estamos revelando un paradigma completamente nuevo en la computación.",
                "tech_2" to "Al aprovechar el entrelazamiento cuántico, operaciones que antes tardaban milenios ahora se ejecutan en segundos.",
                "tech_3" to "Observen el gráfico de coherencia de cúbits en pantalla. La matriz de estabilidad ha mejorado cinco veces.",
                "tech_4" to "Este avance transformará el descubrimiento de fármacos médicos, las simulaciones climáticas y la criptografía.",
                "tech_5" to "La era de la supremacía cuántica práctica ya no es un sueño teórico lejano. Está aquí hoy.",
                "travel_1" to "Entrar al bosque de bambú de Arashiyama se siente como cruzar a otro mundo completamente diferente.",
                "travel_2" to "El viento susurrando entre las altas cañas crea una melodía que los japoneses llaman paisaje de tranquilidad.",
                "travel_3" to "Justo al final de este sendero de piedra se encuentra el santuario de un maestro del té centenario.",
                "travel_4" to "Disfrutemos de un tazón de matcha ceremonial fresco mientras escuchamos la fuente del jardín.",
                "doc_1" to "En el extremo sur de los Andes, colosales murallas de hielo azul se encuentran con el mar salvaje.",
                "doc_2" to "Feroz viento antártico barre los fiordos a más de cien kilómetros por hora.",
                "doc_3" to "Sin embargo, en esta naturaleza implacable, guanacos y cóndores andinos reinan con gracia sin esfuerzo.",
                "doc_4" to "Esto es la Patagonia: la frontera intacta donde la tierra y el clima escriben su épica historia.",
                "cul_1" to "El secreto del auténtico ragù a la boloñesa es la paciencia y la suave caramelización del sofrito.",
                "cul_2" to "Al amasar la masa de pasta, sientan la elasticidad hasta que quede tan suave como el terciopelo.",
                "cul_3" to "Extiéndanla fina a mano, doblen con cuidado y corten en tiras clásicas de ocho milímetros de tagliatelle."
            ),
            SupportedLanguage.JAPANESE to mapOf(
                "tech_1" to "皆様、ようこそ。本日はコンピューティングのまったく新しいパラダイムをご紹介します。",
                "tech_2" to "量子もつれを活用することで、かつて数千年かかった計算がわずか数秒で完了します。",
                "tech_3" to "画面上の量子ビットコヒーレンスグラフにご注目ください。安定性マトリクスは5倍向上しました。",
                "tech_4" to "この突破口は、創薬研究、気候シミュレーション、そして暗号技術を劇的に革新します。",
                "tech_5" to "実用的な量子超越性の時代は、もはや遠い夢ではありません。今日、ここに実現しました。",
                "travel_1" to "嵐山の竹林に足を踏み入れると、まるで別世界へ迷い込んだかのような感覚に包まれます。",
                "travel_2" to "そびえ立つ竹を揺らす風の音は、日本の人々が『静寂の音景』と呼ぶ美しい調べを奏でます。",
                "travel_3" to "この曲がりくねった石畳の先には、何世紀もの歴史を持つ茶室が静かに佇んでいます。",
                "travel_4" to "庭園の蹲の水音に耳を傾けながら、点てたての抹茶をいただきましょう。"
            ),
            SupportedLanguage.HINDI to mapOf(
                "tech_1" to "आप सभी का स्वागत है। आज हम कंप्यूटिंग में एक पूरी तरह से नए युग की शुरुआत कर रहे हैं।",
                "tech_2" to "क्वांटम उलझाव का उपयोग करके, जो गणनाएं पहले सदियों लेती थीं, वे अब सेकंडों में पूरी होती हैं।",
                "tech_3" to "स्क्रीन पर क्यूबिट कोहेरेंस ग्राफ देखें। स्थिरता मैट्रिक्स में पांच गुना सुधार हुआ है।",
                "tech_4" to "यह सफलता नई दवाओं की खोज, जलवायु सिमुलेशन और सुरक्षा को पूरी तरह बदल देगी।",
                "tech_5" to "व्यावहारिक क्वांटम सर्वोच्चता का युग अब कोई दूर का सपना नहीं है। यह आज हमारे सामने है।"
            ),
            SupportedLanguage.MANDARIN to mapOf(
                "tech_1" to "欢迎大家。今天我们将揭开计算领域的全新范式。",
                "tech_2" to "通过利用量子纠缠，曾经需要几千年的计算现在只需几秒钟即可完成。",
                "tech_3" to "请看屏幕上的量子比特相干图，稳定性矩阵已提升了整整五倍。",
                "tech_4" to "这一突破将彻底改变药物研发、气候模拟以及现代密码学。",
                "tech_5" to "实用量子霸权的时代不再是遥远的理论梦想。它就在今天实现。"
            ),
            SupportedLanguage.GERMAN to mapOf(
                "tech_1" to "Herzlich willkommen. Heute erschließen wir ein völlig neues Paradigma in der Datenverarbeitung.",
                "tech_2" to "Durch die Nutzung von Quantenverschränkung laufen Berechnungen, die einst Jahrtausende brauchten, in Sekunden.",
                "tech_3" to "Beachten Sie das Qubit-Kohärenzdiagramm auf dem Bildschirm. Die Stabilitätsmatrix hat sich verfünffacht.",
                "tech_4" to "Dieser Durchbruch wird die Wirkstoffforschung, Klimasimulationen und Kryptographie grundlegend verändern.",
                "tech_5" to "Die Ära praktischer Quantenüberlegenheit ist kein ferner Traum mehr. Sie ist heute da."
            ),
            SupportedLanguage.FRENCH to mapOf(
                "tech_1" to "Bienvenue à tous. Aujourd'hui, nous ouvrons un tout nouveau paradigme dans le domaine du calcul.",
                "tech_2" to "En exploitant l'intrication quantique, des calculs qui prenaient des millénaires s'exécutent en quelques secondes.",
                "tech_3" to "Observez le graphique de cohérence des qubits à l'écran. La matrice de stabilité a été multipliée par cinq.",
                "tech_4" to "Cette avancée transformera la découverte de médicaments, les simulations climatiques et la cryptographie.",
                "tech_5" to "L'ère de la suprématie quantique pratique n'est plus un rêve lointain. Elle est là aujourd'hui."
            )
        )
    }
}
