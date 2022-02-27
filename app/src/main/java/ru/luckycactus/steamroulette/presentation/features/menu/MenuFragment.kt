package ru.luckycactus.steamroulette.presentation.features.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.assistedViewModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.viewLifecycleScope
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment : BaseBottomSheetDialogFragment(),
    MessageDialogFragment.Callbacks {

    @Inject
    lateinit var viewModelFactory: MenuViewModel.Factory

    private val viewModel: MenuViewModel by assistedViewModel {
        viewModelFactory.create((activity as MainActivity).viewModel) // todo refactor
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    ProvideWindowInsets {
                        MenuScreen(
                            viewModel = viewModel,
                            onExitClick = this@MenuFragment::showExitDialog
                        )
                    }
                }
            }
        }
    }

    private fun showExitDialog() {
        MessageDialogFragment.create(
            requireContext(),
            titleResId = R.string.exit_dialog_title,
            messageResId = R.string.exit_warning,
            negativeResId = R.string.cancel
        ).show(childFragmentManager, CONFIRM_EXIT_DIALOG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.closeAction
                    .onEach { dismiss() }
                    .launchIn(this)
            }
        }
    }

    override fun onMessageDialogResult(
        dialog: MessageDialogFragment,
        result: MessageDialogFragment.Result
    ) {
        when (dialog.tag) {
            CONFIRM_EXIT_DIALOG -> {
                if (result == MessageDialogFragment.Result.Positive)
                    viewModel.logout()
            }
        }
    }

    companion object {
        private const val CONFIRM_EXIT_DIALOG = "CONFIRM_DIALOG"
        fun newInstance() = MenuFragment()
    }
}