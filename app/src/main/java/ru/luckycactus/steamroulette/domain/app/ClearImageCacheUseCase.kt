package ru.luckycactus.steamroulette.domain.app

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearImageCacheUseCase @Inject constructor(
    private val imageCacheCleaner: ImageCacheCleaner
) : AbstractSuspendUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        imageCacheCleaner.clearAllCache()
    }
}