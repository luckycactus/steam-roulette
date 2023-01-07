package ru.luckycactus.steamroulette.domain.about

import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import javax.inject.Inject

class GetAppLibrariesUseCase @Inject constructor(
    private val aboutRepository: AboutRepository
) {
    suspend operator fun invoke(): List<AppLibrary> {
        return aboutRepository.getAppLibraries()
    }
}