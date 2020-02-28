package ru.luckycactus.steamroulette.data.repositories.games.datastore

import org.intellij.lang.annotations.Language
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.presentation.utils.longLog
import ru.luckycactus.steamroulette.presentation.utils.onDebug

class GamesVerifier(
    private val previouslyVerifiedAppIds: Set<Int>,
    log: Boolean
) {
    private val logger = GamesParseLogger(log)

    fun verify(game: OwnedGameEntity): Boolean {
        val suspicious = game.iconUrl.isNullOrEmpty() || game.logoUrl.isNullOrEmpty()
        if (previouslyVerifiedAppIds.contains(game.appId)) {
            onDebug {
                if (suspicious)
                    logger.addSuspiciousGame(game)
            }
            return true
        } else {
            var banned = game.iconUrl.isNullOrEmpty() && game.logoUrl.isNullOrEmpty()
            if (!banned && !game.name.isNullOrEmpty()) {
                banned = banRegex.containsMatchIn(game.name)

                if (!banned && suspicious) {
                    banned = banIfSuspiciousRegex.containsMatchIn(game.name)
                }
            }
            if (banned) {
                logger.addBannedGame(game)
            } else if (suspicious) {
                logger.addSuspiciousGame(game)
            }
            return !banned
        }
    }

    fun log() {
        logger.log()
    }

    private class GamesParseLogger(
        private val enable: Boolean = BuildConfig.DEBUG
    ) {
        private val bannedGames = if (enable) {
            mutableListOf<String>()
        } else null
        private val suspiciousGames = if (enable) {
            mutableListOf<String>()
        } else null

        fun addBannedGame(game: OwnedGameEntity) {
            bannedGames?.add(game.name!!)
        }

        fun addSuspiciousGame(game: OwnedGameEntity) {
            suspiciousGames?.add(game.name!!)
        }

        fun log() {
            if (enable) {
                longLog(
                    "GamesParseLogger",
                    "Banned games (${bannedGames!!.size}): ${bannedGames.joinToString(separator = "\n")}"
                )
                longLog(
                    "GamesParseLogger",
                    "Suspicious games (${suspiciousGames!!.size}): ${suspiciousGames.joinToString(
                        separator = "\n"
                    )}"
                )
            }
        }
    }

    companion object {
        @Language("RegExp")
        private val banRegex = arrayOf(
            "public test$",
            "public testing$",
            "closed test$",
            "system test$",
            "test server$",
            "testlive client$",
            "screen tests$",
            " demo$",
            " beta$",
            "\\(Theatrical",
            "\\(Subtitled",
            "PTS"
        ).joinToString(separator = "|")
            .toRegex(RegexOption.IGNORE_CASE)

        @Language("RegExp")
        private val banIfSuspiciousRegex =
            arrayOf(
                "\\btest$",
                "\\bEp\\d\\d",
                "Player Profiles",
                "\\bSkin\\b",
                "\\(Class\\)"

            ).joinToString(separator = "|")
                .toRegex(RegexOption.IGNORE_CASE)
    }
}