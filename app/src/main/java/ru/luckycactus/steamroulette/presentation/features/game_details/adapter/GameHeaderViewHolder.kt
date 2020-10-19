package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.view.View
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.item_game_details_header.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.GameView
import ru.luckycactus.steamroulette.presentation.utils.visibility

class GameHeaderViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsUiModel.Header>(view) {

    init {
        gameView.memoryCacheEnabled = true
    }

    fun bind(
        item: GameDetailsUiModel.Header,
        disableTransition: Boolean,
        listener: GameView.Listener?
    ) {
        tvHeaderGameName.text = item.gameHeader.name
        tvPublisher.text = item.publisher
        tvPublisher.visibility(!item.publisher.isNullOrBlank())
        tvDeveloper.text = item.developer
        tvDeveloper.visibility(!item.developer.isNullOrBlank())
        tvReleaseDate.text = item.releaseDate
        tvReleaseDate.visibility(!item.releaseDate.isNullOrBlank())
//        ViewCompat.setTransitionName(
//            gameView.ivGame,
//            gameView.context.getString(
//                R.string.image_shared_element_transition,
//                item.gameHeader.appId
//            )
//        )
        ViewCompat.setTransitionName(
            gameView,
            gameView.context.getString(
                R.string.cardview_shared_element_transition,
                item.gameHeader.appId
            )
        )
        gameView.setGame(
            item.gameHeader,
            disableTransition,
            listener,
            imageType = GameView.ImageType.HdOrSd
        )
    }

    override fun bind(item: GameDetailsUiModel.Header) {
        throw UnsupportedOperationException()
    }
}