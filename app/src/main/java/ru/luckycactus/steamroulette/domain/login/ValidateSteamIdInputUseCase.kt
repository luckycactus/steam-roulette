package ru.luckycactus.steamroulette.domain.login

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import javax.inject.Inject

@Reusable
class ValidateSteamIdInputUseCase @Inject constructor() : UseCase<String, Boolean>() {

    override fun getResult(params: String): Boolean {
        with(params.trim()) {
            return SteamId.getFormat(this) != SteamId.Format.Invalid
                    || SteamId.getVanityUrlFormat(this) != SteamId.VanityUrlFormat.Invalid
        }
    }
}