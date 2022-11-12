package ru.luckycactus.steamroulette.presentation.features.roulette

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.doOnNextLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentRouletteBinding
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper
import ru.luckycactus.steamroulette.presentation.utils.doOnEnd
import ru.luckycactus.steamroulette.presentation.utils.extensions.*
import ru.luckycactus.steamroulette.presentation.utils.palette.PalettePageHelper
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import ru.luckycactus.steamroulette.presentation.utils.palette.TintContext
import ru.luckycactus.steamroulette.presentation.utils.slide
import ru.luckycactus.steamroulette.presentation.utils.transitionSet
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class RouletteFragment : BaseFragment(R.layout.fragment_roulette) {

    private val binding by viewBinding(FragmentRouletteBinding::bind)

    @Inject
    lateinit var viewModelFactory: RouletteViewModel.Factory

    private val viewModel: RouletteViewModel by assistedViewModel {
        viewModelFactory.create((activity as MainActivity).viewModel) // todo refactor
    }

    private lateinit var fabs: List<FloatingActionButton>

    private lateinit var rouletteAdapter: RouletteAdapter

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    private lateinit var itemTouchHelper: ItemTouchHelper


    //private lateinit var tintContext: TintContext
    private val paletteHelper = PalettePageHelper {
        //tintContext.updateColor(it, false)
    }

    private val fabClickListener = View.OnClickListener { fab ->
        when (fab.id) {
            R.id.fabNextGame -> swipe(ItemTouchHelper.RIGHT)
            R.id.fabHideGame -> swipe(ItemTouchHelper.LEFT)
            R.id.fabGameInfo -> {
                binding.rvRoulette.findViewHolderForAdapterPosition(0)?.itemView?.callOnClick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        fabs = listOf(fabNextGame, fabHideGame, fabGameInfo)

        root.addSystemBottomPadding()
        toolbar.root.addSystemTopPadding()

        setupTint()

        toolbar.root.setNavigationOnClickListener {
            childFragmentManager.showIfNotExist(MENU_FRAGMENT_TAG) {
                MenuFragment.newInstance()
            }
        }
        toolbar.root.inflateMenu(R.menu.menu_roulette)
        toolbar.root.setOnMenuItemClickListener(this@RouletteFragment::onMenuItemClick)

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

        rvRoulette.doOnNextLayout {
            val translationFraction = 1f + 0.3f + rvRoulette.left / rvRoulette.width.toFloat()
            itemTouchHelper = ItemTouchHelper(CardStackTouchHelperCallback(
                onSwiped = {
                    updatePaletteHelper(true)
                },
                onSwipedRight = {
                    viewModel.onGameSwiped(false)
                    analytics.logSelectContent("Game swipe", "right")
                },
                onSwipedLeft = {
                    viewModel.onGameSwiped(true)
                    analytics.logSelectContent("Game swipe", "left")
                },
                onSwipeProgress = { _progress, _ ->
                    paletteHelper.edit {
                        progress = abs(_progress)
                    }
                    viewModel.onSwipeProgress(_progress)
                }
            ), translationFraction)
            itemTouchHelper.attachToRecyclerView(rvRoulette)
        }
        with(rvRoulette) {
            layoutManager = CardStackLayoutManager()
            rouletteAdapter = RouletteAdapter(viewLifecycle, ::onGameClick, ::onPaletteReady)
            adapter = rouletteAdapter
            itemAnimator = null
        }

        dataLoadingViewHolder = DataLoadingViewHolder(
            empty.root,
            progress.root,
            content,
            viewModel::onRetryClick
        )

        viewLifecycleScope.launch {
            viewModel.itemRemovals.collect {
                rouletteAdapter.notifyItemRemoved(it)
            }
        }

        viewLifecycleScope.launch {
            viewModel.itemInsertions.collect {
                rouletteAdapter.notifyItemRangeInserted(it.first, it.second)
            }
        }

        observe(viewModel.games) {
            rouletteAdapter.items = it
            viewLifecycleScope.launch {
                delay(100)
                updatePaletteHelper()
            }
        }

        observe(viewModel.contentState) {
            dataLoadingViewHolder.showContentState(it)
        }

        observe(viewModel.controlsAvailable) {
            val listener = if (it) fabClickListener else null
            fabs.forEach { fab -> fab.setOnClickListener(listener) }
        }
    }

    private fun setupTint() {
        //tintContext = TintContext()

//        binding.root.background =
//            tintContext.createTintedBackgroundGradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM)

//        observe(tintContext.fabBackgroundTint) { colorStateList ->
//            fabs.forEach {
//                it.backgroundTintList = colorStateList
//            }
//        }
    }

    private fun updatePaletteHelper(resetProgress: Boolean = false) {
        paletteHelper.edit {
            for (i in 0..1) {
                val vh =
                    (binding.rvRoulette.findViewHolderForAdapterPosition(i) as? RouletteAdapter.RouletteViewHolder)
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

    private fun onMenuItemClick(item: MenuItem): Boolean {
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

    private fun onGameClick(game: GameHeader) {
//        reenterTransition = createExitTransition().apply {
//            doOnEnd {
//                reenterTransition = null
//            }
//        }
//        exitTransition = createExitTransition().apply {
//            doOnEnd {
//                exitTransition = null
//            }
//        }
        (activity as MainActivity).onGameClick(game, Color.TRANSPARENT) // todo compose
    }

    private fun createExitTransition() = transitionSet {
        slide(Gravity.BOTTOM) {
            fabs.forEach { addTarget(it) }
        }
        slide(Gravity.TOP) {
            addTarget(binding.toolbar.root)
        }
        addListener((activity as MainActivity).touchSwitchTransitionListener)
    }

    private fun swipe(direction: Int) {
        binding.rvRoulette.findViewHolderForAdapterPosition(0)?.let {
            itemTouchHelper.swipe(it, direction)
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
        private const val MENU_FRAGMENT_TAG = "MENU_FRAGMENT_TAG"
        private const val FILTER_FRAGMENT_TAG = "FILTER_FRAGMENT_TAG"
    }
}