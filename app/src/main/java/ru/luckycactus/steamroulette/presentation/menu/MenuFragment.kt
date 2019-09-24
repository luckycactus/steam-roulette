package ru.luckycactus.steamroulette.presentation.menu

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.ivAvatar
import kotlinx.android.synthetic.main.fragment_menu.tvNickname
import kotlinx.android.synthetic.main.fragment_roulette.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.main.MainActivity
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteViewModel
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeNonNull

class MenuFragment : BaseBottomSheetDialogFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this, object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                    val mainFlowViewModel = (parentFragment as MainFlowFragment).viewModel
                    MenuViewModel(mainFlowViewModel) as T
                } else {
                    throw IllegalArgumentException("ViewModel Not Found")
                }
            }

        }).get(MenuViewModel::class.java)
    }

    override val layoutResId: Int = R.layout.fragment_menu

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnExit.setOnClickListener {
            (activity as MainActivity).viewModel.onExit()
        }

        btnRefreshProfile.setOnClickListener {
            viewModel.refreshUserSummary()
        }

        observeNonNull(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }
    }

    companion object {
        fun newInstance() = MenuFragment()
    }
}