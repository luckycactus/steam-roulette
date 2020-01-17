package ru.luckycactus.steamroulette.presentation.features.main

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.component
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.showIfNotExist
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class MainFlowFragment : BaseFragment(),
    ComponentOwner<MainFlowComponent> {

    private val viewModel by viewModel { component.mainFlowViewModel }

    override val layoutResId = R.layout.fragment_main_flow

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        avatarContainer.setOnClickListener {
            childFragmentManager.showIfNotExist(MENU_FRAGMENT_TAG) {
                MenuFragment.newInstance()
            }
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this)
                .load(it.avatarFull)
                .placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }

        if (childFragmentManager.fragments.isEmpty()) {
            childFragmentManager.beginTransaction()
                .add(R.id.container, RouletteFragment.newInstance(), ROULETTE_FRAGMENT_TAG)
                .setReorderingAllowed(true)
                .commit()
        }
    }

    override fun createComponent(): MainFlowComponent =
        findComponent<MainActivityComponent>()
            .mainFlowComponentFactory()
            .create(this)


    companion object {
        fun newInstance() = MainFlowFragment()
        const val ROULETTE_FRAGMENT_TAG = "ROULETTE_FRAGMENT_TAG"
        const val MENU_FRAGMENT_TAG = "menu_fragment_tag"
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
    }
}