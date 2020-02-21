package ru.luckycactus.steamroulette.presentation.features.main

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.di.scopes.ActivityScope
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

@AssistedModule
@Module(includes = [AssistedInject_MainActivityModule::class])
abstract class MainActivityModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideUserViewModelDelegate(activity: MainActivity): UserViewModelDelegate =
            activity.viewModel

        @JvmStatic
        @ActivityScope
        @Provides
        fun provideCicerone(): Cicerone<Router> = Cicerone.create()

        @JvmStatic
        @Provides
        fun provideNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.navigatorHolder

        @JvmStatic
        @Provides
        fun provideGlobalRouter(cicerone: Cicerone<Router>): Router = cicerone.router
    }
}