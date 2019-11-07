package ru.luckycactus.steamroulette.presentation.login

import dagger.Subcomponent

@Subcomponent
interface LoginComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): LoginComponent
    }

    val loginViewModel: LoginViewModel
}