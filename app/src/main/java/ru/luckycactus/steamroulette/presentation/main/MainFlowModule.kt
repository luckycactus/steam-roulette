package ru.luckycactus.steamroulette.presentation.main

import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.utils.viewModel

@Module
abstract class MainFlowModule {

    @Module
    companion object {
        //todo
        @Provides
        @JvmStatic
        fun provideUserViewModelDelegate(fragment: MainFlowFragment): UserViewModelDelegate =
            fragment.viewModel
    }
}