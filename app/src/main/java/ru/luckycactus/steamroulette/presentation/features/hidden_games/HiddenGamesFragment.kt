package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.os.Bundle
import android.view.*
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_hidden_games.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.GridSpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.SimpleIdItemKeyProvider
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.*


class HiddenGamesFragment : BaseFragment(), MessageDialogFragment.Callbacks {
    private val viewModel by viewModel {
        InjectionManager.findComponent<MainActivityComponent>().hiddenGamesViewModel
    }

    private val adapter = HiddenGamesAdapter(::onGameClick)

    private lateinit var selectionTracker: SelectionTracker<Long>

    private var inSelectionMode = false

    private var actionMode: ActionMode? = null
    override val layoutResId = R.layout.fragment_hidden_games

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.apply {
            setNavigationOnClickListener(::onNavigationIconClick)
            setOnMenuItemClickListener(::onMenuItemClick)
            inflateMenu(R.menu.menu_hidden_games)
        }

        rvHiddenGames.adapter = adapter
        rvHiddenGames.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvHiddenGames.addItemDecoration(
            GridSpaceDecoration(
                SPAN_COUNT,
                resources.getDimensionPixelSize(R.dimen.spacing_small)
            )
        )

        selectionTracker = SelectionTracker.Builder<Long>(
            "hidden_games",
            rvHiddenGames,
            SimpleIdItemKeyProvider(rvHiddenGames),
            MyItemDetailsLookup(rvHiddenGames),
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

        observe(viewModel.hiddenGames) {
            adapter.submitList(it)
        }
    }

    override fun onDialogPositiveClick(dialog: MessageDialogFragment, tag: String?) {
        viewModel.clearAll()
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                showClearAllConfirmation()
                true
            }
            R.id.action_unhide -> {
                viewModel.unhide(selectionTracker.selection.map { it.toInt() })
                selectionTracker.clearSelection()
                true
            }
            else -> false
        }
    }

    private fun onGameClick(sharedViews: List<View>, game: GameHeader) {
        reenterTransition = createDefaultExitTransition()
        if (sharedViews.isNotEmpty()) {
            exitTransition = transitionSet {
                //excludeTarget(rvRoulette, true)
                fade()
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
            exitTransition = createDefaultExitTransition()
        }
        (activity as MainActivity).onGameClick(sharedViews, game)
    }


    private fun createDefaultExitTransition() = transitionSet {
        fade {}
        addListener((activity as MainActivity).touchSwitchTransitionListener)
    }

    private fun onNavigationIconClick(view: View) {
        if (inSelectionMode)
            selectionTracker.clearSelection()
        else
            requireActivity().onBackPressed()
    }

    private fun showClearAllConfirmation() {
        MessageDialogFragment.create(
            context!!,
            messageResId = R.string.dialog_message_reset_hidden_games,
            negativeResId = R.string.cancel
        ).show(childFragmentManager, null)
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
            onMenuItemClick(item)
            return true
        }
    }

    private class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as HiddenGamesAdapter.HiddenGameViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
        fun newInstance() = HiddenGamesFragment()
    }
}