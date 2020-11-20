package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.databinding.FragmentAppLibrariesBinding
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.viewLifecycleScope

@AndroidEntryPoint
class AppLibrariesFragment : BaseFragment<FragmentAppLibrariesBinding>() {
    private val viewModel: AppLibrariesViewModel by viewModels()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAppLibrariesBinding.inflate(inflater, container, false)

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