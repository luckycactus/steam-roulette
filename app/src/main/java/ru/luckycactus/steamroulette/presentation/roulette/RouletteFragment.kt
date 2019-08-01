package ru.luckycactus.steamroulette.presentation.roulette

import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_roulette.*
import kotlinx.android.synthetic.main.fullscreen_progress.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.widget.DataLoadingViewHolder

class RouletteFragment : BaseFragment() {

    private val viewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this).get(RouletteViewModel::class.java)
    }

    private lateinit var behavior: BottomSheetBehavior<*>

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    override val layoutResId: Int = R.layout.fragment_roulette

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        behavior = BottomSheetBehavior.from(view?.findViewById<ViewGroup>(R.id.menuSheet)!!)
        behavior.skipCollapsed = true
        behavior.isHideable = true
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        ivAvatar.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        btnNextGame.setOnClickListener {
            viewModel.onNextGameClick()
        }

        btnHideAndNextGame.setOnClickListener {
            viewModel.onHideGameClick()
        }

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            gameRouletteLayout,
            viewModel::onRetryClick
        )

        observe(viewModel.userSummary) {
            tvNickname.text = it.personaName
            Glide.with(this).load(it.avatarFull).placeholder(R.drawable.avatar_placeholder).into(ivAvatar)
        }

        observe(viewModel.errorState) {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        }

        observe(viewModel.currentGame) {
            gameView.setGame(it)
        }

        observe(viewModel.contentState) {
            when (it) {
                RouletteViewModel.ContentState.Loading -> dataLoadingViewHolder.showLoading()
                is RouletteViewModel.ContentState.Error -> dataLoadingViewHolder.showErrorWithButton(
                    msg = it.message
                )
                RouletteViewModel.ContentState.Loaded -> dataLoadingViewHolder.showContent()
            }
        }
    }

    companion object {
        fun newInstance() = RouletteFragment()
    }
}