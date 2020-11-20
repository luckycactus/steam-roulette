package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import androidx.core.text.HtmlCompat
import ru.luckycactus.steamroulette.databinding.ItemGameDetailsLanguagesBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel

class GameLanguagesViewHolder(
    private val binding: ItemGameDetailsLanguagesBinding
) : GameDetailsViewHolder<GameDetailsUiModel.Languages>(binding.root) {

    override fun bind(item: GameDetailsUiModel.Languages) {
        binding.tvLanguages.text = HtmlCompat.fromHtml(item.languages, 0)
    }
}