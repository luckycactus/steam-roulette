package ru.luckycactus.steamroulette.domain.library

import javax.inject.Inject

class GetLibraryScaleUseCase @Inject constructor(
    private val librarySettingsRepository: LibrarySettingsRepository
) {
    operator fun invoke(params: Int): Int = librarySettingsRepository.getScale(params)
}