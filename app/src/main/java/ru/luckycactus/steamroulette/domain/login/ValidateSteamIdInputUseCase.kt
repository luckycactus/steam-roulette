package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

class ValidateSteamIdInputUseCase @Inject constructor() {

    operator fun invoke(params: String): Boolean {
        val input = params.trim()
        return SteamId.getFormat(input) != SteamId.Format.Invalid
                || SteamId.getVanityUrlFormat(input) != SteamId.VanityUrlFormat.Invalid
    }
}