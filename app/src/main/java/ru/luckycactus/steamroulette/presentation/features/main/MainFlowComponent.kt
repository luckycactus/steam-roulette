package ru.luckycactus.steamroulette.presentation.features.main

import dagger.BindsInstance
import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.FlowScope
import ru.luckycactus.steamroulette.presentation.features.menu.MenuViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette_options.PlaytimeViewModel
import ru.luckycactus.steamroulette.presentation.features.roulette_options.RouletteOptionsViewModel

@Subcomponent
@FlowScope
interface MainFlowComponent {
    @Subcomponent.Factory
    interface Factory {

        fun create(@BindsInstance mainFlowFragment: MainFlowFragment): MainFlowComponent
    }

    fun inject(rouletteFragment: RouletteFragment)

    val mainFlowViewModel: MainFlowViewModel

    val menuViewModel: MenuViewModel

    val rouletteViewModel: RouletteViewModel

    val rouletteOptionsViewModel: RouletteOptionsViewModel

    val playtimeDialogViewModel: PlaytimeViewModel
}