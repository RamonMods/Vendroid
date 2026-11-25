package io.github.ramonmods.vendroid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Locale

class AppLanguageTest {
    @Test
    fun `picker offers every language that has a translation directory`() {
        val resources = listOf(File("src/main/res"), File("app/src/main/res"))
            .firstOrNull { it.isDirectory }
            ?: error("Cannot locate app/src/main/res from ${File(".").absolutePath}")
        val translated = resources.listFiles().orEmpty()
            .map { it.name }
            .filter { it.startsWith("values-") && it != "values-night" }
            .mapNotNull { name ->
                Regex("^values-([a-z]{2,3})(?:-r([A-Z]{2}))?$").matchEntire(name)?.let { match ->
                    val region = match.groupValues[2]
                    if (region.isEmpty()) match.groupValues[1] else "${match.groupValues[1]}-$region"
                }
            }
            .plus("en")
            .toSet()
        assertEquals(translated, AppLanguage.entries.mapNotNull { it.tag }.toSet())
    }

    @Test
    fun `automatic follows translated system languages and falls back to English`() {
        assertEquals("ar", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("ar-SA")).language)
        assertEquals("ar", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("ar-EG")).language)
        assertEquals("pt-BR", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("pt-BR")).toLanguageTag())
        assertEquals("pt", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("pt-PT")).toLanguageTag())
        assertEquals("fr", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("fr-CA")).language)
        assertEquals("he", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("he-IL")).language)
        assertEquals("fa", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("fa-IR")).language)
        assertEquals("zh-TW", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("zh-HK")).toLanguageTag())
        assertEquals("zh-CN", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("zh-SG")).toLanguageTag())
        assertEquals("bn-BD", AppLanguage.AUTOMATIC.resolve(Locale.forLanguageTag("bn-IN")).toLanguageTag())
        for (locale in listOf(Locale.US, Locale.UK, Locale("sw"), null)) {
            assertEquals("en", AppLanguage.AUTOMATIC.resolve(locale).language)
        }
    }

    @Test
    fun `explicit language overrides the device and malformed saved values use Automatic`() {
        assertEquals("en", AppLanguage.ENGLISH.resolve(Locale("ar")).language)
        assertEquals("ar", AppLanguage.ARABIC.resolve(Locale.ENGLISH).language)
        for (language in AppLanguage.entries) {
            assertEquals(language, AppLanguage.fromPreference(language.name))
            if (language != AppLanguage.AUTOMATIC) {
                assertEquals(requireNotNull(language.tag), language.resolve(Locale.ENGLISH).toLanguageTag())
            }
        }
        for (value in listOf(null, "", "GERMAN", "ar", "AUTOMATIC ")) {
            assertEquals(AppLanguage.AUTOMATIC, AppLanguage.fromPreference(value))
        }
    }
}
