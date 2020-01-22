package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_details.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.*
import javax.inject.Inject

class GameDetailsFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var gameDetailsAdapterFactory: GameDetailsAdapter.Factory

    lateinit var adapter: GameDetailsAdapter

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().gameDetailsViewModelFactory.create(
            arguments!!.getParcelable(ARG_GAME)!!
        )
    }

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val shouldPlaySharedElementTransition =
            arguments!!.getBoolean(ARG_ENABLE_TRANSITION) && savedInstanceState == null

        enterTransition = transitionSet {
            fade { }
        }

        if (shouldPlaySharedElementTransition) {
            sharedElementEnterTransition = transitionSet {
                changeBounds { }
                changeClipBounds { }
                changeTransform { }
            }

            postponeEnterTransition()
        }

        adapter = gameDetailsAdapterFactory.create(shouldPlaySharedElementTransition) {
            startPostponedEnterTransition()
        }

        rvGameDetails.adapter = adapter
        rvGameDetails.layoutManager = LinearLayoutManager(context)

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
        }
    }


    override fun inject() {
        findComponent<MainActivityComponent>().inject(this)
    }

    companion object {
        private const val ARG_GAME = "ARG_GAME"
        private const val ARG_ENABLE_TRANSITION = "ARG_ENABLE_SHARED_ELEMENT_TRANSITION"

        fun newInstance(game: OwnedGame, enableSharedElementTransition: Boolean) =
            GameDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_GAME, game)
                    putBoolean(ARG_ENABLE_TRANSITION, enableSharedElementTransition)
                }
            }

    }
}