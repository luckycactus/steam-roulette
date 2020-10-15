package ru.luckycactus.steamroulette.domain.library

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class SaveLibraryScaleUseCase @Inject constructor(
    private val librarySettingsRepository: LibrarySettingsRepository
): SuspendUseCase<Int, Unit>() {
    override suspend fun execute(params: Int) {
        librarySettingsRepository.saveScale(params)
    }
}