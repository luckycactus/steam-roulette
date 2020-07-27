package ru.luckycactus.steamroulette.presentation.features.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.hideKeyboard
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.showSnackbar
import ru.luckycactus.steamroulette.presentation.utils.visibility

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by viewModels()

    override val layoutResId = R.layout.fragment_login

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        etUserId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onSteamIdInputChanged(s.toString())
            }
        })

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