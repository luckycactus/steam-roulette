package ru.luckycactus.steamroulette.domain.entity

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.Target

interface GameCoverPreloader {

    fun preload(game: OwnedGame): Target<*>

    fun cancelPreload(target: Target<*>)
}