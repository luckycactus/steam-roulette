package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_game_details_screenshots.*
import kotlinx.android.synthetic.main.item_screenshot.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.ScreenshotEntity
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.utils.getThemeColorOrThrow
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.glide.crossfade.CrossFadeFactory
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColor

class GameScreenshotsViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.Screenshots>(view) {
    private lateinit var screenshots: List<ScreenshotEntity>
    private var viewer: StfalconImageViewer<ScreenshotEntity>? = null
    private val placeholder: Drawable

    init {
        rvMedia.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        val margin = view.resources.getDimensionPixelSize(R.dimen.default_activity_margin)
        rvMedia.addItemDecoration(
            SpaceDecoration(margin, 0, margin, false)
        )
        //todo move color into xml on minSdkVersion >= 21
        placeholder =
            ContextCompat.getDrawable(itemView.context, R.drawable.screenshot_placeholder)!!.apply {
                setDrawableColor(
                    this,
                    itemView.context.getThemeColorOrThrow(R.attr.colorSurface)
                )
            }
    }

    fun onScreenshotClick(position: Int, target: ImageView) {
        viewer = StfalconImageViewer.Builder<ScreenshotEntity>(
            target.context,
            screenshots
        ) { view, screenshot ->
            GlideApp.with(view)
                .load(screenshot.full)
                .thumbnail(
                    GlideApp.with(view)
                        .load(screenshot.thumbnail)
                )
                .into(view)
        }.withStartPosition(position)
            .withTransitionFrom(target)
            .withImagesMargin(R.dimen.default_activity_margin)
            .withImageChangeListener {
                val holder = rvMedia.findViewHolderForAdapterPosition(it)
                        as? ScreenshotsAdapter.ScreenshotViewHolder
                viewer!!.updateTransitionImage(holder?.ivScreenshot)
            }
            .withDismissListener {
                viewer = null
            }
            .withHiddenStatusBar(false)
            .show()
    }

    override fun bind(item: GameDetailsUiModel.Screenshots) {
        this.screenshots = item.screenshots
        rvMedia.adapter = ScreenshotsAdapter()
    }

    private inner class ScreenshotsAdapter :
        RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder =
            ScreenshotViewHolder(parent.inflate(R.layout.item_screenshot))

        override fun getItemCount(): Int = screenshots.size

        override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
            holder.bind(screenshots[position])
        }

        inner class ScreenshotViewHolder(
            override val containerView: View
        ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

            init {
                ivScreenshot.setOnClickListener {
                    onScreenshotClick(adapterPosition, ivScreenshot)
                }
            }

            fun bind(screenshotEntity: ScreenshotEntity) {
                GlideApp.with(itemView)
                    .load(screenshotEntity.thumbnail)
                    .transition(DrawableTransitionOptions.with(CrossFadeFactory()))
                    .placeholder(placeholder)
                    .into(ivScreenshot)
            }
        }

    }
}