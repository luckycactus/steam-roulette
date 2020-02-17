package ru.luckycactus.steamroulette.presentation.utils.glide

import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.bumptech.glide.util.pool.FactoryPools
import okhttp3.OkHttpClient
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import java.io.InputStream

class GameCoverModelLoader : ModelLoader<GameHeader, InputStream> {
    private val client = OkHttpClient()
    private val throwableListPool = FactoryPools.threadSafeList<Throwable>(5)

    override fun buildLoadData(
        model: GameHeader,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val fetchers = listOf(
            OkHttpStreamFetcher(client, GlideUrl(GameUrlUtils.libraryPortraitImageHD(model.appId))),
            OkHttpStreamFetcher(client, GlideUrl(GameUrlUtils.headerImage(model.appId)))
        )
        return ModelLoader.LoadData(
            ObjectKey(model),
            MultiFetcher<InputStream>(fetchers, throwableListPool)
        )
    }

    override fun handles(model: GameHeader): Boolean {
        return true
    }

    class Factory : ModelLoaderFactory<GameHeader, InputStream> {
        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<GameHeader, InputStream> {
            return GameCoverModelLoader()
        }

        override fun teardown() {}
    }
}