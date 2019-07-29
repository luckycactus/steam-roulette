package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ValidateSteamIdInputUseCase : UseCase<String, Boolean>() {

    override fun getResult(params: String): Boolean {
        with(params.trim()) {
            return SteamId.getFormat(this) != SteamId.Format.Invalid
                    || SteamId.getVanityUrlFormat(this) != SteamId.VanityUrlFormat.Invalid
        }
    }
}