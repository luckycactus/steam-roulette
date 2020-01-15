package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.transition.ViewAnimationFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.GameCoverModel
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.visibility

class GameView : MaterialCardView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setRippleColorResource(android.R.color.transparent)
    }

    private var current: OwnedGame? = null

    fun setGame(game: OwnedGame?) {
        if (game == current)
            return

        current = game
        tvName.text = game?.name

        placeholder.visibility(true)
        if (game != null) {
            createRequestBuilder(this, game)
                .into(ivGame)
        } else {
            Glide.with(ivGame).clear(ivGame)
        }
    }

    private val headerImageTransformation = MultiTransformation(
        FitCenter(),
        CoverBlurTransformation(50, 5, 0.5f)
    )

    //todo Грузить hd через wifi, обычную через мобильную сеть
    fun createRequestBuilder(view: GameView, game: OwnedGame): RequestBuilder<Drawable> {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    placeholder.visibility(false)
                }

                override fun onAnimationStart(animation: Animation?) {

                }
            })
        }

        val transitionOptions = GenericTransitionOptions<Drawable>().transition(
            ViewAnimationFactory<Drawable>(anim)
        )

        return GlideApp.with(view)
            .load(GameCoverModel(game))
            .diskCacheStrategy(DiskCacheStrategy.ALL) //todo
            .transform(headerImageTransformation)
            .transition(transitionOptions)
    }
}