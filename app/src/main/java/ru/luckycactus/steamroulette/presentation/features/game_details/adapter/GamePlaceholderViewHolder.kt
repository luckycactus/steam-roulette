package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import ru.luckycactus.steamroulette.databinding.ItemPlaceholderBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder

class GamePlaceholderViewHolder(
    binding: ItemPlaceholderBinding,
    viewModel: GameDetailsViewModel
) : GameDetailsViewHolder<GameDetailsUiModel.Placeholder>(binding.root) {

    private val dataLoadingViewHolder = DataLoadingViewHolder(
        binding.empty.root,
        binding.progress.root,
        binding.dummyContent,
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