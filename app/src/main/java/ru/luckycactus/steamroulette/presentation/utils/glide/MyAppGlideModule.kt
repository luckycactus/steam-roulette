package ru.luckycactus.steamroulette.presentation.utils.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            GameHeader::class.java,
            InputStream::class.java,
            GameCoverModelLoader.Factory()
        )
    }
}