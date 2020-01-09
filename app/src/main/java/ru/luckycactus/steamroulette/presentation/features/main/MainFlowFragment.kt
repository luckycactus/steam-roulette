package ru.luckycactus.steamroulette.presentation.features.main

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.common.component
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.features.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.utils.*

class MainFlowFragment : BaseFragment(),
    ComponentOwner<MainFlowComponent> {

    val viewModel by viewModel { component.mainFlowViewModel }

    override val layoutResId = R.layout.fragment_main_flow

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        if (savedInstanceState == null) {
            viewModel.coldStart()
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

        observeEvent(viewModel.errorMessage) {
            container.showSnackbar(it) {
                anchorView = toolbar
            }
        }

        observeEvent(viewModel.logonCheckedAction) {
            childFragmentManager.beginTransaction()
                .add(R.id.container, RouletteFragment.newInstance())
                .commit()
        }
    }

    override fun createComponent(): MainFlowComponent =
        findComponent<MainActivityComponent>()
            .mainFlowComponentFactory()
            .create(this)


    companion object {
        fun newInstance() = MainFlowFragment()
        const val MENU_FRAGMENT_TAG = "menu_fragment_tag"
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
    }
}