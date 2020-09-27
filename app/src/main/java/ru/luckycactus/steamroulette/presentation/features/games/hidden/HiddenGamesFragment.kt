package ru.luckycactus.steamroulette.presentation.features.games.hidden

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_games.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.games.base.BaseGamesLibraryFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.observe

@AndroidEntryPoint
class HiddenGamesFragment : BaseGamesLibraryFragment(), MessageDialogFragment.Callbacks {

    override val viewModel: HiddenGamesViewModel by viewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.apply {
            setTitle(R.string.hidden_games)
            setOnMenuItemClickListener(::onMenuItemClick)
            inflateMenu(R.menu.menu_hidden_games)
        }

        observe(viewModel.hiddenGamesCount) {
            toolbar.title = """${getString(R.string.hidden_games)} ($it)"""
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

    override fun isSelectionEnabled(): Boolean {
        return true
    }

    override fun onCreateSelectionActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_hidden_games_action_mode, menu)
        return true
    }

    override fun onSelectionActionItemClicked(
        mode: ActionMode,
        item: MenuItem,
        selectionTracker: SelectionTracker<Long>
    ): Boolean {
        return when (item.itemId) {
            R.id.action_unhide -> {
                viewModel.unhide(selectionTracker.selection.map { it.toInt() })
                selectionTracker.clearSelection()
                true
            }
            else -> false
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

    companion object {
        private const val CONFIRM_CLEAR_DIALOG_TAG = "CONFIRM_CLEAR_DIALOG"
        fun newInstance() = HiddenGamesFragment()
    }
}