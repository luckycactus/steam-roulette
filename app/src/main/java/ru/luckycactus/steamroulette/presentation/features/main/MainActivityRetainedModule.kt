package ru.luckycactus.steamroulette.presentation.features.main

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class MainActivityRetainedModule {
    companion object {
        @ActivityRetainedScoped
        @Provides
        fun provideCicerone(): Cicerone<Router> = Cicerone.create()

        @Provides
        fun provideNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.navigatorHolder

        @Provides
        fun provideGlobalRouter(cicerone: Cicerone<Router>): Router = cicerone.router
    }
}