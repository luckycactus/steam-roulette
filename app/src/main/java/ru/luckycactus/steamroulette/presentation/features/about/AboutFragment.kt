package ru.luckycactus.steamroulette.presentation.features.about

import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment

class AboutFragment: BaseFragment() {
    override val layoutResId = R.layout.fragment_about

    companion object {
        fun newInstance() = AboutFragment()
    }
}