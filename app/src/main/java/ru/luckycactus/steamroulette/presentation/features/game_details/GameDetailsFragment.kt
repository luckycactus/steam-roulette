package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ArcMotion
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.fragment_game_details.*
import kotlinx.android.synthetic.main.item_game_details_screenshots.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.games.entity.ScreenshotEntity
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.SpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.utils.*
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideApp
import javax.inject.Inject

class GameDetailsFragment : BaseFragment(), Injectable {

    lateinit var adapter: GameDetailsAdapter

    private val viewModel by viewModel {
        findComponent<MainActivityComponent>().gameDetailsViewModelFactory.create(
            requireArguments().getParcelable(ARG_GAME)!!
        )
    }

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val shouldPlaySharedElementTransition =
            requireArguments().getBoolean(ARG_ENABLE_TRANSITION) && savedInstanceState == null

        enterTransition = transitionSet {
            fade { }
        }

        if (shouldPlaySharedElementTransition) {
            sharedElementEnterTransition = transitionSet {
                changeBounds {
                    //setPathMotion(ArcMotion()) //todo
                }
                changeClipBounds { }
                changeTransform { }
            }
            postponeEnterTransition()
        }

        adapter = GameDetailsAdapter(
            shouldPlaySharedElementTransition,
            ::startPostponedEnterTransition,
            viewModel
        )

        rvGameDetails.adapter = adapter
        rvGameDetails.layoutManager = LinearLayoutManager(context)
        val margin = resources.getDimensionPixelSize(R.dimen.default_activity_margin)
        rvGameDetails.addItemDecoration(SpaceDecoration(margin, 0, margin))

        fabBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        observe(viewModel.gameDetails) {
            adapter.submitList(it)
        }

        observeEvent(viewModel.openUrlAction) {
            (activity as MainActivity).openUrl(it, true)
        }

        fabBack.doOnLayout {
            rvGameDetails.updatePadding(bottom = it.height + it.marginBottom)
        }
    }

    override fun inject() {
        findComponent<MainActivityComponent>().inject(this)
    }

    companion object {
        private const val ARG_GAME = "ARG_GAME"
        private const val ARG_ENABLE_TRANSITION = "ARG_ENABLE_SHARED_ELEMENT_TRANSITION"

        fun newInstance(game: OwnedGame, enableSharedElementTransition: Boolean) =
            GameDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_GAME, game)
                    putBoolean(ARG_ENABLE_TRANSITION, enableSharedElementTransition)
                }
            }

    }
}