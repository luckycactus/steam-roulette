package ru.luckycactus.steamroulette.presentation.features.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.github.terrakok.cicerone.*
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ActivityMainBinding
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.PlayUtils
import ru.luckycactus.steamroulette.presentation.utils.extensions.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.extensions.showSnackbar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MessageDialogFragment.Callbacks {

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var analytics: AnalyticsHelper

    val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private var runningTransitions = 0
    val touchSwitchTransitionListener = object : TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) {
            runningTransitions--
        }

        override fun onTransitionStart(transition: Transition) {
            runningTransitions++
        }
    }

    private val navigator = object : AppNavigator(this, R.id.container, supportFragmentManager) {

        override fun setupFragmentTransaction(
            screen: FragmentScreen,
            fragmentTransaction: FragmentTransaction,
            currentFragment: Fragment?,
            nextFragment: Fragment
        ) {
            fragmentTransaction.setReorderingAllowed(true)
            when (nextFragment) {
                is LoginFragment -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
                is RouletteFragment -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                }
            }
            if (currentFragment != null) {
                if (nextFragment is LoginFragment || nextFragment is RouletteFragment) {
                    fragmentTransaction.setCustomAnimations(
                        R.anim.fragment_fade_in,
                        R.anim.fragment_fade_out,
                        R.anim.fragment_fade_in,
                        R.anim.fragment_fade_out
                    )
                } else /*if (nextFragment !is GameDetailsFragment)*/ {
                    fragmentTransaction.setCustomAnimations(
                        R.anim.anim_fragment_enter,
                        R.anim.anim_fragment_exit,
                        R.anim.anim_fragment_pop_enter,
                        R.anim.anim_fragment_pop_exit
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeEvent(viewModel.errorMessage) {
            binding.container.showSnackbar(it, Snackbar.LENGTH_LONG)
        }

        observeEvent(viewModel.reviewRequest) {
            askForReview()
        }

        if (savedInstanceState == null) {
            viewModel.onColdStart()
        }
    }

    private fun askForReview() {
        MessageDialogFragment.create(
            this,
            titleResId = R.string.rate_app_title,
            messageResId = R.string.rate_app_dialog_message,
            positiveResId = R.string.rate,
            negativeResId = R.string.later,
            neutralResId = R.string.never
        ).show(supportFragmentManager, TAG_REVIEW_REQUEST)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return runningTransitions == 0 && super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (runningTransitions == 0) {
            super.onBackPressed()
        }
    }

    override fun onMessageDialogResult(
        dialog: MessageDialogFragment,
        result: MessageDialogFragment.Result
    ) {
        when (dialog.tag) {
            TAG_REVIEW_REQUEST -> when (result) {
                MessageDialogFragment.Result.Positive -> {
                    analytics.logSelectContent("Review request", "Accepted")
                    reviewApp()
                }
                MessageDialogFragment.Result.Neutral -> {
                    analytics.logSelectContent("Review request", "Disabled")
                    viewModel.disableAppReview()
                }
                MessageDialogFragment.Result.Negative -> {
                    analytics.logSelectContent("Review request", "Delayed")
                    viewModel.delayAppReview()
                }
                MessageDialogFragment.Result.Cancel -> {
                    analytics.logSelectContent("Review request", "Cancelled")
                }
            }
        }
    }

    fun onGameClick(
        game: GameHeader,
        color: Int = Color.TRANSPARENT
    ) {
        viewModel.onGameClick(game, color)
    }

    fun reviewApp() {
        lifecycleScope.launch {
            PlayUtils.reviewApp(this@MainActivity)
            viewModel.onAppReviewed()
        }
    }

    companion object {
        const val TAG_REVIEW_REQUEST = "TAG_REVIEW_REQUEST"
    }
}