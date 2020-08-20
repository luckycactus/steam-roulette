package ru.luckycactus.steamroulette.presentation.features.roulette

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper
import ru.luckycactus.steamroulette.presentation.utils.*
import kotlin.math.abs

@AndroidEntryPoint
class RouletteFragment : BaseFragment() {
    private val viewModel: RouletteViewModel by viewModels()

    private lateinit var fabs: List<FloatingActionButton>

    private lateinit var rouletteAdapter: RouletteAdapter

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    private lateinit var itemTouchHelper: ItemTouchHelper

    private var colorBackground: Int = 0

    override val layoutResId: Int = R.layout.fragment_roulette

    private val bgDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
    }
    private val bgGradientColors = IntArray(2)
    private val paletteHelper = PalettePageHelper {
        updateBgDrawable(it)
    }

    private val fabClickListener = View.OnClickListener { fab ->
        when (fab) {
            fabNextGame -> swipeTop(ItemTouchHelper.RIGHT)
            fabHideGame -> swipeTop(ItemTouchHelper.LEFT)
            fabGameInfo -> {
                rvRoulette.findViewHolderForAdapterPosition(0)?.itemView?.callOnClick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        colorBackground =
            MaterialColors.getColor(roulette_fragment_root, android.R.attr.colorBackground)
        bgGradientColors[1] = colorBackground
        updateBgDrawable(colorBackground)

        roulette_fragment_root.doOnLayout {
            val toolbarBgDrawable = ColorDrawable(colorBackground)
            val layerDrawable = LayerDrawable(arrayOf(bgDrawable, toolbarBgDrawable))
            layerDrawable.setLayerInset(0, 0, 0, 0, toolbar.height)
            layerDrawable.setLayerInset(1, 0, roulette_fragment_root.height - toolbar.height, 0, 0)
            roulette_fragment_root.background = layerDrawable
        }

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        avatarContainer.setOnClickListener {
            childFragmentManager.showIfNotExist(MENU_FRAGMENT_TAG) {
                MenuFragment.newInstance()
            }
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this)
                .load(it.avatarFull)
                .placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }

        fabs = listOf(fabNextGame, fabHideGame, fabGameInfo)

        val fabLongClickListener = View.OnLongClickListener {
            val text = getString(
                when (it) {
                    fabNextGame -> R.string.next_game
                    fabHideGame -> R.string.hide_game
                    fabGameInfo -> R.string.fab_info_hint
                    else -> 0
                }
            )
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            true
        }

        fabs.forEach {
            it.setOnLongClickListener(fabLongClickListener)
        }

        itemTouchHelper = ItemTouchHelper(CardStackTouchHelperCallback(
            onSwiped = {
                updatePaletteHelper(true)
            },
            onSwipedRight = {
                viewModel.onGameSwiped(false)
            },
            onSwipedLeft = {
                viewModel.onGameSwiped(true)
            },
            onSwipeProgress = { _progress, _ ->
                paletteHelper.edit {
                    progress = abs(_progress)
                }
                viewModel.onSwipeProgress(_progress)
            }
        ), 1.5f)
        with(rvRoulette) {
            itemTouchHelper.attachToRecyclerView(this)
            layoutManager = CardStackLayoutManager()
            rouletteAdapter = RouletteAdapter(::onGameClick, ::onPaletteReady)
            adapter = rouletteAdapter
            itemAnimator = null
            ViewGroupCompat.setTransitionGroup(this, true)
        }

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            content,
            viewModel::onRetryClick
        )

        lifecycleScope.launch {
            viewModel.itemRemoved.collect {
                rouletteAdapter.notifyItemRemoved(it)
            }
        }

        lifecycleScope.launch {
            viewModel.itemsInserted.collect {
                rouletteAdapter.notifyItemRangeInserted(it.first, it.second)
            }
        }

        observe(viewModel.games) {
            rouletteAdapter.items = it
            updatePaletteHelper()
        }

        observe(viewModel.contentState) {
            dataLoadingViewHolder.showContentState(it)
        }

        observe(viewModel.controlsAvailable) {
            val listener = if (it) fabClickListener else null
            fabs.forEach { fab -> fab.setOnClickListener(listener) }
        }

        lifecycleScope
    }

    private fun updatePaletteHelper(resetProgress: Boolean = false) {
        paletteHelper.edit {
            for (i in 0..1) {
                val vh =
                    (rvRoulette.findViewHolderForAdapterPosition(i) as? RouletteAdapter.RouletteViewHolder)
                setPageColor(i, getColorFromPalette(vh?.palette))
            }
            if (resetProgress)
                progress = 0f
        }
    }

    private fun getColorFromPalette(palette: Palette?): Int {
        if (palette == null)
            return Color.TRANSPARENT
        var color = palette.getVibrantColor(Color.TRANSPARENT)
        if (color != Color.TRANSPARENT) return color
        color = palette.getLightMutedColor(Color.TRANSPARENT)
        if (color != Color.TRANSPARENT) return color
        color = palette.getDominantColor(Color.TRANSPARENT)
        return color
    }

    private fun onPaletteReady(position: Int) {
        if (position in 0..1) {
            updatePaletteHelper()
        }
    }

    private fun updateBgDrawable(color: Int) {
        bgGradientColors[0] = MaterialColors.layer(colorBackground, color, BG_TINT_ALPHA)
        bgDrawable.colors = bgGradientColors
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roulette, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_roulette_options -> {
                childFragmentManager.showIfNotExist(FILTER_FRAGMENT_TAG) {
                    RouletteOptionsFragment.newInstance()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
    }

    private fun onGameClick(sharedViews: List<View>, game: GameHeader) {
        reenterTransition = createDefaultExitTransition().apply {
            listener(onTransitionEnd = {
                reenterTransition = null
            })
        }
        if (sharedViews.isNotEmpty()) {
            exitTransition = transitionSet {
                excludeTarget(rvRoulette, true)
                excludeTarget(roulette_fragment_root, true)
                slide()
                listener(onTransitionEnd = {
                    exitTransition = null
                    //todo comment
                    sharedViews.forEach {
                        it.trySetTransitionAlpha(1f)
                    }
                })
                addListener((activity as MainActivity).touchSwitchTransitionListener)
            }
        } else {
            exitTransition = createDefaultExitTransition().apply {
                listener(onTransitionEnd = {
                    exitTransition = null
                })
            }
        }
        (activity as MainActivity).onGameClick(sharedViews, game)
    }

    private fun createDefaultExitTransition() = transitionSet {
        slide {
            excludeTarget(roulette_fragment_root, true)
            excludeTarget(rvRoulette, true)
        }
        fade {
            addTarget(rvRoulette)
        }
        addListener((activity as MainActivity).touchSwitchTransitionListener)
    }

    private fun swipeTop(direction: Int) {
        rvRoulette.findViewHolderForAdapterPosition(0)?.let {
            itemTouchHelper.swipe(it, direction)
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
        private const val MENU_FRAGMENT_TAG = "MENU_FRAGMENT_TAG"
        private const val FILTER_FRAGMENT_TAG = "FILTER_FRAGMENT_TAG"

        private const val BG_TINT_ALPHA = 0.55f
    }
}