package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ArcMotion
import kotlinx.android.synthetic.main.fragment_game_details.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.GameView
import ru.luckycactus.steamroulette.presentation.utils.*

class GameDetailsFragment : BaseFragment() {

    lateinit var adapter: GameDetailsAdapter

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().gameDetailsViewModelFactory.create(
            requireArguments().getParcelable(ARG_GAME)!!
        )
    }

    private val enableTransition: Boolean by argument(ARG_ENABLE_TRANSITION)

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        postponeEnterTransition()
        setupTransition()

        adapter = GameDetailsAdapter(
            enableTransition && savedInstanceState == null,
            ::startPostponedEnterTransition,
            viewModel
        )

        with(rvGameDetails) {
            this.adapter = this@GameDetailsFragment.adapter
            layoutManager = LinearLayoutManager(context)
            val margin = resources.getDimensionPixelSize(R.dimen.spacing_small)
            addItemDecoration(SpaceDecoration(margin, 0, 0))
        }

        fabBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
        }

        fabBack.doOnLayout {
            rvGameDetails.updatePadding(bottom = it.height + it.marginBottom)
        }
    }

    private fun setupTransition() {
        enterTransition = transitionSet {
            fade()
        }

        if (enableTransition) {
            returnTransition = transitionSet {
                excludeTarget(GameView::class.java, true)
                fade()
            }
        }

        sharedElementEnterTransition = transitionSet {
            //to avoid weird bug when enter animation ends before shared element animation and
            //shared views don't invalidate for some reason and stuck in intermediate state
            duration = DEFAULT_TRANSITION_DURATION - 25L
            changeTransform()
            changeClipBounds()
            changeBounds()
            setPathMotion(ArcMotion())
        }
    }

    companion object {
        private const val ARG_GAME = "ARG_GAME"
        private const val ARG_ENABLE_TRANSITION = "ARG_ENABLE_SHARED_ELEMENT_TRANSITION"

        fun newInstance(game: GameHeader, enableSharedElementTransition: Boolean) =
            GameDetailsFragment().apply {
                arguments = bundleOf(
                    ARG_GAME to game,
                    ARG_ENABLE_TRANSITION to enableSharedElementTransition
                )
            }

    }
}