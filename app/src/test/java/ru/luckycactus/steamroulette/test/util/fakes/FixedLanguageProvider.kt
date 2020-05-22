package ru.luckycactus.steamroulette.test.util.fakes

import ru.luckycactus.steamroulette.domain.common.LanguageProvider

class FixedLanguageProvider(
    private val storeApiLang: String = "en",
    private val webApiLang: String = "english"
) :
    LanguageProvider {
    override fun getLanguageForStoreApi(): String = storeApiLang

    override fun getLanguageForWebApi(): String = webApiLang

    override fun updateLanguage() {
        //nothing
    }
}