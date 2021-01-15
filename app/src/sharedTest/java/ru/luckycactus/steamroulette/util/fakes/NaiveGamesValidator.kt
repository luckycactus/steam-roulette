package ru.luckycactus.steamroulette.util.fakes

import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesValidator
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity

class NaiveGamesValidator : GamesValidator {
    override fun validate(game: OwnedGameEntity): Boolean {
        return true
    }

    override fun log() {
        //nothing
    }

    class Factory :
        GamesValidator.Factory {
        override fun create(previouslyVerifiedAppIds: Set<Int>): GamesValidator {
            return NaiveGamesValidator()
        }

    }
}