package ru.luckycactus.steamroulette.presentation.features.game_details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ArcMotion
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_game_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val initialTintColor: Int by argument(ARG_COLOR)

    var colorBackground: Int = 0
    private val bgTintDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
    }
    private val bgTintColors = IntArray(2)

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        postponeEnterTransition()
        setupTransition()

        colorBackground =
            MaterialColors.getColor(game_details_fragment_root, android.R.attr.colorBackground)

        bgTint.background = bgTintDrawable
        bgTintColors[1] = Color.TRANSPARENT
        updateBgTint(initialTintColor)

        adapter = GameDetailsAdapter(
            enableTransition && savedInstanceState == null,
            ::startPostponedEnterTransition,
            ::onCoverChanged,
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

        rvGameDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                bgTint.translationY += -dy
            }
        })

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

    private fun onCoverChanged(bitmap: Bitmap?) {
        if (initialTintColor == 0 || viewModel.resolvedGameHasDifferentId()) {
            lifecycleScope.launch {
                val palette = bitmap?.let {
                    withContext(Dispatchers.Default) {
                        Palette.from(it).generate()
                    }
                }
                updateBgTint(PaletteUtils.getColorForGameCover(palette))
            }
        }
    }

    private fun updateBgTint(color: Int) {
        bgTintColors[0] = MaterialColors.layer(
            colorBackground,
            color,
            PaletteUtils.GAME_COVER_BG_TINT_ALPHA
        )
        bgTintDrawable.colors = bgTintColors
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
        private const val ARG_COLOR = "ARG_COLOR"

        fun newInstance(game: GameHeader, color: Int, enableSharedElementTransition: Boolean) =
            GameDetailsFragment().apply {
                arguments = bundleOf(
                    GameDetailsViewModel.ARG_GAME to game,
                    ARG_COLOR to color,
                    ARG_ENABLE_TRANSITION to enableSharedElementTransition
                )
            }

    }
}