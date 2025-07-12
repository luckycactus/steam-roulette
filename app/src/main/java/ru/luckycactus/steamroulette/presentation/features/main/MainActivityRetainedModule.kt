package ru.luckycactus.steamroulette.presentation.features.main

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import com.github.terrakok.cicerone.Router

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class MainActivityRetainedModule {
    companion object {
        @ActivityRetainedScoped
        @Provides
        fun provideCicerone(): Cicerone<Router> = Cicerone.create()

        @Provides
        fun provideNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.getNavigatorHolder()

        @Provides
        fun provideGlobalRouter(cicerone: Cicerone<Router>): Router = cicerone.router
    }
}
