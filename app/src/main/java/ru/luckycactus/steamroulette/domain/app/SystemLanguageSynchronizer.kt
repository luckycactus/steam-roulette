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
    appRepository: AppRepository,
    private val languageProvider: LanguageProvider,
    @AppCoScope private val appScope: CoroutineScope
) {
    private val localeChangesFlow = appRepository.observeSystemLocaleChanges()
    private var job: Job? = null

    fun start() {
        job = appScope.launch {
            localeChangesFlow.collect {
                languageProvider.updateLanguage()
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}