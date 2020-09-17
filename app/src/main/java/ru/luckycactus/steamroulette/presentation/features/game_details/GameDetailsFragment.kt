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
import androidx.transition.Fade
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
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
import ru.luckycactus.steamroulette.presentation.utils.DEFAULT_TRANSITION_DURATION
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.doOnApplyWindowInsets
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import ru.luckycactus.steamroulette.presentation.utils.palette.TintContext

@AndroidEntryPoint
class GameDetailsFragment : BaseFragment() {

    private val viewModel: GameDetailsViewModel by viewModels()

    lateinit var adapter: GameDetailsAdapter

    private val waitForImage: Boolean by argument(ARG_WAIT_FOR_IMAGE)
    private val initialTintColor: Int by argument(ARG_COLOR)

    private lateinit var tintContext: TintContext

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        postponeEnterTransition()
        setupTransition()

        setupTint()

        adapter = GameDetailsAdapter(
            waitForImage && savedInstanceState == null,
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

        var insetsApplied = false
        val fabInitialMargin = fabBack.marginBottom
        rvGameDetails.doOnApplyWindowInsets { it, insets, padding ->
            it.updatePadding(
                top = padding.top + insets.systemWindowInsetTop,
                bottom = padding.bottom + insets.systemWindowInsetBottom + fabInitialMargin + fabBack.height
            )
            fabBack.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                updateMargins(bottom = fabInitialMargin + insets.tappableElementInsets.bottom)
            }
            insetsApplied = true
            insets
        }

        fabBack.doOnNextLayout {
            //if insets were applied already but fab isn't laid out yet, then we must set extra padding here
            if (insetsApplied)
                rvGameDetails.updatePadding(bottom = rvGameDetails.paddingBottom + fabBack.height)
        }

        rvGameDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                bgTint.translationY += -dy
            }
        })

        fabBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
        }
    }

    private fun setupTint() {
        tintContext = TintContext(requireContext(), tintColor = initialTintColor)

        bgTint.background =
            tintContext.createTintedBackgroundGradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM)

        observe(tintContext.fabBackgroundTint) {
            fabBack.backgroundTintList = it
        }
    }

    private fun onCoverChanged(bitmap: Bitmap?) {
        if (initialTintColor == 0 || viewModel.resolvedGameHasDifferentId()) {
            lifecycleScope.launch {
                val palette = bitmap?.let {
                    withContext(Dispatchers.Default) {
                        PaletteUtils.getGameCoverPalette(it).generate()
                    }
                }
                tintContext.updateColor(PaletteUtils.getColorForGameCover(palette))
            }
        }
    }

    private fun setupTransition() {
        enterTransition = Fade()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = DEFAULT_TRANSITION_DURATION
            setPathMotion(MaterialArcMotion())
            scrimColor = Color.TRANSPARENT
            // adding listener (even empty) breaks transition for some reason
//            addListener((activity as MainActivity).touchSwitchTransitionListener)
        }
    }

    companion object {
        private const val ARG_WAIT_FOR_IMAGE = "ARG_WAIT_FOR_IMAGE"
        private const val ARG_COLOR = "ARG_COLOR"

        fun newInstance(game: GameHeader, color: Int, waitForImage: Boolean) =
            GameDetailsFragment().apply {
                arguments = bundleOf(
                    GameDetailsViewModel.ARG_GAME to game,
                    ARG_COLOR to color,
                    ARG_WAIT_FOR_IMAGE to waitForImage
                )
            }

    }
}