package ru.luckycactus.steamroulette.presentation.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.getColorFromRes
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val headerImageTransformation = MultiTransformation(
        FitCenter(),
        CoverBlurTransformation(50, 5, 0.5f)
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setCardBackgroundColor(getColorFromRes(R.color.gameCardBackground))
        radius = resources.getDimension(R.dimen.cardview_corner_radius)
    }

    fun setGame(game: OwnedGame) {
        tvName.text = game.name

        val headerImageRequest = Glide.with(this)
            .load(game.headerImageUrl)
            .transform(headerImageTransformation)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

        //todo Грузить hd через wifi, обычную через мобильную сеть
        Glide.with(this)
            .load(game.libraryPortraitImageUrlHD)
            .thumbnail(headerImageRequest.onlyRetrieveFromCache(true))
            .error(headerImageRequest)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(ivGame)
    }
}