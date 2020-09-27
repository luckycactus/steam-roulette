package ru.luckycactus.steamroulette.presentation.features.games.base

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_games.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.GridSpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.doOnEnd
import ru.luckycactus.steamroulette.presentation.utils.fade
import ru.luckycactus.steamroulette.presentation.utils.transitionSet

abstract class BaseGamesLibraryFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_games

    protected val adapter = GamesLibraryAdapter(::onGameClick)

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var inSelectionMode = false

    abstract val viewModel: BaseGamesLibraryViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.addSystemTopPadding()
        toolbar.setNavigationOnClickListener(::onNavigationIconClick)

        rvGames.adapter = adapter
        rvGames.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvGames.addItemDecoration(
            GridSpaceDecoration(SPAN_COUNT, resources.getDimensionPixelSize(R.dimen.spacing_games_library))
        )
        if (isSelectionEnabled()) {
            selectionTracker = SelectionTracker.Builder(
                "games",
                rvGames,
                GamesLibraryItemKeyProvider(rvGames),
                GamesLibraryDetailsLookup(rvGames),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build()

            selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val selectionSize = selectionTracker.selection.size()
                    setSelectionModeEnabled(selectionSize > 0)
                    actionMode?.title = selectionSize.toString()
                }
            })

            adapter.tracker = selectionTracker
        }

        lifecycleScope.launch {
            viewModel.games.collect {
                adapter.submitData(it)
            }
        }
    }

    private fun onGameClick(game: GameHeader, sharedViews: List<View>, imageIsReady: Boolean) {
        reenterTransition = createExitTransition()
        exitTransition = createExitTransition().apply {
            doOnEnd {
                exitTransition = null
            }
        }
        (activity as MainActivity).onGameClick(game, sharedViews, imageIsReady)
    }

    private fun onNavigationIconClick(view: View) {
        if (inSelectionMode)
            selectionTracker.clearSelection()
        else
            requireActivity().onBackPressed()
    }

    private fun setSelectionModeEnabled(enable: Boolean) {
        if (inSelectionMode != enable) {
            inSelectionMode = enable
            if (enable)
                actionMode = toolbar.startActionMode(actionModeCallback)
            else
                actionMode?.finish()
            val toolbarParams = toolbar.layoutParams as AppBarLayout.LayoutParams
            toolbarParams.scrollFlags = if (enable)
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            else
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            toolbar.layoutParams = toolbarParams
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onDestroyActionMode(mode: ActionMode) {
            selectionTracker.clearSelection()
            actionMode = null
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return this@BaseGamesLibraryFragment.onCreateSelectionActionMode(mode, menu)
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return this@BaseGamesLibraryFragment.onSelectionActionItemClicked(mode, item, selectionTracker)
        }
    }

    protected open fun isSelectionEnabled(): Boolean {
        return false
    }

    protected open fun onCreateSelectionActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    protected open fun onSelectionActionItemClicked(
        mode: ActionMode,
        item: MenuItem,
        selectionTracker: SelectionTracker<Long>
    ): Boolean {
        return false
    }

    private fun createExitTransition() = transitionSet {
        fade {}
        addListener((activity as MainActivity).touchSwitchTransitionListener)
    }

    private class GamesLibraryDetailsLookup(
        private val recyclerView: RecyclerView
    ) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            return if (view != null)
                (recyclerView.getChildViewHolder(view) as GamesLibraryAdapter.GameViewHolder)
                    .getItemDetails()
            else null
        }
    }

    private class GamesLibraryItemKeyProvider(
        recyclerView: RecyclerView
    ) : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        private val adapter: GamesLibraryAdapter = (recyclerView.adapter as? GamesLibraryAdapter)
            ?: throw IllegalStateException("RecyclerView must have GamesLibraryAdapter set")
        private val lm = (recyclerView.layoutManager as? GridLayoutManager)
            ?: throw IllegalStateException("RecyclerView must have GridLayoutManager set")

        override fun getKey(position: Int): Long? = adapter.getSelectionKeyForPosition(position)

        override fun getPosition(key: Long): Int {
            for (i in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
                if (key == adapter.getSelectionKeyForPosition(i))
                    return i
            }
            return RecyclerView.NO_POSITION
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
    }
}