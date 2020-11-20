package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.tabs.TabLayoutMediator
import ru.luckycactus.steamroulette.databinding.FragmentSystemReqsBinding
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

class SystemReqsFragment : BaseFragment<FragmentSystemReqsBinding>() {
    private val appName: String by argument(ARG_APP_NAME)
    private val systemReqs: List<SystemRequirements> by argument(ARG_SYSTEM_REQS)

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSystemReqsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        appBarLayout.addSystemTopPadding()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        toolbar.title = appName
        vpSystemReqs.adapter = SystemReqsViewPagerAdapter(systemReqs)
        TabLayoutMediator(tabsLayoutSystemReqs, vpSystemReqs) { tab, position ->
            tab.text =
                (vpSystemReqs.adapter as SystemReqsViewPagerAdapter).getPageTitle(position)
        }.attach()
    }

    companion object {
        private const val ARG_APP_NAME = "ARG_APP_NAME"
        private const val ARG_SYSTEM_REQS = "ARG_SYSTEM_REQS"

        fun newInstance(appName: String, systemReqs: List<SystemRequirements>) =
            SystemReqsFragment().apply {
                arguments = bundleOf(
                    ARG_APP_NAME to appName,
                    ARG_SYSTEM_REQS to systemReqs
                )
            }
    }
}