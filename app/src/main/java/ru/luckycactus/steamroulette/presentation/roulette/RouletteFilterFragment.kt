package ru.luckycactus.steamroulette.presentation.roulette

import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment

class RouletteFilterFragment : BaseBottomSheetDialogFragment() {

    override val layoutResId = R.layout.fragment_roulette_filter

    companion object {
        fun newInstance() = RouletteFilterFragment()
    }
}