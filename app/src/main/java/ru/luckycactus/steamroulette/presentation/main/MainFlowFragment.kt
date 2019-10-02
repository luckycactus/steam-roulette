package ru.luckycactus.steamroulette.presentation.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.menu.MenuFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFilterFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.observeNonNull

class MainFlowFragment : BaseFragment() {

    val viewModel by lazy {
        ViewModelProviders.of(this).get(MainFlowViewModel::class.java)
    }

    override val layoutResId = R.layout.fragment_main_flow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        observeNonNull(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }

        observeEvent(viewModel.errorMessage) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        observeEvent(viewModel.logonCheckedAction) {
            childFragmentManager.beginTransaction()
                .add(R.id.container, RouletteFragment.newInstance())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roulette, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_roulette_options -> {
                RouletteFilterFragment.newInstance().show(
                    childFragmentManager,
                    FILTER_FRAGMENT_TAG
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance() = MainFlowFragment()
        const val MENU_FRAGMENT_TAG = "menu_fragment_tag"
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
    }
}