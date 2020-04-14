package ru.luckycactus.steamroulette.domain.about

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import javax.inject.Inject

class GetAppLibrariesUseCase @Inject constructor(
    private val aboutRepository: AboutRepository
) : SuspendUseCase<Unit, List<AppLibrary>>() {

    override suspend fun getResult(params: Unit): List<AppLibrary> =
        aboutRepository.getAppLibraries()
}