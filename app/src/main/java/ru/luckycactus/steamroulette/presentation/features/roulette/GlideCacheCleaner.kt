package ru.luckycactus.steamroulette.presentation.features.roulette

import android.content.Context
import com.bumptech.glide.Glide
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.di.ForApplication
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import javax.inject.Inject

@Reusable
class GlideCacheCleaner @Inject constructor(
    @ForApplication private val appContext: Context
) : ImageCacheCleaner {

    override suspend fun clearAllCache() {
        withContext(Dispatchers.Main) {
            Glide.get(appContext).clearMemory()
        }
        withContext(Dispatchers.IO) {
            Glide.get(appContext).clearDiskCache()
        }
    }
}