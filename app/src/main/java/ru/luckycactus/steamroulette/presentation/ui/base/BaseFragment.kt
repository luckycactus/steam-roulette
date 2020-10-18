package ru.luckycactus.steamroulette.presentation.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper

abstract class BaseFragment : Fragment() {
    abstract val layoutResId: Int

    protected val analytics: AnalyticsHelper

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            App.getInstance(),
            BaseFragmentEntryPoint::class.java
        )
        analytics = entryPoint.analytics()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    open val logScreenName: String? = this::class.simpleName

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
    @InstallIn(ApplicationComponent::class)
    interface BaseFragmentEntryPoint {
        fun analytics(): AnalyticsHelper
    }
}
