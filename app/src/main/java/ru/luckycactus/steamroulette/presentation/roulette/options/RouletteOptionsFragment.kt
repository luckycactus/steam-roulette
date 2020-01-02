package ru.luckycactus.steamroulette.presentation.roulette.options

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_options_filter.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.viewModel


class RouletteOptionsFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().rouletteOptionsViewModel
    }

    private val adapter by lazyNonThreadSafe {
        OptionsFilterAdapter {
            viewModel.onPlayTimeFilterSelect(it.tag as EnPlayTimeFilter)
        }
    }

    override val layoutResId = R.layout.fragment_options_filter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvGameTimeFilter.layoutManager = LinearLayoutManager(context)
        rvGameTimeFilter.adapter = adapter
        rvGameTimeFilter.isNestedScrollingEnabled = false


        observe(viewModel.playTimeFilterData) {
            adapter.submitList(it)
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.hiddenGamesCount) {
            tvHiddenGamesCount.text = it.toString()
            val enabled = it > 0
            btnClearHiddenGames.isEnabled = enabled
            tvHiddenGamesCount.isEnabled = enabled
            tvHiddenGamesLabel.isEnabled = enabled
        }

        btnClearHiddenGames.setOnClickListener {
            MessageDialogFragment.create(
                context!!,
                messageResId = R.string.dialog_message_reset_hidden_games,
                negativeResId = R.string.cancel
            ).show(childFragmentManager, null)
        }
    }

    override fun onDialogPositiveClick(dialog: MessageDialogFragment, tag: String?) {
        viewModel.onClearHiddenGames()
    }

    companion object {
        fun newInstance() =
            RouletteOptionsFragment()
    }
}