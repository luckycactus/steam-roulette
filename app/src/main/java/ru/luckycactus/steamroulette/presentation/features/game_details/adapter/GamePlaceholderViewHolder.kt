package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.item_placeholder.*
import kotlinx.android.synthetic.main.progress.*
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder

class GamePlaceholderViewHolder(
    view: View,
    viewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Placeholder>(view) {

    private val dataLoadingViewHolder = DataLoadingViewHolder(
        emptyLayout,
        progress,
        dummyContent,
        viewModel::onRetryClick
    )

    override fun bind(item: GameDetailsUiModel.Placeholder) {
        when (item.contentState) {
            ContentState.Success -> throw IllegalStateException()
            else -> dataLoadingViewHolder.showContentState(item.contentState)
        }
        itemView.requestLayout()
    }
}