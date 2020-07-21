package ru.luckycactus.steamroulette.presentation.features.menu

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_menu.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.core.findComponent
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColorFromAttribute
import ru.luckycactus.steamroulette.presentation.utils.viewModel
import ru.luckycactus.steamroulette.presentation.utils.visibility

class MenuFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().menuViewModel
    }

    override val layoutResId: Int = R.layout.fragment_menu

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listOf(tvExit, tvAbout).forEach {
            it.setDrawableColorFromAttribute(R.attr.colorOnBackground)
        }

        tvExit.setOnClickListener {
            MessageDialogFragment.create(
                requireContext(),
                titleResId = R.string.exit_dialog_title,
                messageResId = R.string.exit_warning,
                negativeResId = R.string.cancel
            ).show(childFragmentManager, null)
        }

        btnRefreshProfile.setOnClickListener {
            viewModel.refreshProfile()
        }

        tvAbout.setOnClickListener {
            viewModel.onAboutClick()
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            GlideApp.with(this)
                .load(it.avatarFull)
                .placeholder(R.drawable.avatar_placeholder)
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
        viewModel.logout()
    }

    companion object {
        fun newInstance() = MenuFragment()
    }
}