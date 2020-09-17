package ru.luckycactus.steamroulette.presentation.features.main

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate

@Module
@InstallIn(ActivityComponent::class)
abstract class MainActivityModule {
    companion object {
        @Provides
        fun provideUserViewModelDelegate(activity: Activity): UserViewModelDelegate =
            (activity as MainActivity).viewModel
    }
}