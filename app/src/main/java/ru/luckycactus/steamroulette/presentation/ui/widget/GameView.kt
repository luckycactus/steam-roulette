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
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.ViewAnimationFactory
import com.google.android.material.card.MaterialCardView
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ViewGameBinding
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.CoverGlareTransformation
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.onApiAtLeast
import ru.luckycactus.steamroulette.presentation.utils.sp
import kotlin.properties.Delegates

class GameView : MaterialCardView {
    var defaultTextSize: Float by Delegates.observable(0f) { _, _, _ ->
        setAutoSizeText(tvName.text)
    }
    var memoryCacheEnabled = false

    var imageReady = false
        private set

    private lateinit var ivGame: ImageView
    private lateinit var tvName: TextView
    private lateinit var placeholder: View

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
        val binding = ViewGameBinding.inflate(LayoutInflater.from(context), this)
        ivGame = binding.ivGame
        tvName = binding.tvName
        placeholder = binding.placeholder

        setRippleColorResource(android.R.color.transparent)
        onApiAtLeast(21) {
            ivGame.clipToOutline = true
        }
        context.withStyledAttributes(attrs, R.styleable.GameView) {
            defaultTextSize = getDimension(R.styleable.GameView_textSize, DEFAULT_TEXT_SIZE)
        }
    }

    fun setGame(
        game: GameHeader?,
        disableTransition: Boolean = false,
        listener: Listener? = null,
        transitionGameId: Int? = game?.appId,
        imageType: ImageType = ImageType.HD
    ) {
        if (game == current)
            return

        val differentAppId = current?.appId != game?.appId
        current = game
        if (differentAppId)
            imageReady = false
        setAutoSizeText(game?.name)
        placeholder.visibility = View.VISIBLE
        val target = MyTarget(ivGame, listener)
        if (game != null) {
            if (differentAppId)
                createRequestBuilder(this, game, disableTransition, imageType).into(target)
            if (transitionGameId != null) {
                ViewCompat.setTransitionName(
                    this,
                    context.getString(R.string.cardview_shared_element_transition, transitionGameId)
                )
            } else {
                ViewCompat.setTransitionName(this, null)
            }
        } else {
            Glide.with(ivGame).clear(target)
            ViewCompat.setTransitionName(ivGame, null)
        }
    }

    private fun setAutoSizeText(text: CharSequence?) {
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            tvName,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
        )
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
        tvName.text = text
        tvName.post {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                tvName,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
        }
    }

    fun setUserVisibleHint(visible: Boolean) {
        userVisibleHint = visible
    }

    private fun createRequestBuilder(
        view: GameView,
        game: GameHeader,
        disableTransition: Boolean,
        imageType: ImageType
    ): RequestBuilder<Bitmap> {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (game != current)
                        return
                    placeholder.visibility = View.INVISIBLE
                    imageReady = true
                }

                override fun onAnimationStart(animation: Animation?) {

                }
            })
        }

        val transitionOptions = GenericTransitionOptions<Bitmap>().transition(
            object : ViewAnimationFactory<Bitmap>(anim) {
                override fun build(
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Transition<Bitmap> {
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

        val headerFirstCache = GlideApp.with(view)
            .asBitmap()
            .fitCenter()
            .load(GameUrlUtils.headerImage(game.appId))
            .transform(headerImageTransformation)
            .transition(transitionOptions)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(!memoryCacheEnabled)
            .onlyRetrieveFromCache(true)

        val headerAfterAll = headerFirstCache.clone()
            .onlyRetrieveFromCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(!memoryCacheEnabled)

        val portraitHd = GlideApp.with(view)
            .asBitmap()
            .load(GameUrlUtils.libraryPortraitImageHD(game.appId))
            .fitCenter()
            .transform(headerImageTransformation)
            .transition(transitionOptions)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(!memoryCacheEnabled)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        val portrait = GlideApp.with(view)
            .asBitmap()
            .load(GameUrlUtils.libraryPortraitImage(game.appId))
            .fitCenter()
            .transform(headerImageTransformation)
            .transition(transitionOptions)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(!memoryCacheEnabled)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        when (imageType) {
            ImageType.HD -> {
                portrait.onlyRetrieveFromCache(true)
                val portraitImageThumbnail = portrait.clone()
                portraitHd.thumbnail(portraitImageThumbnail)
                headerFirstCache.error(portraitHd)
                portraitHd.error(portrait)
            }
            ImageType.HdIfCachedOrSd -> {
                portraitHd.onlyRetrieveFromCache(true)
                headerFirstCache.error(portraitHd)
                portraitHd.error(portrait)
            }
            ImageType.SD -> {
                headerFirstCache.error(portrait)
            }
        }

        portrait.error(headerAfterAll)

        return headerFirstCache
    }

    private class MyTarget(
        view: ImageView,
        private val listener: Listener?
    ) : BitmapImageViewTarget(view) {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            listener?.onLoadFinished(null)
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            super.onResourceReady(resource, transition)
            listener?.onLoadFinished(resource)
        }
    }

    fun interface Listener {
        fun onLoadFinished(resource: Bitmap?)
    }

    enum class ImageType {
        SD, HD, HdIfCachedOrSd
    }

    companion object {
        private val headerImageTransformation = MultiTransformation<Bitmap>(
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