package ru.luckycactus.steamroulette.presentation.roulette

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.common.ContentState
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.roulette.options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fabNextGame.setOnClickListener {
            viewModel.onNextGameClick()
        }

        fabHideGame.setOnClickListener {
            viewModel.onHideGameClick()
        }

        fabSteamInfo.setOnClickListener {
            viewModel.onSteamInfoClick()
        }

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            content,
            viewModel::onRetryClick
        )

        observe(viewModel.currentGame) {
            gameView.setGame(it)
        }

        observe(viewModel.contentState) {
            dataLoadingViewHolder.showContentState(it)
        }

        observe(viewModel.controlsAvailable) {
            fabNextGame.isClickable = it
            fabHideGame.isClickable = it
            fabSteamInfo.isClickable = it
        }

        observeEvent(viewModel.openUrlAction) {
            //todo into navigation
            //todo customtabs
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            if (isAppInstalled(context!!, "com.valvesoftware.android.steam.community")) {
                with(intent) {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    `package` = "com.valvesoftware.android.steam.community"
                }
            }
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roulette, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_roulette_options -> {
                RouletteOptionsFragment.newInstance().show(
                    childFragmentManager,
                    MainFlowFragment.FILTER_FRAGMENT_TAG
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}