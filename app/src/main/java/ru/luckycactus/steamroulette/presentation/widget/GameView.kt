package ru.luckycactus.steamroulette.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.getColorFromRes
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.DrawableAlwaysCrossFadeFactory

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var current: OwnedGame? = null

    private val headerImageTransformation = MultiTransformation(
        FitCenter(),
        CoverBlurTransformation(50, 5, 0.5f)
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setCardBackgroundColor(getColorFromRes(R.color.gameCardBackground))
        radius = resources.getDimension(R.dimen.cardview_corner_radius)
        clipChildren = false
    }

    fun setGame(game: OwnedGame?) {
        if (game == current)
            return

        current = game
        tvName.text = game?.name

        if (game != null) {
            val errorRequest = Glide.with(this)
                .load(game.headerImageUrl)
                .transform(headerImageTransformation)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))

            val thumbnailRequest = errorRequest.clone()
                .onlyRetrieveFromCache(true)

            Glide.with(this)
                .load(game.libraryPortraitImageUrlHD)
                .thumbnail(thumbnailRequest)
                .error(errorRequest)
                .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(ivGame)
        } else {
            Glide.with(this)
                .clear(ivGame)
        }
        //todo Грузить hd через wifi, обычную через мобильную сеть
    }
}