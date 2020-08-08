package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_game_details_screenshots.*
import kotlinx.android.synthetic.main.item_screenshot.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.glide.crossfade.CrossFadeFactory
import ru.luckycactus.steamroulette.presentation.utils.inflate

class GameScreenshotsViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.Screenshots>(view) {
    private lateinit var screenshots: List<Screenshot>
    private var viewer: StfalconImageViewer<Screenshot>? = null

    init {
        rvMedia.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        val margin = view.resources.getDimensionPixelSize(R.dimen.default_activity_margin)
        rvMedia.addItemDecoration(
            SpaceDecoration(margin, 0, margin, false)
        )
    }

    fun onScreenshotClick(position: Int, target: ImageView) {
        viewer = StfalconImageViewer.Builder<Screenshot>(
            target.context,
            screenshots
        ) { view, screenshot ->
            val thumbnail = GlideApp.with(view)
                .load(screenshot.thumbnail)
                .downsample(DownsampleStrategy.CENTER_INSIDE)

            GlideApp.with(view)
                .load(screenshot.full)
                .thumbnail(thumbnail)
                .skipMemoryCache(true)
                .downsample(DownsampleStrategy.NONE)
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
                    onScreenshotClick(absoluteAdapterPosition, ivScreenshot)
                }
            }

            fun bind(screenshot: Screenshot) {
                GlideApp.with(itemView)
                    .load(screenshot.thumbnail)
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.with(CrossFadeFactory()))
                    .placeholder(R.drawable.screenshot_placeholder)
                    .into(ivScreenshot)
            }
        }

    }
}