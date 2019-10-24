package ru.luckycactus.steamroulette.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.showSnackbar

class MainFlowFragment : BaseFragment() {

    val viewModel by lazy {
        ViewModelProviders.of(this).get(MainFlowViewModel::class.java)
    }

    override val layoutResId = R.layout.fragment_main_flow


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        if (savedInstanceState == null) {
            viewModel.coldStart()
        }

        avatarContainer.setOnClickListener {
            //todo можно открыть 2 раза
            MenuFragment.newInstance().show(
                childFragmentManager,
                MENU_FRAGMENT_TAG
            )
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder)
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

    companion object {
        fun newInstance() = MainFlowFragment()
        const val MENU_FRAGMENT_TAG = "menu_fragment_tag"
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
    }
}