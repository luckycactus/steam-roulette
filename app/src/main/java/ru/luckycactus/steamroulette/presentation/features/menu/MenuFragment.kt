package ru.luckycactus.steamroulette.presentation.features.menu

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
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
            ).show(childFragmentManager, CONFIRM_EXIT_DIALOG)
        }

        btnRefreshProfile.setOnClickListener {
            viewModel.refreshProfile()
        }

        tvAbout.setOnClickListener {
            viewModel.onAboutClick()
        }

        tvLibrary.setOnClickListener {
            viewModel.onLibraryClick()
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            GlideApp.with(this)
                .load(it.avatarFull)
                .placeholder(R.drawable.avatar_placeholder)
                .signature(ObjectKey(viewModel.userSummaryLastSync))
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
                resources.getString(
                    R.string.you_have_n_games,
                    resources.getQuantityString(R.plurals.games_count_plurals, it, it)
                )
        }

        observe(viewModel.gamesLastUpdate) {
            tvGamesUpdateDate.text = it
        }
    }

    override fun onMessageDialogResult(
        dialog: MessageDialogFragment,
        result: MessageDialogFragment.Result
    ) {
        when (dialog.tag) {
            CONFIRM_EXIT_DIALOG -> {
                if (result == MessageDialogFragment.Result.Positive)
                    viewModel.logout()
            }
        }
    }

    companion object {
        private const val CONFIRM_EXIT_DIALOG = "CONFIRM_DIALOG"
        fun newInstance() = MenuFragment()
    }
}