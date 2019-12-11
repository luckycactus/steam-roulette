package ru.luckycactus.steamroulette.presentation.roulette

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.AutoInjectable
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.roulette.options.RouletteOptionsFragment
import ru.luckycactus.steamroulette.presentation.utils.*
import ru.luckycactus.steamroulette.presentation.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.widget.card_stack.CardStackLayoutManager
import ru.luckycactus.steamroulette.presentation.widget.card_stack.CardStackTouchHelperCallback
import ru.luckycactus.steamroulette.presentation.widget.touchhelper.ItemTouchHelper
import javax.inject.Inject
import kotlin.math.absoluteValue

class RouletteFragment : BaseFragment(), AutoInjectable {

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().rouletteViewModel
    }

    @Inject
    lateinit var rouletteAdapter: RouletteAdapter

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
                rouletteAdapter.notifyItemRemoved(0)
                viewModel.onAdapterUpdatedAfterSwipe()
            },
            onSwipedLeft = {
                viewModel.onGameSwiped(true)
                rouletteAdapter.notifyItemRemoved(0)
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

        observeEvent(viewModel.queueResetAction) {
            rouletteAdapter.items = null
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