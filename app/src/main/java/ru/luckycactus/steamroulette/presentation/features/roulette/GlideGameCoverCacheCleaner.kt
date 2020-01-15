package ru.luckycactus.steamroulette.presentation.features.roulette

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.ViewAnimationFactory
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.common.GameCoverCacheCleaner
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import javax.inject.Inject

@Reusable
class GlideGameCoverCacheCleaner @Inject constructor(
    @ForApplication private val appContext: Context
) : GameCoverCacheCleaner {

    override suspend fun clearAllCache() {
        withContext(Dispatchers.Main) {
            Glide.get(appContext).clearMemory()
        }
        withContext(Dispatchers.IO) {
            Glide.get(appContext).clearDiskCache()
        }
    }
}