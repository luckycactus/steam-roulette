package ru.luckycactus.steamroulette.presentation.roulette

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.utils.*
import ru.luckycactus.steamroulette.presentation.widget.DataLoadingViewHolder


class RouletteFragment : BaseFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(RouletteViewModel::class.java)) {
                    val mainFlowViewModel = (parentFragment as MainFlowFragment).viewModel
                    RouletteViewModel(mainFlowViewModel) as T
                } else {
                    throw IllegalArgumentException("ViewModel Not Found")
                }
            }
        }).get(RouletteViewModel::class.java)
    }

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    override val layoutResId: Int = R.layout.fragment_roulette

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnNextGame.setOnClickListener {
            viewModel.onNextGameClick()
        }

        btnHideAndNextGame.setOnClickListener {
            viewModel.onHideGameClick()
        }

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            gameRouletteLayout,
            viewModel::onRetryClick
        )

        observe(viewModel.currentGame) {
            gameView.setGame(it)
        }

        observe(viewModel.contentState) {
            when (it) {
                RouletteViewModel.ContentState.Loading -> dataLoadingViewHolder.showLoading()
                is RouletteViewModel.ContentState.Error -> dataLoadingViewHolder.showErrorWithButton(
                    msg = it.message
                )
                RouletteViewModel.ContentState.Loaded -> dataLoadingViewHolder.showContent()
            }
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}