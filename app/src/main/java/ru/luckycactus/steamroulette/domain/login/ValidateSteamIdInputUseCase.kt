package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

class ValidateSteamIdInputUseCase @Inject constructor() : UseCase<String, Boolean>() {

    override fun execute(params: String): Boolean {
        val input = params.trim()
        return SteamId.getFormat(input) != SteamId.Format.Invalid
                || SteamId.getVanityUrlFormat(input) != SteamId.VanityUrlFormat.Invalid
    }
}