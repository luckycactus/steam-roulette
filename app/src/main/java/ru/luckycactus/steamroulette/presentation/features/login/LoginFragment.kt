package ru.luckycactus.steamroulette.presentation.features.login

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentLoginBinding
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.*

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        root.doOnApplyWindowInsets { view, insets, initialPadding ->
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
        with(binding) {
            progress.root.visibility(show)
            content.visibility(!show)
        }
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}