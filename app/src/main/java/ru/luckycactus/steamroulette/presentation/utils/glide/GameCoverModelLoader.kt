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
import java.io.InputStream

class GameCoverModelLoader : ModelLoader<GameCoverModel, InputStream> {
    private val client = OkHttpClient() //todo
    private val throwableListPool = FactoryPools.threadSafeList<Throwable>(5)

    override fun buildLoadData(
        model: GameCoverModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val fetchers = listOf(
            OkHttpStreamFetcher(client, GlideUrl(model.primary)),
            OkHttpStreamFetcher(client, GlideUrl(model.secondary))
        )
        return ModelLoader.LoadData(
            ObjectKey(model),
            MultiFetcher<InputStream>(fetchers, throwableListPool)
        )
    }

    override fun handles(model: GameCoverModel): Boolean {
        return true
    }

    class Factory : ModelLoaderFactory<GameCoverModel, InputStream> {
        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<GameCoverModel, InputStream> {
            return GameCoverModelLoader()
        }

        override fun teardown() {}
    }
}