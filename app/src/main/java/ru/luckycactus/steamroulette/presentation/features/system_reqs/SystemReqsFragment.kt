package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_system_reqs.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.argument
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class SystemReqsFragment : BaseFragment() {
    private val gameHeader: GameHeader by argument(ARG_GAME)

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().systemReqsViewModelFactory.create(gameHeader)
    }

    override val layoutResId: Int = R.layout.fragment_system_reqs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observe(viewModel.systemReqs) {
            vpSystemReqs.adapter = SystemReqsViewPagerAdapter(it)
            TabLayoutMediator(tabsLayoutSystemReqs, vpSystemReqs,
                TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                    tab.text = (vpSystemReqs.adapter as SystemReqsViewPagerAdapter).getPageTitle(position)
                }
            ).attach()
        }
    }

    companion object {
        const val ARG_GAME = "ARG_GAME"

        fun newInstance(game: GameHeader) =
            SystemReqsFragment().apply {
                arguments = bundleOf(ARG_GAME to game)
            }
    }
}