package ru.luckycactus.steamroulette.presentation.features.game_details

import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment

class GameDetailsFragment: BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_game_details

    companion object {
        fun newInstance() = GameDetailsFragment()
    }
}