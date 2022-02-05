package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import ru.luckycactus.steamroulette.databinding.FragmentSystemReqsBinding
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

class SystemReqsFragment : BaseFragment() {

    private val binding by viewBinding(FragmentSystemReqsBinding::bind)

    private var appName: String by argument()
    private var systemReqs: List<SystemRequirements> by argument()

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
        fun newInstance(appName: String, systemReqs: List<SystemRequirements>) =
            SystemReqsFragment().apply {
                this.appName = appName
                this.systemReqs = systemReqs
            }
    }
}