package ru.luckycactus.steamroulette.presentation.menu

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.ivAvatar
import kotlinx.android.synthetic.main.fragment_menu.tvNickname
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.main.MainActivity
import ru.luckycactus.steamroulette.presentation.main.MainFlowFragment
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.visibility
import ru.luckycactus.steamroulette.presentation.widget.MessageDialogFragment

class MenuFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {

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
            MessageDialogFragment.create(
                context!!,
                titleResId = R.string.exit_dialog_title,
                messageResId = R.string.exit_warning,
                negativeResId = R.string.cancel
            ).show(childFragmentManager, null)
        }

        btnRefreshProfile.setOnClickListener {
            viewModel.refreshProfile()
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder)
                .into(ivAvatar)
        }

        observe(viewModel.refreshProfileState) {
            btnRefreshProfile.isEnabled = !it
            btnRefreshProfile.visibility(!it)
            profileRefreshProgressBar.visibility(it)
        }

        observe(viewModel.gameCount) {
            tvGamesCount.text =
                resources.getQuantityString(R.plurals.account_games_count_plurals, it, it)
        }

        observe(viewModel.gamesLastUpdate) {
            tvGamesUpdateDate.text = it
        }
    }

    override fun onDialogPositiveClick(dialog: MessageDialogFragment, tag: String?) {
        (activity as MainActivity).viewModel.onExit()
    }

    companion object {
        fun newInstance() = MenuFragment()
    }
}