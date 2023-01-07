package ru.luckycactus.steamroulette.domain.app

import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import javax.inject.Inject

class ClearImageCacheUseCase @Inject constructor(
    private val imageCacheCleaner: ImageCacheCleaner
) {
    suspend operator fun invoke() {
        imageCacheCleaner.clearAllCache()
    }
}