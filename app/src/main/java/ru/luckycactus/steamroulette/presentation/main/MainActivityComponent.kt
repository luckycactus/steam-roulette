package ru.luckycactus.steamroulette.presentation.main

import dagger.BindsInstance
import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.ActivityScope
import ru.luckycactus.steamroulette.presentation.login.LoginComponent

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
}