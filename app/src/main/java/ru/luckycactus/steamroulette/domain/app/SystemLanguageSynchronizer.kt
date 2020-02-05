package ru.luckycactus.steamroulette.domain.app

import androidx.lifecycle.Observer
import ru.luckycactus.steamroulette.domain.common.Event
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemLanguageSynchronizer @Inject constructor(
    appRepository: AppRepository,
    private val languageProvider: LanguageProvider
) {
    private val liveData = appRepository.observeSystemLocaleChanges()
    private val observer = Observer<Event<Unit>> {
        it.ifNotHandled {
            languageProvider.updateLanguage()
        }
    }

    fun start() {
        liveData.observeForever(observer)
    }

    fun stop() {
        liveData.removeObserver(observer)
    }
}