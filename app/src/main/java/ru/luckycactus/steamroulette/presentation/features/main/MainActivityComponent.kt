package ru.luckycactus.steamroulette.presentation.features.main

import dagger.BindsInstance
import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.ActivityScope
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.hidden_games.HiddenGamesViewModel
import ru.luckycactus.steamroulette.presentation.features.login.LoginComponent
import ru.luckycactus.steamroulette.presentation.features.menu.MenuViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette_options.PlaytimeViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsViewModel

@ActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: MainActivity): MainActivityComponent
    }

    fun loginComponentFactory(): LoginComponent.Factory

    val mainViewModel: MainViewModel
    val gameDetailsViewModelFactory: GameDetailsViewModel.Factory
    val menuViewModel: MenuViewModel
    val rouletteViewModel: RouletteViewModel
    val rouletteOptionsViewModel: RouletteOptionsViewModel
    val playtimeDialogViewModel: PlaytimeViewModel
    val hiddenGamesViewModel: HiddenGamesViewModel

    fun inject(gameDetailsFragment: GameDetailsFragment)
    fun inject(mainActivity: MainActivity)
}