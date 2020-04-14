package ru.luckycactus.steamroulette.presentation.features.about

import android.annotation.SuppressLint
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_about.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColorFromAttribute
import ru.luckycactus.steamroulette.presentation.utils.viewModel
import javax.inject.Inject

class AboutFragment : BaseFragment() {
    private val viewModel by viewModel {
        InjectionManager.findComponent<MainActivityComponent>().aboutViewModel
    }
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