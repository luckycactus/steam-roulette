package ru.luckycactus.steamroulette.domain.analytics

import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.login.LoginUseCase

object Events {

    fun ReviewRequestResult(option: ReviewRequestOption): SelectContentEvent {
        return SelectContentEvent("Review request", option.itemId)
    }

    fun SwipeGame(hide: Boolean): SelectContentEvent {
        return SelectContentEvent("Game swipe", if (hide) "left" else "right")
    }

    fun Click(button: String): SelectContentEvent {
        return SelectContentEvent("button", button)
    }

    fun AttemptLogin(result: LoginUseCase.Result) : SelectContentEvent {
        val loginMethod =
            if (result.steamIdFormat != null && result.steamIdFormat != SteamId.Format.Invalid) {
                result.steamIdFormat.name
            } else if (result.vanityUrlFormat != null && result.vanityUrlFormat != SteamId.VanityUrlFormat.Invalid) {
                "Vanity: ${result.vanityUrlFormat}"
            } else {
                "error"
            }

        return SelectContentEvent(
            "Login attempt",
            loginMethod,
            params = mapOf(
                "result" to if (result is LoginUseCase.Result.Success) "success" else result::class.simpleName!!
            )
        )
    }

    enum class ReviewRequestOption(val itemId: String) {
        ACCEPT("Accepted"),
        DISABLE("Disabled"),
        DELAY("Delayed"),
        CANCEL("Cancelled")
    }
}