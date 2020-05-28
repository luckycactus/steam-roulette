package ru.luckycactus.steamroulette.domain.about

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import javax.inject.Inject

class GetAppLibrariesUseCase @Inject constructor(
    private val aboutRepository: AboutRepository
) : AbstractSuspendUseCase<Unit, List<AppLibrary>>() {

    override suspend fun execute(params: Unit): List<AppLibrary> =
        aboutRepository.getAppLibraries()
}