package ru.luckycactus.steamroulette.presentation.features.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.analytics.Events
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    LoginRoute(
                        viewModel = viewModel(),
                        this@LoginFragment::showHelpDialog
                    )
                }
            }
        }
    }

    private fun showHelpDialog() {
        MessageDialogFragment.create(
            requireContext(),
            titleResId = R.string.supported_steamid_formats,
            messageResId = R.string.steamid_help
        ).show(childFragmentManager, null)
        analyticsNew.track(Events.Click("Login help"))
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}