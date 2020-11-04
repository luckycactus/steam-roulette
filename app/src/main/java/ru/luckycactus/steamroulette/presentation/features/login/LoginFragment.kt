package ru.luckycactus.steamroulette.presentation.features.login

import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.*

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by viewModels()

    override val layoutResId = R.layout.fragment_login

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        login_fragment_root.doOnApplyWindowInsets { view, insets, initialPadding ->
            view.updatePadding(
                top = initialPadding.top + insets.systemWindowInsetTop,
                bottom = initialPadding.bottom + insets.systemWindowInsetBottom
            )
            insets
        }

        etUserId.doOnTextChanged { text, _, _, _ ->
            viewModel.onSteamIdInputChanged(text.toString())
        }

        btnOk.setOnClickListener {
            activity?.hideKeyboard()
            viewModel.onSteamIdConfirmed(etUserId.text.toString())
        }

        tvSteamIdHelp.setOnClickListener {
            MessageDialogFragment.create(
                requireContext(),
                titleResId = R.string.supported_steamid_formats,
                messageResId = R.string.steamid_help
            ).show(childFragmentManager, null)
            analytics.logClick("Login help")
        }

        observe(viewModel.progressState) {
            showProgress(it)
        }

        observe(viewModel.loginButtonAvailableState) {
            btnOk.isEnabled = it
        }

        observe(viewModel.errorState) {
            showSnackbar(it)
        }
    }

    private fun showProgress(show: Boolean) {
        progress.visibility(show)
        content.visibility(!show)
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}