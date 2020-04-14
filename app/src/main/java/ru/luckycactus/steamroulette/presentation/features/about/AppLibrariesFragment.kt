package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_app_libraries.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.observeFirst
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class AppLibrariesFragment : BaseFragment() {

    private val viewModel by viewModel {
        InjectionManager.findComponent<MainActivityComponent>().appLibrariesViewModel
    }

    override val layoutResId = R.layout.fragment_app_libraries

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        observeFirst(viewModel.libraries) {
            val adapter = AppLibrariesAdapter(it, ::onItemClick)
            rvAppLibraries.layoutManager = LinearLayoutManager(requireContext())
            rvAppLibraries.adapter = adapter
            Log.d("ololo", it.toString())
        }
    }

    private fun onItemClick(library: AppLibrary) {
        viewModel.onLibraryClick(library)
    }

    companion object {
        fun newInstance() = AppLibrariesFragment()
    }
}