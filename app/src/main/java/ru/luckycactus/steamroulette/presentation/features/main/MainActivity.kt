package ru.luckycactus.steamroulette.presentation.features.main

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_toolbar.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.BaseAppComponent
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.di.core.component
import ru.luckycactus.steamroulette.di.qualifier.ForActivity
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.navigation.Screens
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.showSnackbar
import ru.luckycactus.steamroulette.presentation.utils.viewModel
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import javax.inject.Inject

class MainActivity : AppCompatActivity(), ComponentOwner<MainActivityComponent>, Injectable {
    private var sharedViews: List<View>? = null

    var touchAndBackPressEnabled: Boolean = true

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    val viewModel by viewModel { component.mainViewModel }

    private val navigator: Navigator =
        object : SupportAppNavigator(this, supportFragmentManager, R.id.container) {
            // "replace" changed to "hide" + "add"
            override fun fragmentForward(command: Forward) {
                val screen = command.screen as SupportAppScreen

                val fragmentParams = screen.fragmentParams
                val fragment = if (fragmentParams == null) createFragment(screen) else null

                val fragmentTransaction = fragmentManager.beginTransaction()

                val currentFragment = fragmentManager.findFragmentById(containerId)
                setupFragmentTransaction(
                    command,
                    currentFragment,
                    fragment,
                    fragmentTransaction
                )

                if (currentFragment != null) {
                    fragmentTransaction.hide(currentFragment)
                }

                if (fragmentParams != null) {
                    fragmentTransaction.add(
                        containerId,
                        fragmentParams.fragmentClass,
                        fragmentParams.arguments
                    )
                } else {
                    fragmentTransaction.add(containerId, fragment!!)
                }

                fragmentTransaction
                    .addToBackStack(screen.screenKey)
                    .commit()
                localStackCopy.add(screen.screenKey)
            }

            override fun setupFragmentTransaction(
                command: Command,
                currentFragment: Fragment?,
                nextFragment: Fragment?,
                fragmentTransaction: FragmentTransaction
            ) {
                fragmentTransaction.setReorderingAllowed(true)
                when (nextFragment) {
                    is LoginFragment -> {
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                    is GameDetailsFragment -> {
                        sharedViews?.forEach {
                            fragmentTransaction.addSharedElement(
                                it,
                                ViewCompat.getTransitionName(it)!!
                            )
                        }
                        sharedViews = null
                    }
                    is RouletteFragment -> {
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    }
                }
            }
        }

    override fun createComponent(): MainActivityComponent =
        InjectionManager.findComponent<BaseAppComponent>()
            .mainActivityComponentFactory()
            .create(this)

    override fun inject() {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            viewModel.onColdStart()
        }

        observeEvent(viewModel.errorMessage) {
            container.showSnackbar(it) {
                anchorView = toolbar
            }
        }
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
        return touchAndBackPressEnabled && super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (touchAndBackPressEnabled) {
            super.onBackPressed()
        }
    }

    fun onGameClick(sharedViews: List<View>, game: GameHeader) {
        this.sharedViews = sharedViews
        viewModel.onGameClick(game, sharedViews.isNotEmpty())
    }
}