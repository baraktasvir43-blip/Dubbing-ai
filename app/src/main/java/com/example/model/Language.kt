package com.example.model

import java.util.Locale

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val locale: Locale
) {
    ENGLISH("en", "English", "English", "🇺🇸", Locale.ENGLISH),
    SPANISH("es", "Spanish", "Español", "🇪🇸", Locale("es", "ES")),
    FRENCH("fr", "French", "Français", "🇫🇷", Locale.FRENCH),
    GERMAN("de", "German", "Deutsch", "🇩🇪", Locale.GERMAN),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", Locale.JAPANESE),
    HINDI("hi", "Hindi", "हिन्दी", "🇮🇳", Locale("hi", "IN")),
    MANDARIN("zh", "Mandarin", "中文", "🇨🇳", Locale.CHINESE),
    PORTUGUESE("pt", "Portuguese", "Português", "🇧🇷", Locale("pt", "BR")),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", Locale.ITALIAN),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", Locale.KOREAN),
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", Locale("ar", "SA")),
    RUSSIAN("ru", "Russian", "Русский", "🇷🇺", Locale("ru", "RU")),
    DUTCH("nl", "Dutch", "Nederlands", "🇳🇱", Locale("nl", "NL")),
    POLISH("pl", "Polish", "Polski", "🇵🇱", Locale("pl", "PL")),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷", Locale("tr", "TR")),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", Locale("vi", "VN")),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia", "🇮🇩", Locale("id", "ID")),
    THAI("th", "Thai", "ไทย", "🇹🇭", Locale("th", "TH")),
    SWEDISH("sv", "Swedish", "Svenska", "🇸🇪", Locale("sv", "SE")),
    GREEK("el", "Greek", "Ελληνικά", "🇬🇷", Locale("el", "GR")),
    CZECH("cs", "Czech", "Čeština", "🇨🇿", Locale("cs", "CZ")),
    DANISH("da", "Danish", "Dansk", "🇩🇰", Locale("da", "DK")),
    FINNISH("fi", "Finnish", "Suomi", "🇫🇮", Locale("fi", "FI")),
    NORWEGIAN("no", "Norwegian", "Norsk", "🇳🇴", Locale("no", "NO")),
    UKRAINIAN("uk", "Ukrainian", "Українська", "🇺🇦", Locale("uk", "UA")),
    TAGALOG("fil", "Filipino", "Tagalog", "🇵🇭", Locale("fil", "PH")),
    HEBREW("he", "Hebrew", "עברית", "🇮🇱", Locale("he", "IL"));

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
