package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_system_reqs.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class SystemReqsFragment : BaseFragment() {
    private val appName: String by argument(ARG_APP_NAME)
    private val systemReqs: List<SystemRequirements> by argument(ARG_SYSTEM_REQS)

    override val layoutResId: Int = R.layout.fragment_system_reqs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        toolbar.title = appName
        vpSystemReqs.adapter = SystemReqsViewPagerAdapter(systemReqs)
        TabLayoutMediator(tabsLayoutSystemReqs, vpSystemReqs,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text =
                    (vpSystemReqs.adapter as SystemReqsViewPagerAdapter).getPageTitle(position)
            }
        ).attach()
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