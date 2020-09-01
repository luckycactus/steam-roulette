package ru.luckycactus.steamroulette.presentation.features.main

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

@Module
@InstallIn(ActivityComponent::class)
abstract class MainActivityModule {
    companion object {
        @Provides
        fun provideUserViewModelDelegate(activity: Activity): UserViewModelDelegate =
            (activity as MainActivity).viewModel
    }
}