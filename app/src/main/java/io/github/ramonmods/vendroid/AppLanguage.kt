package io.github.ramonmods.vendroid

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Every language that ships translations in `app/src/main/res/values-*`.
 *
 * The AUTOMATIC, ENGLISH, and ARABIC constants keep their historical names so
 * preferences saved by older versions continue to load unchanged.
 */
enum class AppLanguage(val tag: String?, val nativeName: String) {
    AUTOMATIC(null, ""),
    ENGLISH("en", "English"),
    AFRIKAANS("af", "Afrikaans"),
    ARABIC("ar", "العربية"),
    AZERBAIJANI("az", "Azərbaycanca"),
    BENGALI("bn-BD", "বাংলা"),
    BURMESE("my", "မြန်မာ"),
    CATALAN("ca", "Català"),
    CHINESE_SIMPLIFIED("zh-CN", "简体中文"),
    CHINESE_TRADITIONAL("zh-TW", "繁體中文"),
    CZECH("cs", "Čeština"),
    DUTCH("nl", "Nederlands"),
    FINNISH("fi", "Suomi"),
    FRENCH("fr", "Français"),
    GALICIAN("gl", "Galego"),
    GERMAN("de", "Deutsch"),
    HEBREW("he", "עברית"),
    HINDI("hi", "हिन्दी"),
    HUNGARIAN("hu", "Magyar"),
    INDONESIAN("id", "Bahasa Indonesia"),
    ITALIAN("it", "Italiano"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어"),
    KURDISH("ckb", "کوردی"),
    MALAYALAM("ml", "മലയാളം"),
    NORWEGIAN_BOKMAL("nb-NO", "Norsk bokmål"),
    PERSIAN("fa", "فارسی"),
    POLISH("pl", "Polski"),
    PORTUGUESE("pt", "Português"),
    PORTUGUESE_BRAZIL("pt-BR", "Português (Brasil)"),
    PUNJABI("pa", "ਪੰਜਾਬੀ"),
    ROMANIAN("ro", "Română"),
    RUSSIAN("ru", "Русский"),
    SARDINIAN("sc", "Sardu"),
    SLOVAK("sk", "Slovenčina"),
    SPANISH("es", "Español"),
    TAMIL("ta", "தமிழ்"),
    THAI("th", "ไทย"),
    TURKISH("tr", "Türkçe"),
    UKRAINIAN("uk", "Українська"),
    VIETNAMESE("vi", "Tiếng Việt");

    fun resolve(systemLocale: Locale?): Locale = when (this) {
        AUTOMATIC -> bestSupportedLocale(systemLocale) ?: Locale.ENGLISH
        else -> Locale.forLanguageTag(requireNotNull(tag))
    }

    /**
     * Picks the translation that best matches the device language, preferring an exact
     * language and region match, then a language-wide translation such as `pt`.
     */
    private fun bestSupportedLocale(systemLocale: Locale?): Locale? {
        val device = systemLocale ?: return null
        if (device.language.isEmpty()) return null
        val language = when (val raw = device.language) {
            // Android and Java still report these obsolete ISO codes on older versions.
            "iw" -> "he"
            "in" -> "id"
            else -> raw
        }
        val candidates = AppLanguage.entries.mapNotNull { entry ->
            entry.tag?.let { Locale.forLanguageTag(it) }
        }.filter { it.language == language }
        if (candidates.isEmpty()) return null

        val country = device.country.orEmpty()
        candidates.firstOrNull {
            it.country.isNotEmpty() && it.country.equals(country, ignoreCase = true)
        }?.let { return it }
        candidates.firstOrNull { it.country.isEmpty() }?.let { return it }
        if (language == "zh") {
            val traditional = country in setOf("TW", "HK", "MO") || device.script == "Hant"
            return Locale.forLanguageTag(if (traditional) "zh-TW" else "zh-CN")
        }
        return candidates.first()
    }

    companion object {
        fun fromPreference(value: String?): AppLanguage =
            entries.firstOrNull { it.name == value } ?: AUTOMATIC
    }
}

/** Automatic keeps no application-locale override so Android follows the device language. */
object AppLocales {
    fun apply(context: Context) {
        val language = AppSettings(context).language
        val tag = language.tag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager = requireNotNull(context.getSystemService(LocaleManager::class.java))
            val requested = if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
            if (manager.applicationLocales != requested) manager.applicationLocales = requested
        } else {
            val requested = if (tag == null) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(tag)
            }
            if (AppCompatDelegate.getApplicationLocales() != requested) {
                AppCompatDelegate.setApplicationLocales(requested)
            }
        }
    }

    // AppCompat only localizes Activity contexts on Android 12 and earlier.
    fun localizedContext(context: Context): Context {
        val locale = selectedLocale(context)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    fun getString(context: Context, @StringRes resource: Int, vararg arguments: Any?): String =
        localizedContext(context).getString(resource, *arguments)

    private fun selectedLocale(context: Context): Locale {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireNotNull(context.getSystemService(LocaleManager::class.java)).systemLocales.get(0)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
        return AppSettings(context).language.resolve(systemLocale)
    }
}
