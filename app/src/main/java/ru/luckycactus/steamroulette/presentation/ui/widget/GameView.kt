package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverLoader

class GameView : MaterialCardView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setRippleColorResource(android.R.color.transparent)
    }

    private var current: OwnedGame? = null

    fun setGame(game: OwnedGame?, gameCoverLoader: GlideGameCoverLoader) {
        if (game == current)
            return

        current = game
        tvName.text = game?.name

        if (game != null) {
            gameCoverLoader.createRequestBuilder(this, game)
                .into(ivGame)
        } else {
            gameCoverLoader.clear(ivGame)
        }
    }
}