package ru.luckycactus.steamroulette.presentation.roulette

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.entity.GameCoverCacheCleaner
import ru.luckycactus.steamroulette.domain.entity.GameCoverPreloader
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.DrawableAlwaysCrossFadeFactory
import javax.inject.Inject

class GlideGameCoverLoader @Inject constructor(
    @ForApplication private val appContext: Context
) : GameCoverPreloader, GameCoverCacheCleaner {
    //todo Грузить hd через wifi, обычную через мобильную сеть

    private val headerImageTransformation = MultiTransformation(
        FitCenter(),
        CoverBlurTransformation(50, 5, 0.5f)
    )

    override fun preload(game: OwnedGame): Target<*> {
        val errorRequest = Glide.with(appContext)
            .load(game.headerImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

        return Glide.with(App.getInstance())
            .load(game.libraryPortraitImageUrlHD)
            .error(errorRequest)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .preload()
    }

    override fun cancelPreload(target: Target<*>) {
        Glide.with(appContext).clear(target)
    }

    fun createRequestBuilder(view: View, game: OwnedGame): RequestBuilder<Drawable> {
        val errorRequest = Glide.with(view)
            .load(game.headerImageUrl)
            .transform(headerImageTransformation)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .transition(DrawableTransitionOptions.withCrossFade())

        val thumbnailRequest = errorRequest.clone()
            .onlyRetrieveFromCache(true)

        return Glide.with(view)
            .load(game.libraryPortraitImageUrlHD)
            .thumbnail(thumbnailRequest)
            .error(errorRequest)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    }

    override suspend fun clearAllCache() {
        withContext(Dispatchers.Main) {
            Glide.get(appContext).clearMemory()
        }
        withContext(Dispatchers.IO) {
            Glide.get(appContext).clearDiskCache()
        }
    }

    fun clear(view: View) {
        Glide.with(view).clear(view)
    }
}