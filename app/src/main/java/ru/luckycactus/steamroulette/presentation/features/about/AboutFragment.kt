package ru.luckycactus.steamroulette.presentation.features.about

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_about.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColorFromAttribute

@AndroidEntryPoint
class AboutFragment : BaseFragment() {
    private val viewModel: AboutViewModel by viewModels()
    override val layoutResId = R.layout.fragment_about

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        tvVersion.text = viewModel.version

        tvSourceCode.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvSourceCode.setOnClickListener {
            viewModel.onSourceCodeClick()
        }

        tvUsedLibraries.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvUsedLibraries.setOnClickListener {
            viewModel.onUsedLibrariesClick()
        }

        tvPrivacyPolicy.setDrawableColorFromAttribute(R.attr.colorOnSurface)
        tvPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }
    }

    companion object {
        fun newInstance() = AboutFragment()
    }
}