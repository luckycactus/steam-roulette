package ru.luckycactus.steamroulette.test.util.fakes

import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesVerifier
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity

class NaiveGamesVerifier : GamesVerifier {
    override fun verify(game: OwnedGameEntity): Boolean {
        return true
    }

    override fun log() {
        //nothing
    }

    class Factory :
        GamesVerifier.Factory {
        override fun create(previouslyVerifiedAppIds: Set<Int>): GamesVerifier {
            return NaiveGamesVerifier()
        }

    }
}