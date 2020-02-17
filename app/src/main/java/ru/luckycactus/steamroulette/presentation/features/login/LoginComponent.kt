package ru.luckycactus.steamroulette.presentation.features.login

import dagger.Subcomponent
import ru.luckycactus.steamroulette.di.scopes.FeatureScope

@Subcomponent
@FeatureScope
interface LoginComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): LoginComponent
    }

    val loginViewModel: LoginViewModel
}