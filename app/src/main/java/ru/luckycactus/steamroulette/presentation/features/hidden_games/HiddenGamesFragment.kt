package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_hidden_games.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class HiddenGamesFragment : BaseFragment() {
    private val viewModel by viewModel {
        InjectionManager.findComponent<MainActivityComponent>().hiddenGamesViewModel
    }

    private val adapter = HiddenGamesAdapter()

    override val layoutResId = R.layout.fragment_hidden_games

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        rvHiddenGames.adapter = adapter
        rvHiddenGames.layoutManager = GridLayoutManager(context, 4)

        observe(viewModel.hiddenGames) {
            adapter.submitList(it)
        }
    }

    companion object {
        fun newInstance() = HiddenGamesFragment()
    }
}