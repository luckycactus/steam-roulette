package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ItemGameDescriptionBlockTagBinding
import ru.luckycactus.steamroulette.databinding.ItemGameDetailsShortDescriptionBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColor

class GameShortDescriptionViewHolder(
    private val binding: ItemGameDetailsShortDescriptionBinding,
    viewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.ShortDescription>(binding.root) {

    init {
        with(binding) {
            val space = itemView.resources.getDimensionPixelSize(R.dimen.default_activity_margin)
            val decoration = SpaceDecoration(space, 0, space / 2, false)

            setupTagsRv(rvGenres, decoration)
            setupTagsRv(rvCategories, decoration)

            layoutMetacriticScore.setOnClickListener {
                viewModel.onMetacriticClick()
            }

            header.setOnClickListener {
                viewModel.onDetailedDescriptionClick()
            }
        }
    }

    private fun setupTagsRv(view: RecyclerView, decoration: RecyclerView.ItemDecoration) {
        view.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        view.addItemDecoration(decoration)
    }

    override fun bind(item: GameDetailsUiModel.ShortDescription): Unit = with(binding) {
        tvDescription.text = item.value?.let { HtmlCompat.fromHtml(it, 0) }
        ivForward.visibility(item.detailedDescriptionAvailable)
        header.isClickable = item.detailedDescriptionAvailable

        bindTagsRv(rvGenres, item.genres)
        bindTagsRv(rvCategories, item.categories)

        val ageResource = item.requiredAge?.let { getAgeDrawableResource(it) }
        if (ageResource != null) {
            ivAge.setImageResource(ageResource)
            ivAge.visibility(true)
        } else {
            ivAge.visibility(false)
        }

        if (item.metacriticInfo != null) {
            tvMetacriticScore.text = item.metacriticInfo.score.toString()
            setDrawableColor(
                tvMetacriticScore.background,
                item.metacriticInfo.color
            )
            layoutMetacriticScore.visibility(true)
        } else {
            layoutMetacriticScore.visibility(false)
        }

        blockExtraInfo.visibility(ageResource != null || item.metacriticInfo != null)
    }

    private fun bindTagsRv(rv: RecyclerView, tags: List<String>?) {
        if (!tags.isNullOrEmpty()) {
            rv.adapter = TagsAdapter(tags)
            rv.visibility = View.VISIBLE
        } else {
            rv.visibility = View.GONE
        }
    }

    private fun getAgeDrawableResource(age: Int): Int? {
        return when (age) {
            0 -> R.drawable.age_0
            6 -> R.drawable.age_6
            12 -> R.drawable.age_12
            16 -> R.drawable.age_16
            18 -> R.drawable.age_18
            else -> null
        }
    }

    class TagsAdapter(
        private val items: List<String>
    ) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TagViewHolder(
                ItemGameDescriptionBlockTagBinding.inflate(parent.layoutInflater, parent, false)
            )

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class TagViewHolder(
            private val binding: ItemGameDescriptionBlockTagBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(tag: String) {
                binding.tagView.text = tag
            }
        }
    }
}