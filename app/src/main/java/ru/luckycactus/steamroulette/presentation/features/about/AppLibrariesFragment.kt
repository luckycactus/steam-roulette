package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.FragmentAppLibrariesBinding
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.viewLifecycleScope

@AndroidEntryPoint
class AppLibrariesFragment : BaseFragment(R.layout.fragment_app_libraries) {

    private val binding by viewBinding(FragmentAppLibrariesBinding::bind)

    private val viewModel: AppLibrariesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.addSystemTopPadding()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        viewLifecycleScope.launch {
            val adapter = AppLibrariesAdapter(viewModel.getLibraries(), ::onItemClick)
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