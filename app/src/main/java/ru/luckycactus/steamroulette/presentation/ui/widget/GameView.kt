package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.ViewAnimationFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverGlareTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.onApiAtLeast
import ru.luckycactus.steamroulette.presentation.utils.sp

class GameView : MaterialCardView {
    var textSize: Float
        set(value) {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                tvName,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
            )
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                tvName,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
        }
        get() = tvName.textSize

    var memoryCacheEnabled = false

    private var imageReady = false

    private var current: GameHeader? = null
    private var userVisibleHint = true

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setRippleColorResource(android.R.color.transparent)
        onApiAtLeast(21) {
            ivGame.clipToOutline = true
        }
        context.withStyledAttributes(attrs, R.styleable.GameView) {
            textSize = getDimension(R.styleable.GameView_textSize, DEFAULT_TEXT_SIZE)
        }
    }

    fun setGame(
        game: GameHeader?,
        disableTransition: Boolean = false,
        listener: RequestListener<Drawable>? = null
    ) {
        if (game == current)
            return

        val differentAppId = current?.appId != game?.appId

        current = game
        if (differentAppId)
            imageReady = false
        tvName.text = game?.name
        placeholder.visibility = View.VISIBLE
        if (game != null) {
            if (differentAppId)
                createRequestBuilder(this, game, disableTransition, listener).into(ivGame)
            ViewCompat.setTransitionName(
                ivGame,
                context.getString(R.string.image_shared_element_transition, game.appId)
            )
//            ViewCompat.setTransitionName(
//                this,
//                context.getString(R.string.cardview_shared_element_transition, game.appId)
//            )
        } else {
            Glide.with(ivGame).clear(ivGame)
            ViewCompat.setTransitionName(ivGame, null)
        }
    }

    fun setUserVisibleHint(visible: Boolean) {
        userVisibleHint = visible
    }

    fun getSharedViews(): List<View> {
        return if (imageReady)
            listOf<View>(ivGame)
        else emptyList()
    }

    //todo Грузить hd через wifi, обычную через мобильную сеть
    private fun createRequestBuilder(
        view: GameView,
        game: GameHeader,
        disableTransition: Boolean,
        listener: RequestListener<Drawable>?
    ): RequestBuilder<Drawable> {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    placeholder.visibility = View.INVISIBLE
                    imageReady = true
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
                    val transition = if (disableTransition || !userVisibleHint)
                        NoTransition.get()
                    else
                        super.build(dataSource, isFirstResource)

                    if (transition is NoTransition) {
                        placeholder.visibility = View.INVISIBLE
                        imageReady = true
                    }

                    return transition
                }
            }
        )

        return GlideApp.with(view)
            .load(game)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(!memoryCacheEnabled)
            .transform(headerImageTransformation)
            .transition(transitionOptions)
            .also { request ->
                listener?.let {
                    request.listener(it)
                }
            }
    }

    companion object {
        private val headerImageTransformation = MultiTransformation<Bitmap>(
            //todo calculate blur scale width
            CoverBlurTransformation(25, 100, 0.5f),
            CoverGlareTransformation(
                BitmapFactory.decodeResource(
                    App.getInstance().resources,
                    R.drawable.cover_glare,
                    BitmapFactory.Options().apply { inScaled = false }
                ).apply {
                    density = Bitmap.DENSITY_NONE
                }
            )
        )

        private val DEFAULT_TEXT_SIZE = sp(20f)
    }
}