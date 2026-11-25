package io.github.ramonmods.vendroid

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.test.core.app.ApplicationProvider
import io.github.ramonmods.vendroid.utils.exception.ServiceTimeoutException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

abstract class AppLocalesTestSuite {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @BeforeEach
    fun resetSettings() {
        assertEquals(requireNotNull(javaClass.getAnnotation(Config::class.java)).sdk.single(), Build.VERSION.SDK_INT)
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().commit()
        RuntimeEnvironment.setQualifiers("en")
        AppLocales.apply(context)
    }

    @AfterEach
    fun restoreAutomatic() {
        AppSettings(context).language = AppLanguage.AUTOMATIC
        RuntimeEnvironment.setQualifiers("en")
        AppLocales.apply(context)
    }

    @Test
    fun `all choices persist across settings instances and corrupt values fall back`() {
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)
        for (language in AppLanguage.entries) {
            AppSettings(context).language = language
            assertEquals(language, AppSettings(context).language)
        }
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("language", "obsolete").commit()
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)
    }

    @Test
    fun `automatic follows every translated system language without keeping an override`() {
        RuntimeEnvironment.setQualifiers("pt-rBR")
        AppLocales.apply(context)
        assertLocale("pt-BR", View.LAYOUT_DIRECTION_LTR, "Instalar Ventoy", requestedTag = null)
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)

        RuntimeEnvironment.setQualifiers("ar-rSA")
        AppLocales.apply(context)
        assertLocale("ar", View.LAYOUT_DIRECTION_RTL, "تثبيت Ventoy", requestedTag = null)
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)

        RuntimeEnvironment.setQualifiers("fr")
        AppLocales.apply(context)
        assertLocale("fr", View.LAYOUT_DIRECTION_LTR, "Installer Ventoy", requestedTag = null)
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)

        RuntimeEnvironment.setQualifiers("sw")
        AppLocales.apply(context)
        assertLocale("en", View.LAYOUT_DIRECTION_LTR, "Install Ventoy", requestedTag = null)
        assertEquals(AppLanguage.AUTOMATIC, AppSettings(context).language)
    }

    @Test
    fun `explicit regional choice remains after device changes and notification strings follow it`() {
        AppSettings(context).language = AppLanguage.PORTUGUESE_BRAZIL
        RuntimeEnvironment.setQualifiers("en")
        AppLocales.apply(context)
        assertLocale("pt-BR", View.LAYOUT_DIRECTION_LTR, "Instalar Ventoy")
        assertEquals("Idioma", AppLocales.getString(context, R.string.language))

        RuntimeEnvironment.setQualifiers("fr")
        AppLocales.apply(context)
        assertLocale("pt-BR", View.LAYOUT_DIRECTION_LTR, "Instalar Ventoy")
        assertEquals("Português (Brasil)", AppLanguage.PORTUGUESE_BRAZIL.nativeName)
    }

    @Test
    fun `explicit choice remains after device changes and notification strings follow a later choice`() {
        AppSettings(context).language = AppLanguage.ARABIC
        RuntimeEnvironment.setQualifiers("en")
        AppLocales.apply(context)
        assertLocale("ar", View.LAYOUT_DIRECTION_RTL, "تثبيت Ventoy")
        assertEquals("جارٍ تنزيل Ventoy 1.1.18…", AppLocales.getString(context, R.string.downloading_online_ventoy, "1.1.18"))
        assertEquals("مُثبّت Ventoy", AppLocales.getString(context, R.string.ventoy_installer_name))
        assertEquals(
            "يبدو أن عملية الكتابة قد تعثرت. حاول مرة اخرى.",
            ServiceTimeoutException().getUiMessage(AppLocales.localizedContext(context)),
        )
        val firstServiceContext = AppLocales.localizedContext(context)
        AppSettings(context).language = AppLanguage.ENGLISH
        RuntimeEnvironment.setQualifiers("ar")
        AppLocales.apply(context)
        assertLocale("en", View.LAYOUT_DIRECTION_LTR, "Install Ventoy")
        // A notification constructed by an already-running service uses the current saved choice.
        assertEquals("Downloading Ventoy 1.1.18…", AppLocales.getString(firstServiceContext, R.string.downloading_online_ventoy, "1.1.18"))
        assertEquals("Ventoy installer", AppLocales.getString(firstServiceContext, R.string.ventoy_installer_name))
        assertEquals(
            "The writing process seems to have gotten stuck. Please try again.",
            ServiceTimeoutException().getUiMessage(AppLocales.localizedContext(firstServiceContext)),
        )
    }

    private fun assertLocale(tag: String, direction: Int, installLabel: String, requestedTag: String? = tag) {
        val localized = AppLocales.localizedContext(context)
        assertEquals(tag, ConfigurationCompat.getLocales(localized.resources.configuration).get(0)?.toLanguageTag())
        assertEquals(direction, localized.resources.configuration.layoutDirection)
        assertEquals(installLabel, localized.getString(R.string.install_ventoy))
        val requested = if (Build.VERSION.SDK_INT >= 33) {
            requireNotNull(context.getSystemService(LocaleManager::class.java)).applicationLocales
        } else {
            AppCompatDelegate.getApplicationLocales()
        }
        if (requestedTag == null) {
            assertEquals(0, requested.size())
        } else {
            assertEquals(requestedTag, requested.get(0)?.toLanguageTag())
        }
    }
}

@ExtendWith(RobolectricExtension::class)
@Config(application = VendroidApplication::class, sdk = [23])
class AppLocalesApi23Test : AppLocalesTestSuite()

@ExtendWith(RobolectricExtension::class)
@Config(application = VendroidApplication::class, sdk = [32])
class AppLocalesApi32Test : AppLocalesTestSuite()

@ExtendWith(RobolectricExtension::class)
@Config(application = VendroidApplication::class, sdk = [35])
class AppLocalesApi35Test : AppLocalesTestSuite()
