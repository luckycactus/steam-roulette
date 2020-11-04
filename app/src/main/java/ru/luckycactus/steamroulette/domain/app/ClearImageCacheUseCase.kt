package ru.luckycactus.steamroulette.domain.app

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class ClearImageCacheUseCase @Inject constructor(
    private val imageCacheCleaner: ImageCacheCleaner
) : SuspendUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        imageCacheCleaner.clearAllCache()
    }
}