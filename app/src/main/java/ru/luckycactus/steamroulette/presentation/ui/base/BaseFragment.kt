package ru.luckycactus.steamroulette.presentation.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int): super(contentLayoutId)

    protected val analytics: AnalyticsHelper

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
        logScreenName?.let {
            analytics.logScreenIfVisibleAndResumed(this, it)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BaseFragmentEntryPoint {
        fun analytics(): AnalyticsHelper
    }
}
