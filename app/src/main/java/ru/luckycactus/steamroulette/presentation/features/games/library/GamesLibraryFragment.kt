package ru.luckycactus.steamroulette.presentation.features.games.library

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.presentation.features.games.base.BaseGamesLibraryFragment

@AndroidEntryPoint
class GamesLibraryFragment: BaseGamesLibraryFragment() {

    override val viewModel: GamesLibraryViewModel by viewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    companion object {
        fun newInstance() = GamesLibraryFragment()
    }
}