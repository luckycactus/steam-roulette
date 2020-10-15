package ru.luckycactus.steamroulette.domain.library

import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class GetLibraryScaleUseCase @Inject constructor(
    private val librarySettingsRepository: LibrarySettingsRepository
) : SuspendUseCase<Int, Int>() {
    override suspend fun execute(params: Int): Int = librarySettingsRepository.getScale(params)
}