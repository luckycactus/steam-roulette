package ru.luckycactus.steamroulette.presentation.features.game_details.adapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.ViewCompat
import com.bumptech.glide.request.RequestListener
import kotlinx.android.synthetic.main.item_game_card_stack.*
import kotlinx.android.synthetic.main.item_game_details_header.*
import kotlinx.android.synthetic.main.item_game_details_header.gameView
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel

class GameHeaderViewHolder(
    view: View
) : GameDetailsViewHolder<GameDetailsItemUiModel.Header>(view) {

    fun bind(
        item: GameDetailsItemUiModel.Header, disableTransition: Boolean,
        listener: RequestListener<Drawable>?
    ) {
        gameView.setGame(item.game, disableTransition, listener)
        ViewCompat.setTransitionName(
            gameView.ivGame,
            gameView.context.getString(R.string.image_shared_element_transition, item.game.appId)
        )
        ViewCompat.setTransitionName(
            gameView,
            gameView.context.getString(R.string.cardview_shared_element_transition, item.game.appId)
        )
    }

    override fun bind(item: GameDetailsItemUiModel.Header) {
        throw UnsupportedOperationException()
    }
}