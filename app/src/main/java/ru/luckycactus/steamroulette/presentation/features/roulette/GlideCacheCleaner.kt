package ru.luckycactus.steamroulette.presentation.features.roulette

import android.content.Context
import android.util.Log
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import javax.inject.Inject

@Reusable
class GlideCacheCleaner @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ImageCacheCleaner {

    override suspend fun clearAllCache() {
        clearMemoryCache()
        clearDiskCache()
    }

    override suspend fun clearMemoryCache() {
        withContext(Dispatchers.Main) {
            GlideApp.get(appContext).clearMemory()
        }
    }

    override suspend fun clearDiskCache() {
        withContext(Dispatchers.IO) {
            GlideApp.get(appContext).clearDiskCache()
        }
    }
}