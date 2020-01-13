package ru.luckycactus.steamroulette.presentation.features.main

import dagger.BindsInstance
import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.ActivityScope
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginComponent

@ActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: MainActivity): MainActivityComponent
    }

    fun mainFlowComponentFactory(): MainFlowComponent.Factory

    fun loginComponentFactory(): LoginComponent.Factory

    val mainViewModel: MainViewModel

    fun inject(gameDetailsFragment: GameDetailsFragment)
}