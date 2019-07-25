package ru.luckycactus.steamroulette.presentation.menu

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_menu.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.main.MainActivity

class MenuFragment : BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_menu

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnExit.setOnClickListener {
            (activity as MainActivity).viewModel.onExit()
        }
    }

}