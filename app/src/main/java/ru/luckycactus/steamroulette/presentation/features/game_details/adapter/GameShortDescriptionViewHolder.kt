package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_game_description_block_tag.*
import kotlinx.android.synthetic.main.item_game_details_short_description.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColor
import ru.luckycactus.steamroulette.presentation.utils.visibility

class GameShortDescriptionViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.ShortDescription>(view) {
    init {
        val space = view.resources.getDimensionPixelSize(R.dimen.default_activity_margin)
        val decoration = SpaceDecoration(space, 0, space / 2, false)

        rvGenres.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        rvGenres.addItemDecoration(decoration)

        rvCategories.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.addItemDecoration(decoration)
    }

    override fun bind(item: GameDetailsUiModel.ShortDescription) {
        tvDescription.text = item.value
        if (!item.genres.isNullOrEmpty()) {
            rvGenres.adapter = TagsAdapter(item.genres)
            rvGenres.visibility = View.VISIBLE
        } else {
            rvGenres.visibility = View.GONE
        }

        if (!item.categories.isNullOrEmpty()) {
            rvCategories.adapter = TagsAdapter(item.categories)
            rvCategories.visibility = View.VISIBLE
        } else {
            rvCategories.visibility = View.GONE
        }

        val ageResource = item.requiredAge?.let { getAgeDrawableResource(it) }
        if (ageResource != null) {
            ivAge.setImageResource(ageResource)
            ivAge.visibility(true)
        } else {
            ivAge.visibility(false)
        }

        if (item.metacriticInfoEntity != null) {
            tvMetacriticScore.text = item.metacriticInfoEntity.score.toString()
            setDrawableColor(
                tvMetacriticScore.background,
                getMetacriticScoreColor(item.metacriticInfoEntity.score)
            )
            layoutMetacriticScore.visibility(true)
            //todo click
        } else {
            layoutMetacriticScore.visibility(false)
        }

        blockExtraInfo.visibility(ageResource != null || item.metacriticInfoEntity != null)
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

    private fun getMetacriticScoreColor(score: Int): Int { //todo move to domain?
        return ContextCompat.getColor(
            itemView.context,
            when (score) {
                in 75..100 -> R.color.metacritic_good
                in 50..74 -> R.color.metacritic_average
                else -> R.color.metacritic_poor
            }
        )
    }

    class TagsAdapter(
        private val items: List<String>
    ) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            return TagViewHolder(parent.inflate(R.layout.item_game_description_block_tag))
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class TagViewHolder(
            override val containerView: View
        ) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

            fun bind(tag: String) {
                tagView.text = tag
            }
        }
    }
}