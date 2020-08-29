package ru.luckycactus.steamroulette.presentation.features.menu

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_menu.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.setDrawableColorFromAttribute
import ru.luckycactus.steamroulette.presentation.utils.visibility

@AndroidEntryPoint
class MenuFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {

    private val viewModel: MenuViewModel by viewModels()

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
                .transition(DrawableTransitionOptions.withCrossFade())
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