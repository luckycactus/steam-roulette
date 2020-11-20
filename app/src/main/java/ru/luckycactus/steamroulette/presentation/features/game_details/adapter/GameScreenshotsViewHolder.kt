package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stfalcon.imageviewer.StfalconImageViewer
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ItemGameDetailsScreenshotsBinding
import ru.luckycactus.steamroulette.databinding.ItemScreenshotBinding
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.glide.crossfade.CrossFadeFactory

class GameScreenshotsViewHolder(
    private val binding: ItemGameDetailsScreenshotsBinding
) : GameDetailsViewHolder<GameDetailsUiModel.Screenshots>(binding.root) {

    private lateinit var screenshots: List<Screenshot>
    private var viewer: StfalconImageViewer<Screenshot>? = null

    init {
        binding.rvMedia.apply {
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            val margin = itemView.resources.getDimensionPixelSize(R.dimen.default_activity_margin)
            addItemDecoration(
                SpaceDecoration(margin, 0, margin, false)
            )
        }
    }

    fun openScreenshot(position: Int, target: ImageView) {
        viewer = StfalconImageViewer.Builder(
            target.context,
            screenshots,
            this::loadFullScreenshot
        ).withStartPosition(position)
            .withTransitionFrom(target)
            .withImagesMargin(R.dimen.default_activity_margin)
            .withImageChangeListener {
                val holder = binding.rvMedia.findViewHolderForAdapterPosition(it)
                        as? ScreenshotsAdapter.ScreenshotViewHolder
                viewer!!.updateTransitionImage(holder?.binding?.ivScreenshot)
            }
            .withDismissListener {
                viewer = null
            }
            .withHiddenStatusBar(false)
            .show()
    }

    override fun bind(item: GameDetailsUiModel.Screenshots) {
        this.screenshots = item.screenshots
        binding.rvMedia.adapter = ScreenshotsAdapter()
    }

    private fun loadFullScreenshot(view: ImageView, screenshot: Screenshot) {
        val thumbnail = GlideApp.with(view)
            .load(screenshot.thumbnail)
            .downsample(DownsampleStrategy.CENTER_INSIDE)

        GlideApp.with(view)
            .load(screenshot.full)
            .thumbnail(thumbnail)
            .skipMemoryCache(true)
            .downsample(DownsampleStrategy.NONE)
            .into(view)
    }

    private inner class ScreenshotsAdapter :
        RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ScreenshotViewHolder(ItemScreenshotBinding.inflate(parent.layoutInflater, parent, false))

        override fun getItemCount(): Int = screenshots.size

        override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
            holder.bind(screenshots[position])
        }

        inner class ScreenshotViewHolder(
            val binding: ItemScreenshotBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                with(binding) {
                    ivScreenshot.setOnClickListener {
                        openScreenshot(bindingAdapterPosition, ivScreenshot)
                    }
                }
            }

            fun bind(screenshot: Screenshot): Unit = with(binding) {
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