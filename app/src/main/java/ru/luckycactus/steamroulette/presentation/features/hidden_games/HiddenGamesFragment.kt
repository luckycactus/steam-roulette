package ru.luckycactus.steamroulette.presentation.features.hidden_games

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_hidden_games.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.SimpleIdItemKeyProvider
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class HiddenGamesFragment : BaseFragment(), MessageDialogFragment.Callbacks {
    private val viewModel by viewModel {
        InjectionManager.findComponent<MainActivityComponent>().hiddenGamesViewModel
    }

    private val adapter = HiddenGamesAdapter()
    private lateinit var selectionTracker: SelectionTracker<Long>

    private lateinit var clearAllMenuItem: MenuItem
    private lateinit var unhideMenuItem: MenuItem

    private var inSelectionMode = false

    override val layoutResId = R.layout.fragment_hidden_games

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.apply {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            inflateMenu(R.menu.menu_hidden_games)
            clearAllMenuItem = toolbar.menu.findItem(R.id.action_clear_all)
            unhideMenuItem = toolbar.menu.findItem(R.id.action_unhide)
            unhideMenuItem.isVisible = false
        }
        setSelectionModeEnabled(false)

        unhideMenuItem.setOnMenuItemClickListener {
            viewModel.unhide(selectionTracker.selection.map { it.toInt() })
            selectionTracker.clearSelection()
            true
        }

        clearAllMenuItem.setOnMenuItemClickListener {
            showClearAllConfirmation()
            true
        }

        rvHiddenGames.adapter = adapter
        rvHiddenGames.layoutManager = GridLayoutManager(context, 4)

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
                setSelectionModeEnabled(selectionTracker.selection.size() > 0)
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

    private fun showClearAllConfirmation() {
        MessageDialogFragment.create(
            context!!,
            messageResId = R.string.dialog_message_reset_hidden_games,
            negativeResId = R.string.cancel
        ).show(childFragmentManager, null)
    }

    private fun setSelectionModeEnabled(enable: Boolean) {
        if (inSelectionMode != enable) {
            clearAllMenuItem.isVisible = !enable
            unhideMenuItem.isVisible = enable
            inSelectionMode = enable
        }
    }

    private class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as HiddenGameViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }

    companion object {
        fun newInstance() = HiddenGamesFragment()
    }
}