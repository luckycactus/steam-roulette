package ru.luckycactus.steamroulette.presentation.roulette.options

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_options_filter.*
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.visibility


class RouletteOptionsFragment : BaseBottomSheetDialogFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(RouletteOptionsViewModel::class.java)) {
                    //todo!!!
                    val mainFlowViewModel =
                        (parentFragment?.parentFragment as MainFlowFragment).viewModel
                    RouletteOptionsViewModel(mainFlowViewModel) as T
                } else {
                    throw IllegalArgumentException("ViewModel Not Found")
                }
            }
        }).get(RouletteOptionsViewModel::class.java)
    }

    private val adapter by lazyNonThreadSafe {
        OptionsFilterAdapter {
            viewModel.onPlayTimeFilterSelect(it.tag as EnPlayTimeFilter)
        }
    }

    override val layoutResId = ru.luckycactus.steamroulette.R.layout.fragment_options_filter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvGameTimeFilter.layoutManager = LinearLayoutManager(context)
        rvGameTimeFilter.adapter = adapter


        observe(viewModel.playTimeFilterData) {
            adapter.submitList(it)
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.hiddenGamesCount) {
            tvHiddenGamesCount.text = it.toString()
            btnClearHiddenGames.isEnabled = it > 0
            //todo change color
        }

        btnClearHiddenGames.setOnClickListener {
            //todo подтверждение
            viewModel.onClearHiddenGames()
        }
    }

    companion object {
        fun newInstance() =
            RouletteOptionsFragment()
    }
}