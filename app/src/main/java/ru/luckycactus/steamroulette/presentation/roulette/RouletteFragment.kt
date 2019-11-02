package ru.luckycactus.steamroulette.presentation.roulette

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.roulette.options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.widget.GameView

class RouletteFragment : BaseFragment() {

    //todo di
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

    private val gameCoverLoader = AppModule.glideGameCoverLoader

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

        //todo Заменить на popupwindow
        val fabLongClickListener = View.OnLongClickListener {
            val text = getString(
                when (it) {
                    fabNextGame -> R.string.next_game
                    fabHideGame -> R.string.hide_game
                    fabSteamInfo -> R.string.open_game_store_page
                    else -> 0
                }
            )
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            true
        }

        fabNextGame.setOnLongClickListener(fabLongClickListener)
        fabHideGame.setOnLongClickListener(fabLongClickListener)
        fabSteamInfo.setOnLongClickListener(fabLongClickListener)

        viewSwitcher.getChildAt(0).setLayerType(View.LAYER_TYPE_HARDWARE, null)
        viewSwitcher.getChildAt(1).setLayerType(View.LAYER_TYPE_HARDWARE, null)

        viewSwitcher.inAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                (viewSwitcher.nextView as GameView).setGame(viewModel.nextGame, gameCoverLoader)
            }
        })

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            content,
            viewModel::onRetryClick
        )

        observeEvent(viewModel.queueResetAction) {
            viewSwitcher.reset()
        }

        observe(viewModel.currentGame) {
            (viewSwitcher.nextView as GameView).setGame(it, gameCoverLoader)
            viewSwitcher.showNext()
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
                if (childFragmentManager.findFragmentByTag(MainFlowFragment.FILTER_FRAGMENT_TAG) == null)
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