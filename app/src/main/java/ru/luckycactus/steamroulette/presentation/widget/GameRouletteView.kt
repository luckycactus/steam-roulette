package ru.luckycactus.steamroulette.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

class GameRouletteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        orientation = VERTICAL
    }

    fun setOnNextGameListener(listener: ((View) -> Unit)?) {
        btnNextGame.setOnClickListener(listener)
    }

    fun setOnHideGameListener(listener: ((View) -> Unit)?) {
        btnHideAndNextGame.setOnClickListener(listener)
    }

    fun setGame(game: OwnedGame) {
        tvName.text = game.name
        //todo glide
    }
}