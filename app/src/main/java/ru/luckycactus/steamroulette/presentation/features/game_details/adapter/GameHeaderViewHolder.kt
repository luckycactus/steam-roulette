package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import ru.luckycactus.steamroulette.databinding.ItemGameDetailsHeaderBinding
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.GameView
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility

class GameHeaderViewHolder(
    private val binding: ItemGameDetailsHeaderBinding,
    private val transitionGameId: Int
) : GameDetailsViewHolder<GameDetailsUiModel.Header>(binding.root) {

    init {
        binding.gameView.memoryCacheEnabled = true
    }

    fun bind(
        item: GameDetailsUiModel.Header,
        disableTransition: Boolean,
        listener: GameView.Listener?
    ): Unit = with(binding) {
        tvHeaderGameName.text = item.gameHeader.name
        tvPublisher.text = item.publisher
        tvPublisher.visibility(!item.publisher.isNullOrBlank())
        tvDeveloper.text = item.developer
        tvDeveloper.visibility(!item.developer.isNullOrBlank())
        tvReleaseDate.text = item.releaseDate
        tvReleaseDate.visibility(!item.releaseDate.isNullOrBlank())
        gameView.setGame(
            item.gameHeader,
            disableTransition,
            listener,
            imageType = GameView.ImageType.HdOrSd,
            transitionGameId = transitionGameId
        )
    }

    override fun bind(item: GameDetailsUiModel.Header) {
        throw UnsupportedOperationException()
    }
}