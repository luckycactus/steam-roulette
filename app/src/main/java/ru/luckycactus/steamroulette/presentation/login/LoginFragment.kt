package ru.luckycactus.steamroulette.presentation.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.main.MainActivity
import ru.luckycactus.steamroulette.presentation.utils.*


class LoginFragment : BaseFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

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

        observe(viewModel.progressState) {
            showProgress(it)
        }

        observe(viewModel.loginButtonAvailableState) {
            btnOk.isEnabled = it
        }

        observe(viewModel.errorState) {
            showSnackbar(it)
        }

        observe(viewModel.signInSuccessEvent) {
            (activity as MainActivity).viewModel.onSignInSuccess()
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