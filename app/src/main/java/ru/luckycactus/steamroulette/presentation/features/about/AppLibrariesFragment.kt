package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_app_libraries.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.observeFirst

@AndroidEntryPoint
class AppLibrariesFragment : BaseFragment() {
    private val viewModel: AppLibrariesViewModel by viewModels()

    override val layoutResId = R.layout.fragment_app_libraries

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.addSystemTopPadding()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        observeFirst(viewModel.libraries) {
            val adapter = AppLibrariesAdapter(it, ::onItemClick)
            rvAppLibraries.layoutManager = LinearLayoutManager(requireContext())
            rvAppLibraries.adapter = adapter
        }
    }

    private fun onItemClick(library: AppLibrary) {
        viewModel.onLibraryClick(library)
    }

    companion object {
        fun newInstance() = AppLibrariesFragment()
    }
}