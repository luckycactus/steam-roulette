package ru.luckycactus.steamroulette.domain.library

import javax.inject.Inject

class SaveLibraryScaleUseCase @Inject constructor(
    private val librarySettingsRepository: LibrarySettingsRepository
) {
    operator fun invoke(params: Int) {
        librarySettingsRepository.saveScale(params)
    }
}