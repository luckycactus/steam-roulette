package ru.luckycactus.steamroulette.presentation.features.roulette

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewGroupCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
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
    private var colorSurface: Int = 0

    override val layoutResId: Int = R.layout.fragment_roulette

    private val bgDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
    }
    private val bgGradientColors = IntArray(2)
    private var tintColor: Int = Color.TRANSPARENT
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

        fabs = listOf(fabNextGame, fabHideGame, fabGameInfo)

        toolbar.addSystemTopPadding()
        roulette_fragment_root.addSystemBottomPadding()

        colorBackground =
            MaterialColors.getColor(roulette_fragment_root, android.R.attr.colorBackground)
        colorSurface =
            MaterialColors.getColor(roulette_fragment_root, R.attr.colorSurface)

        roulette_fragment_root.background = bgDrawable
        bgGradientColors[1] = colorBackground
        updateBgDrawable(Color.TRANSPARENT)

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        toolbar.setNavigationIcon(R.drawable.ic_menu_24)
        toolbar.setNavigationOnClickListener {
            childFragmentManager.showIfNotExist(MENU_FRAGMENT_TAG) {
                MenuFragment.newInstance()
            }
        }

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
                setPageColor(i, PaletteUtils.getColorForGameCover(vh?.palette))
            }
            if (resetProgress)
                progress = 0f
        }
    }

    private fun onPaletteReady(position: Int) {
        if (position in 0..1) {
            updatePaletteHelper()
        }
    }

    private fun updateBgDrawable(color: Int) {
        tintColor = color
        bgGradientColors[0] = MaterialColors.layer(
            colorBackground,
            tintColor,
            PaletteUtils.GAME_COVER_BG_TINT_ALPHA
        )
        bgDrawable.colors = bgGradientColors

        val fabTint = ColorStateList.valueOf(
            MaterialColors.layer(
                colorSurface,
                tintColor,
                PaletteUtils.CONTROLS_TINT_ALPHA
            )
        )
        fabs.forEach {
            it.backgroundTintList = fabTint
        }
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
                slide(Gravity.BOTTOM) {
                    fabs.forEach { addTarget(it) }
                }
                slide(Gravity.TOP) {
                    addTarget(toolbar)
                }
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
        (activity as MainActivity).onGameClick(sharedViews, game, tintColor)
    }

    private fun createDefaultExitTransition() = transitionSet {
        slide(Gravity.BOTTOM) {
            fabs.forEach { addTarget(it) }
        }
        slide(Gravity.TOP) {
            addTarget(toolbar)
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
    }
}