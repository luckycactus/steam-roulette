package ru.luckycactus.steamroulette.data.local

import android.content.Context
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageProviderImpl @Inject constructor(
    @ForApplication private val context: Context
) : LanguageProvider {

    //system_locale - (store_api_code - web_api_code)
    private val languages = mapOf(
        "ar" to ("ar" to "arabic"),
        "bg" to ("bg" to "bulgarian"),
        "zh-rCN" to ("zh-CN" to "schinese"),
        "zh-rTW" to ("zh-TW" to "tchinese"),
        "cs" to ("cs" to "czech"),
        "da" to ("da" to "danish"),
        "nl" to ("nl" to "dutch"),
        "en" to ("en" to "english"),
        "fi" to ("fi" to "finnish"),
        "fr" to ("fr" to "french"),
        "de" to ("de" to "german"),
        "el" to ("el" to "greek"),
        "hu" to ("hu" to "hungarian"),
        "it" to ("it" to "italian"),
        "ja" to ("ja" to "japanese"),
        "ko" to ("ko" to "koreana"),
        "no" to ("no" to "norwegian"),
        "pl" to ("pl" to "polish"),
        "pt" to ("pt" to "portuguese"),
        "pt-rBR" to ("pt-BR" to "brazilian"),
        "ro" to ("ro" to "romanian"),
        "ru" to ("ru" to "russian"),
        "es" to ("es" to "spanish"),
        "es-rUS" to ("es-419" to "latam"),
        "sv" to ("sv" to "swedish"),
        "th" to ("th" to "thai"),
        "tr" to ("tr" to "turkish"),
        "uk" to ("uk" to "ukrainian"),
        "vi" to ("vn" to "vietnamese")
    )

    private lateinit var localeCode: String
    private var languageInfo: Pair<String, String>? = null

    override fun updateLanguage() {
        localeCode = context.getString(R.string.locale)
        var info = languages[localeCode]
        if (info == null) {
            info = languages["en"]
        }
        languageInfo = info!!
    }

    private fun getLanguageInfo(): Pair<String, String> {
        if (languageInfo == null)
            updateLanguage()
        return languageInfo!!
    }

    override fun getLanguageForWebApi(): String {
        return getLanguageInfo().first
    }

    override fun getLanguageForStoreApi(): String {
        return getLanguageInfo().second
    }
}