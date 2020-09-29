package ru.luckycactus.steamroulette.presentation.features.games

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_games.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.GridSpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.*

@AndroidEntryPoint
class GamesLibraryFragment : BaseFragment(), MessageDialogFragment.Callbacks {

    override val layoutResId = R.layout.fragment_games

    protected val adapter = GamesLibraryAdapter(::onGameClick)

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var inSelectionMode = false

    private val viewModel: GamesLibraryViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.run {
            addSystemTopPadding()
            setNavigationOnClickListener(::onNavigationIconClick)
            setTitle(R.string.my_steam_library)
            inflateMenu(R.menu.menu_games_library)
            setOnMenuItemClickListener(::onMenuItemClick)
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
        }

        searchView.run {
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text).filters += AlphaNumSpaceInputFilter()

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    clearFocus()
                    return onQueryTextChange(query)
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onSearchQueryChanged(newText)
                    return true
                }
            })
        }

        fab.run {
            setImageResource(R.drawable.ic_filter_list_24dp)
            backgroundTintList =
                ColorStateList.valueOf(MaterialColors.getColor(fab, R.attr.colorPrimary))
            setOnClickListener {

            }
        }

        rvGames.adapter = adapter
        rvGames.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvGames.addItemDecoration(
            GridSpaceDecoration(
                SPAN_COUNT,
                resources.getDimensionPixelSize(R.dimen.spacing_games_library)
            )
        )
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


        lifecycleScope.launch {
            viewModel.games.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onMessageDialogResult(
        dialog: MessageDialogFragment,
        result: MessageDialogFragment.Result
    ) {
        when (dialog.tag) {
            CONFIRM_CLEAR_DIALOG_TAG ->
                if (result == MessageDialogFragment.Result.Positive)
                    viewModel.clearAll()
        }

    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                showClearAllConfirmation()
                true
            }
            else -> false
        }
    }

    private fun showClearAllConfirmation() {
        MessageDialogFragment.create(
            requireContext(),
            messageResId = R.string.dialog_message_reset_hidden_games,
            negativeResId = R.string.cancel
        ).show(childFragmentManager, CONFIRM_CLEAR_DIALOG_TAG)
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
            mode.menuInflater.inflate(R.menu.menu_hidden_games_action_mode, menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_unhide -> {
                    viewModel.unhide(selectionTracker.selection.map { it.toInt() })
                    selectionTracker.clearSelection()
                    true
                }
                else -> false
            }
        }
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
        private const val CONFIRM_CLEAR_DIALOG_TAG = "CONFIRM_CLEAR_DIALOG"
        fun newInstance() = GamesLibraryFragment()
    }
}