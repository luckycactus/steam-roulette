package ru.luckycactus.steamroulette.presentation.features.roulette

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewGroupCompat
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.features.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper
import ru.luckycactus.steamroulette.presentation.utils.*
import javax.inject.Inject

class RouletteFragment : BaseFragment(), Injectable {

    private lateinit var fabs: List<FloatingActionButton>

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().rouletteViewModel
    }

    @Inject
    lateinit var rouletteAdapterFactory: RouletteAdapter.Factory

    private val rouletteAdapter: RouletteAdapter by lazyNonThreadSafe {
        rouletteAdapterFactory.create(::onGameClick)
    }

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    //private var fabsAlphaRecoveryAnimator: Animator? = null

    private lateinit var itemTouchHelper: ItemTouchHelper

    override val layoutResId: Int = R.layout.fragment_roulette

    private val fabClickListener = View.OnClickListener { fab ->
        when (fab) {
            fabNextGame -> swipeTop(ItemTouchHelper.RIGHT)
            fabHideGame -> swipeTop(ItemTouchHelper.LEFT)
            fabGameInfo -> {
                rvRoulette.findViewHolderForAdapterPosition(0)?.let {
                    it.itemView.callOnClick()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun inject() {
        findComponent<MainFlowComponent>().inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fabs = listOf(fabNextGame, fabHideGame, fabGameInfo)

        val fabLongClickListener = View.OnLongClickListener {
            val text = getString(
                when (it) {
                    fabNextGame -> R.string.next_game
                    fabHideGame -> R.string.hide_game
                    fabGameInfo -> R.string.open_game_store_page
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
                //animateFabsAlphaRecovery()
            },
            onSwipedRight = {
                viewModel.onGameSwiped(false)
                //rouletteAdapter.notifyItemRemoved(0)
                viewModel.onAdapterUpdatedAfterSwipe()
            },
            onSwipedLeft = {
                viewModel.onGameSwiped(true)
                //rouletteAdapter.notifyItemRemoved(0)
                viewModel.onAdapterUpdatedAfterSwipe()
            },
            onSwipeProgress = { progress, _ ->
                //                fabsAlphaRecoveryAnimator?.let {
//                    it.cancel()
//                    fabsAlphaRecoveryAnimator = null
//                }
//                val fraction = 0.8f * progress
//                fabNextGame.alpha = 1f + fraction
//                fabHideGame.alpha = 1f - fraction
//                fabSteamInfo.alpha = 1f - fraction.absoluteValue
                viewModel.onSwipeProgress(progress)
            }
        ), 1.5f)
        itemTouchHelper.attachToRecyclerView(rvRoulette)
        rvRoulette.layoutManager = CardStackLayoutManager()
        rvRoulette.adapter = rouletteAdapter
        rvRoulette.itemAnimator = null
        ViewGroupCompat.setTransitionGroup(rvRoulette, true)

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            content,
            viewModel::onRetryClick
        )

        observe(
            viewModel.itemRemoved
        ) {
            it?.ifNotHandled {
                rouletteAdapter.notifyItemRemoved(it)
            }
        }

        observe(
            viewModel.itemsInserted
        ) {
            it?.ifNotHandled {
                rouletteAdapter.notifyItemRangeInserted(it.first, it.second)
            }
        }

        observe(viewModel.games) {
            rouletteAdapter.items = it
        }

        observe(viewModel.contentState) {
            dataLoadingViewHolder.showContentState(it)
        }

        observe(viewModel.controlsAvailable) {
            val listener = if (it) fabClickListener else null
            fabs.forEach { fab -> fab.setOnClickListener(listener) }
        }

        observeEvent(viewModel.openUrlAction) {
            (activity as MainActivity).openUrl(it, true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roulette, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_roulette_options -> {
                childFragmentManager.showIfNotExist(MainFlowFragment.FILTER_FRAGMENT_TAG) {
                    RouletteOptionsFragment.newInstance()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    private fun animateFabsAlphaRecovery() {
//        fabsAlphaRecoveryAnimator = AnimatorSet().apply {
//            play(ObjectAnimator.ofFloat(fabNextGame, View.ALPHA, 1f))
//                .with(ObjectAnimator.ofFloat(fabHideGame, View.ALPHA, 1f))
//                .with(ObjectAnimator.ofFloat(fabSteamInfo, View.ALPHA, 1f))
//            setDuration(200L)
//                .start()
//        }
//    }

    private fun onGameClick(sharedViews: List<View>, game: OwnedGame) {
        requireParentFragment().reenterTransition = createDefaultExitTransition()
        if (sharedViews.isNotEmpty()) {
            requireParentFragment().exitTransition = transitionSet {
                excludeTarget(rvRoulette, true)
                slide()
                listener(
                    onTransitionEnd = {
                        parentFragment?.exitTransition = null
                        //todo comment
                        sharedViews.forEach {
                            it.trySetTransitionAlpha(1f)
                        }
                    }
                )
                addListener(touchSwitchListener)
            }
        } else {
            requireParentFragment().exitTransition = createDefaultExitTransition()
        }
        (activity as MainActivity).onGameClick(sharedViews, game)
    }

    private fun createDefaultExitTransition() = transitionSet {

        slide {
            excludeTarget(rvRoulette, true)
        }
        fade {
            addTarget(rvRoulette)
        }
        addListener(touchSwitchListener)
    }

    private val touchSwitchListener = object : TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) {
            (activity as MainActivity).touchAndBackPressEnabled = true
        }

        override fun onTransitionStart(transition: Transition) {
            (activity as MainActivity).touchAndBackPressEnabled = false
        }
    }

    private fun swipeTop(direction: Int) {
        rvRoulette.findViewHolderForAdapterPosition(0)?.let {
            itemTouchHelper.swipe(it, direction)
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}