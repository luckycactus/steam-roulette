package ru.luckycactus.steamroulette.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_game_roulette.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.getColorFromRes

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_roulette, this, true)
        setCardBackgroundColor(getColorFromRes(R.color.gameCardBackground))
        radius = resources.getDimension(R.dimen.cardview_corner_radius)
    }

    fun setGame(game: OwnedGame) {
        tvName.text = game.name
        Glide.with(this).load(game.headerImageUrl).into(ivGame)
    }
}