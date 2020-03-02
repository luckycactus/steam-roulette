package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_system_reqs.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class SystemReqsFragment : BaseFragment() {
    private val appId: Int by argument(ARG_GAME_ID)

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().systemReqsViewModelFactory.create(appId)
    }

    override val layoutResId: Int = R.layout.fragment_system_reqs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        observe(viewModel.game) {
            toolbar.title = it.name
            vpSystemReqs.adapter = SystemReqsViewPagerAdapter(it.requirements)
            TabLayoutMediator(tabsLayoutSystemReqs, vpSystemReqs,
                TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                    tab.text = (vpSystemReqs.adapter as SystemReqsViewPagerAdapter).getPageTitle(position)
                }
            ).attach()
        }
    }

    companion object {
        private const val ARG_GAME_ID = "ARG_GAME_ID"

        fun newInstance(appId: Int) =
            SystemReqsFragment().apply {
                arguments = bundleOf(ARG_GAME_ID to appId)
            }
    }
}