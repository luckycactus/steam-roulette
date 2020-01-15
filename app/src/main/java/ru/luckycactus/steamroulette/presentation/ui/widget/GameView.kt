package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.ViewAnimationFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverGlareTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.GameCoverModel
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
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

        placeholder.visibility = View.VISIBLE
        if (game != null) {
            createRequestBuilder(this, game).into(ivGame)
        } else {
            Glide.with(ivGame).clear(ivGame)
        }
    }

    //todo Грузить hd через wifi, обычную через мобильную сеть
    fun createRequestBuilder(view: GameView, game: OwnedGame): RequestBuilder<Drawable> {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    placeholder.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {

                }
            })
        }

        val transitionOptions = GenericTransitionOptions<Drawable>().transition(
            object : ViewAnimationFactory<Drawable>(anim) {
                override fun build(
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Transition<Drawable> {
                    return super.build(dataSource, isFirstResource).also {
                        if (it is NoTransition) {
                            placeholder.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        )

        return GlideApp.with(view)
            .load(GameCoverModel(game))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .transform(headerImageTransformation)
            .transition(transitionOptions)
    }

    companion object {
        private val headerImageTransformation = MultiTransformation<Bitmap>(
            CoverBlurTransformation(50, 5, 0.5f),
            CoverGlareTransformation(
                BitmapFactory.decodeResource(
                    App.getInstance().resources,
                    R.drawable.cover_glare
                )
            )
        )
    }
}