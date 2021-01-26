package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import org.intellij.lang.annotations.Language
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import timber.log.Timber

interface GamesValidator {
    fun validate(game: OwnedGameEntity): Boolean
    fun log()

    interface Factory {
        fun create(previouslyVerifiedAppIds: Set<Int>): GamesValidator
    }
}

class GamesValidatorImpl(
    private val previouslyValidatedAppIds: Set<Int>,
    private val log: Boolean = BuildConfig.DEBUG
) : GamesValidator {
    private val logger = GamesParseLogger(log)

    override fun validate(game: OwnedGameEntity): Boolean {
        var valid = false
        //usually games have both iconUrl and logoUrl
        val suspicious = game.iconUrl.isNullOrEmpty() || game.logoUrl.isNullOrEmpty()
        if (previouslyValidatedAppIds.contains(game.appId)) {
            valid = true
        } else {
            valid = if (game.name == null) { //shouldn't be null or empty, added just for null-safety
                false
            } else {
                val ban = hasNoImages(game) ||
                        containsBannedWordsInName(game) ||
                        (suspicious && containsSuspiciousWordsInName(game))

                if (ban)
                    logger.addBannedGame(game)

                !ban
            }
        }

        if (log && valid && suspicious)
            logger.addSuspiciousGame(game)

        return valid
    }

    private fun hasNoImages(game: OwnedGameEntity) =
        game.iconUrl.isNullOrEmpty() && game.logoUrl.isNullOrEmpty()

    private fun containsBannedWordsInName(game: OwnedGameEntity) =
        banRegex.containsMatchIn(game.name!!)

    private fun containsSuspiciousWordsInName(game: OwnedGameEntity) =
        banIfSuspiciousRegex.containsMatchIn(game.name!!)

    override fun log() {
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
                Timber.v(
                    "Banned games (%d): %s",
                    bannedGames!!.size,
                    bannedGames.joinToString(separator = "\n")
                )
                Timber.v(
                    "Suspicious games (%d): %s",
                    suspiciousGames!!.size,
                    suspiciousGames.joinToString(separator = "\n")
                )
            }
        }
    }

    class Factory constructor(
        private val log: Boolean = BuildConfig.DEBUG
    ) : GamesValidator.Factory {
        override fun create(previouslyVerifiedAppIds: Set<Int>): GamesValidator {
            return GamesValidatorImpl(previouslyVerifiedAppIds, log)
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