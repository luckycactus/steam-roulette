package ru.luckycactus.steamroulette.presentation.features.roulette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.utils.extensions.assistedViewModel
import ru.luckycactus.steamroulette.presentation.utils.extensions.showIfNotExist
import javax.inject.Inject

@AndroidEntryPoint
class RouletteFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: RouletteViewModel.Factory

    private val viewModel: RouletteViewModel by assistedViewModel {
        viewModelFactory.create((activity as MainActivity).viewModel) // todo refactor
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    RouletteRoute(
                        viewModel = viewModel,
                        onGameClick = ::onGameClick,
                        onMenuClick = ::onMenuClick,
                        onOptionsClick = ::onOptionsClick
                    )
                }
            }
        }
    }

    private fun onMenuClick() {
        childFragmentManager.showIfNotExist(MENU_FRAGMENT_TAG) {
            MenuFragment.newInstance()
        }
    }

    private fun onOptionsClick() {
        childFragmentManager.showIfNotExist(FILTER_FRAGMENT_TAG) {
            RouletteOptionsFragment.newInstance()
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val fabLongClickListener = View.OnLongClickListener {
//            val text = getString(
//                when (it) {
//                    fabNextGame -> R.string.next_game
//                    fabHideGame -> R.string.hide_game
//                    fabGameInfo -> R.string.fab_info_hint
//                    else -> 0
//                }
//            )
//            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
//            true
//        }
//
//        fabs.forEach {
//            it.setOnLongClickListener(fabLongClickListener)
//        }
//
//        observe(viewModel.controlsAvailable) {
//            val listener = if (it) fabClickListener else null
//            fabs.forEach { fab -> fab.setOnClickListener(listener) }
//        }
//    }

    // todo compose
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
    }

    private fun onGameClick(game: GameHeader, tintColor: Color?) {
//        reenterTransition = createExitTransition().apply {
//            doOnEnd {
//                reenterTransition = null
//            }
//        }
//        exitTransition = createExitTransition().apply {
//            doOnEnd {
//                exitTransition = null
//            }
//        }
        (activity as MainActivity).onGameClick(game, (tintColor ?: Color.Transparent).toArgb())
    }

//    private fun createExitTransition() = transitionSet {
//        slide(Gravity.BOTTOM) {
//            fabs.forEach { addTarget(it) }
//        }
//        slide(Gravity.TOP) {
//            addTarget(binding.toolbar.root)
//        }
//        addListener((activity as MainActivity).touchSwitchTransitionListener)
//    }

    companion object {
        fun newInstance() = RouletteFragment()
        private const val MENU_FRAGMENT_TAG = "MENU_FRAGMENT_TAG"
        private const val FILTER_FRAGMENT_TAG = "FILTER_FRAGMENT_TAG"
    }
}