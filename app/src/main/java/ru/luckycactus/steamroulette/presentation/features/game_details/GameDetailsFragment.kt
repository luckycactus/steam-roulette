package ru.luckycactus.steamroulette.presentation.features.game_details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import androidx.transition.Fade
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentGameDetailsBinding
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.DEFAULT_TRANSITION_DURATION
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument
import ru.luckycactus.steamroulette.presentation.utils.extensions.doOnApplyWindowInsets
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.viewLifecycleScope
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import ru.luckycactus.steamroulette.presentation.utils.palette.TintContext
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class GameDetailsFragment : BaseFragment(R.layout.fragment_game_details) {

    private val binding by viewBinding(FragmentGameDetailsBinding::bind)

    private val viewModel: GameDetailsViewModel by viewModels()

    lateinit var adapter: GameDetailsAdapter

    private var initialTintColor: Int by argument()
    private var game: GameHeader by argument(GameDetailsViewModel.ARG_GAME)

    private lateinit var tintContext: TintContext

    private var tintIsReady = false
    private var transitionStarted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition(200, TimeUnit.MILLISECONDS)
        setupTransition()

        setupTint()

        tintIsReady = initialTintColor != 0

        adapter = GameDetailsAdapter(
            ::onHeaderImageChanged,
            viewModel
        )
        // we need to get scroll position to set bg translation
        adapter.stateRestorationPolicy = PREVENT_WHEN_EMPTY

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

        rvGameDetails.doOnLayout {
            bgTint.translationY = -rvGameDetails.computeVerticalScrollOffset().toFloat()
        }

        rvGameDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                bgTint.translationY = minOf(0f, bgTint.translationY - dy)
            }
        })

        fabBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
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

    private fun setupTint() {
        tintContext = TintContext(requireContext(), tintColor = initialTintColor)

        binding.bgTint.background =
            tintContext.createTintedBackgroundGradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM)

        observe(tintContext.fabBackgroundTint) {
            binding.fabBack.backgroundTintList = it
        }
    }

    private fun onHeaderImageChanged(bitmap: Bitmap?) {
        if (tintContext.tintColor != 0 && viewModel.resolvedGameHasSameId())
            return
        if (tintContext.tintColor == 0 && bitmap == null)
            return
        viewLifecycleScope.launch {
            val palette = PaletteUtils.generateGameCoverPalette(bitmap)
            tintContext.updateColor(
                PaletteUtils.getColorForGameCover(palette),
                animate = transitionStarted
            )
            tintIsReady = true
            startTransitionIfNotStartedAndReady()
        }
    }

    private fun startTransitionIfNotStartedAndReady() {
        if (!transitionStarted && tintIsReady) {
            startPostponedEnterTransition()
            transitionStarted = true
        }
    }

    companion object {
        fun newInstance(game: GameHeader, color: Int) =
            GameDetailsFragment().apply {
                this.initialTintColor = color
                this.game = game
            }

    }
}