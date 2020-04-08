package ru.luckycactus.steamroulette.presentation.features.roulette

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewGroupCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import kotlinx.android.synthetic.main.main_toolbar.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper
import ru.luckycactus.steamroulette.presentation.utils.*

class RouletteFragment : BaseFragment() {

    private lateinit var fabs: List<FloatingActionButton>

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().rouletteViewModel
    }

    private lateinit var rouletteAdapter: RouletteAdapter

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
            onSwiped = {},
            onSwipedRight = {
                viewModel.onGameSwiped(false)
            },
            onSwipedLeft = {
                viewModel.onGameSwiped(true)
            },
            onSwipeProgress = { progress, _ ->
                viewModel.onSwipeProgress(progress)
            }
        ), 1.5f)
        with(rvRoulette) {
            itemTouchHelper.attachToRecyclerView(this)
            layoutManager = CardStackLayoutManager()
            adapter = RouletteAdapter(::onGameClick).also {
                rouletteAdapter = it
            }
            itemAnimator = null
            ViewGroupCompat.setTransitionGroup(this, true)
        }

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
        //xcludeChildren(roulette_fragment_root, true)
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
    }
}