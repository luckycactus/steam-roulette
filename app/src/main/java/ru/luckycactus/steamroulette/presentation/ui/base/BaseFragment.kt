package ru.luckycactus.steamroulette.presentation.ui.base

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ru.luckycactus.steamroulette.domain.analytics.Analytics
import ru.luckycactus.steamroulette.presentation.common.App

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int): super(contentLayoutId)

    protected val analytics: Analytics

    open val logScreenName: String? = this::class.simpleName

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            App.getInstance(),
            BaseFragmentEntryPoint::class.java
        )
        analytics = entryPoint.analytics()
    }

    override fun onResume() {
        super.onResume()
        logScreen()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        logScreen()
    }

    private fun logScreen() {
        if (isResumed
            && isAdded
            && !isHidden
            && view?.visibility == View.VISIBLE
        ) {
            analytics.trackScreen(logScreenName)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BaseFragmentEntryPoint {
        fun analytics(): Analytics
    }
}
