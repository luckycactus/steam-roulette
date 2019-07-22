package ru.luckycactus.steamroulette.domain

import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.user.SteamId

class ValidateSteamIdInputUseCase : UseCase<String, Boolean>() {

    override fun getResult(params: String): Boolean {
        with(params.trim()) {
            return SteamId.getFormat(this) != SteamId.Format.Invalid
                    || SteamId.getVanityUrlFormat(this) != SteamId.VanityUrlFormat.Invalid
        }
    }
}