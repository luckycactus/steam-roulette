package ru.luckycactus.steamroulette.domain.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemLanguageSynchronizer @Inject constructor(
    private val appRepository: AppRepository,
    private val languageProvider: LanguageProvider,
    @AppCoScope private val appScope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true)
            return
        job = appScope.launch {
            appRepository.observeSystemLocaleChanges().collect {
                languageProvider.updateLanguage()
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}