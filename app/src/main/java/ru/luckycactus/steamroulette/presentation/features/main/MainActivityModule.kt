package ru.luckycactus.steamroulette.presentation.features.main

import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate

@Module
abstract class MainActivityModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideUserViewModelDelegate(activity: MainActivity): UserViewModelDelegate =
            activity.viewModel
    }
}