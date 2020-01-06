package ru.luckycactus.steamroulette.presentation.main

import dagger.BindsInstance
import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.FlowScope
import ru.luckycactus.steamroulette.presentation.menu.MenuViewModel
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteViewModel
import ru.luckycactus.steamroulette.presentation.roulette.options.PlaytimeViewModel
import ru.luckycactus.steamroulette.presentation.roulette.options.RouletteOptionsViewModel

@Subcomponent(modules = [MainFlowModule::class])
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