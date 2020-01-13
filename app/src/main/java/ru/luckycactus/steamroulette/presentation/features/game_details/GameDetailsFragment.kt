package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_game_details.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.di.core.Injectable
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.game_details.adapter.GameDetailsAdapter
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsItemUiModel
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import javax.inject.Inject

class GameDetailsFragment : BaseFragment(), Injectable {
    @Inject
    lateinit var gameDetailsAdapter: GameDetailsAdapter

    override val layoutResId: Int = R.layout.fragment_game_details

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvGameDetails.adapter = gameDetailsAdapter
        rvGameDetails.layoutManager = LinearLayoutManager(context)

        val game = arguments!!.getParcelable<OwnedGame>(ARG_GAME)!!
        val details = listOf<GameDetailsItemUiModel>(GameDetailsItemUiModel.Header(game))
        gameDetailsAdapter.setItems(details)
    }

    override fun inject() {
        findComponent<MainActivityComponent>().inject(this)
    }

    companion object {
        private const val ARG_GAME = "ARG_GAME"

        fun newInstance(game: OwnedGame) = GameDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_GAME, game)
            }
        }

    }
}