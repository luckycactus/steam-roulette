package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.text.Html
import android.view.View
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.item_game_details_languages.*
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel

class GameLanguagesViewHolder(view: View) :
    GameDetailsViewHolder<GameDetailsUiModel.Languages>(view) {

    override fun bind(item: GameDetailsUiModel.Languages) {
        tvLanguages.text = HtmlCompat.fromHtml(item.languages, 0)
    }
}