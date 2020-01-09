package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class ObserveCurrentUserSteamIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<Unit?, LiveData<SteamId?>>() {

    override fun getResult(params: Unit?): LiveData<SteamId?> {
        return userRepository.observeCurrentUserSteamId()
    }
}