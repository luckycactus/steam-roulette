package ru.luckycactus.steamroulette.presentation.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper

abstract class BaseFragment<Binding : ViewBinding> : Fragment() {

    protected val analytics: AnalyticsHelper

    protected val binding: Binding get() = _binding!!
    private var _binding: Binding? = null

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
        _binding = inflateViewBinding(inflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): Binding

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
    @InstallIn(SingletonComponent::class)
    interface BaseFragmentEntryPoint {
        fun analytics(): AnalyticsHelper
    }
}
