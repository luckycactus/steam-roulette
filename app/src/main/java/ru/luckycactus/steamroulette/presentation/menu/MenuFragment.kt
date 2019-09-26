package ru.luckycactus.steamroulette.presentation.menu

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.ivAvatar
import kotlinx.android.synthetic.main.fragment_menu.tvNickname
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.main.MainActivity
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.observeNonNull
import ru.luckycactus.steamroulette.presentation.utils.visibility

class MenuFragment : BaseBottomSheetDialogFragment() {

    //private var refreshUserStateAnimator: Animator? = null


    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
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
            viewModel.refreshProfile()
        }

        observeNonNull(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }

        observe(viewModel.refreshUserSummaryState) {
            btnRefreshProfile.isEnabled = !it
            btnRefreshProfile.visibility(!it)
            profileRefreshProgressBar.visibility(it)
//            if (it && refreshUserStateAnimator?.isRunning != true) {
//                refreshUserStateAnimator =
//                    ObjectAnimator.ofFloat(btnRefreshProfile, View.ROTATION, 0f, 360f).apply {
//                        duration = 1500
//                        repeatCount = ValueAnimator.INFINITE
//                        interpolator = LinearInterpolator()
//                        start()
//                    }
//            } else if (!it && refreshUserStateAnimator?.isRunning == true) {
//                refreshUserStateAnimator?.end()
//                refreshUserStateAnimator = null
//            }
        }

        observe(viewModel.gameCount) {
            tvGamesCount.text = getString(R.string.account_games_count).format(it)
        }
    }

    companion object {
        fun newInstance() = MenuFragment()
    }
}