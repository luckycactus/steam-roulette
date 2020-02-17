package ru.luckycactus.steamroulette.presentation.features.main

import androidx.lifecycle.*
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.features.user.UserViewModelDelegatePublic
import javax.inject.Inject

//todo Отдавать Result из UseCase?
class MainFlowViewModel @Inject constructor(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel(), UserViewModelDelegatePublic by userViewModelDelegate
