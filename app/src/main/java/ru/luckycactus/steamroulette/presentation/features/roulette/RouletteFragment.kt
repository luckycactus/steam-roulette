package ru.luckycactus.steamroulette.presentation.features.roulette

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.transition.*
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import kotlinx.coroutines.delay
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.features.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.features.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.utils.*
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.ui.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper
import javax.inject.Inject

class RouletteFragment : BaseFragment(), Injectable {

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().rouletteViewModel
    }

    @Inject
    lateinit var rouletteAdapterFactory: RouletteAdapter.Factory

    private val rouletteAdapter: RouletteAdapter by lazyNonThreadSafe {
        rouletteAdapterFactory.create { sharedViews, game ->
            if (sharedViews.isNotEmpty()) {
                requireParentFragment().exitTransition = transitionSet {
                    fade { }
                    excludeTarget(rvRoulette, true)
                    onTransitionEnd {
                        parentFragment?.exitTransition = null
                    }
                }

                requireParentFragment().reenterTransition = transitionSet {
                    fade {
                        excludeTarget(rvRoulette, true)
                    }
                    //delay cardstack appear
                    fade {
                        startDelay = 375 //todo
                        duration = 0
                        addTarget(rvRoulette)
                    }
                    onTransitionEnd {
                        parentFragment?.reenterTransition = null
                    }
                }
            } else {
                requireParentFragment().exitTransition = transitionSet {
                    fade { }
                }
                requireParentFragment().reenterTransition = transitionSet {
                    fade { }
                }
            }
            (activity as MainActivity).onGameClick(sharedViews, game)
        }
    }

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    //private var fabsAlphaRecoveryAnimator: Animator? = null

    private lateinit var itemTouchHelper: ItemTouchHelper

    override val layoutResId: Int = R.layout.fragment_roulette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun inject() {
        findComponent<MainFlowComponent>().inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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


        itemTouchHelper = ItemTouchHelper(CardStackTouchHelperCallback(
            onSwiped = {
                //animateFabsAlphaRecovery()
            },
            onSwipedRight = {
                viewModel.onGameSwiped(false)
                //rouletteAdapter.notifyItemRemoved(0)
                viewModel.onAdapterUpdatedAfterSwipe()
            },
            onSwipedLeft = {
                viewModel.onGameSwiped(true)
                //rouletteAdapter.notifyItemRemoved(0)
                viewModel.onAdapterUpdatedAfterSwipe()
            },
            onSwipeProgress = { progress, _ ->
                //                fabsAlphaRecoveryAnimator?.let {
//                    it.cancel()
//                    fabsAlphaRecoveryAnimator = null
//                }
//                val fraction = 0.8f * progress
//                fabNextGame.alpha = 1f + fraction
//                fabHideGame.alpha = 1f - fraction
//                fabSteamInfo.alpha = 1f - fraction.absoluteValue
                viewModel.onSwipeProgress(progress)
            }
        ), 1.5f)
        itemTouchHelper.attachToRecyclerView(rvRoulette)
        rvRoulette.layoutManager =
            CardStackLayoutManager()
        rvRoulette.adapter = rouletteAdapter

        fabNextGame.setOnClickListener {
            swipeTop(ItemTouchHelper.RIGHT)
        }

        fabHideGame.setOnClickListener {
            swipeTop(ItemTouchHelper.LEFT)
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

        observe(
            viewModel.itemRemoved
        ) {
            it?.ifNotHandled {
                rouletteAdapter.notifyItemRemoved(it)
            }
        }

        observe(
            viewModel.itemsInserted
        ) {
            it?.ifNotHandled {
                rouletteAdapter.notifyItemRangeInserted(it.first, it.second)
            }
        }

        observe(viewModel.games) {
            rouletteAdapter.items = it
        }

        observe(viewModel.contentState) {
            dataLoadingViewHolder.showContentState(it)
        }

        observe(viewModel.controlsAvailable) {
            fabNextGame.isEnabled = it
            fabHideGame.isEnabled = it
            fabSteamInfo.isEnabled = it
        }

        observeEvent(viewModel.openUrlAction) {
            //todo into navigation
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
                childFragmentManager.showIfNotExist(MainFlowFragment.FILTER_FRAGMENT_TAG) {
                    RouletteOptionsFragment.newInstance()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    private fun animateFabsAlphaRecovery() {
//        fabsAlphaRecoveryAnimator = AnimatorSet().apply {
//            play(ObjectAnimator.ofFloat(fabNextGame, View.ALPHA, 1f))
//                .with(ObjectAnimator.ofFloat(fabHideGame, View.ALPHA, 1f))
//                .with(ObjectAnimator.ofFloat(fabSteamInfo, View.ALPHA, 1f))
//            setDuration(200L)
//                .start()
//        }
//    }

    private fun swipeTop(direction: Int) {
        rvRoulette.findViewHolderForAdapterPosition(0)?.let {
            itemTouchHelper.swipe(it, direction)
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}