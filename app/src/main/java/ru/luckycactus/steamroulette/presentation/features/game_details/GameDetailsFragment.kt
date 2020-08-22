package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ArcMotion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_game_details.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.GameView
import ru.luckycactus.steamroulette.presentation.utils.*

@AndroidEntryPoint
class GameDetailsFragment : BaseFragment() {

    private val viewModel: GameDetailsViewModel by viewModels()

    lateinit var adapter: GameDetailsAdapter

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

        val fabInitialMargin = fabBack.marginBottom
        rvGameDetails.doOnApplyWindowInsets { it, insets, padding ->
            it.updatePadding(
                top = padding.top + insets.systemWindowInsetTop,
                bottom = padding.bottom + insets.systemWindowInsetBottom
            )
            fabBack.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                updateMargins(bottom = fabInitialMargin + insets.tappableElementInsets.bottom)
            }
            insets
        }

        fabBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        fabBack.doOnLayout {
            rvGameDetails.updatePadding(bottom = it.height + it.marginBottom)
        }

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
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
        private const val ARG_ENABLE_TRANSITION = "ARG_ENABLE_SHARED_ELEMENT_TRANSITION"

        fun newInstance(game: GameHeader, enableSharedElementTransition: Boolean) =
            GameDetailsFragment().apply {
                arguments = bundleOf(
                    GameDetailsViewModel.ARG_GAME to game,
                    ARG_ENABLE_TRANSITION to enableSharedElementTransition
                )
            }

    }
}