package ru.luckycactus.steamroulette.data.local

import androidx.room.Database
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.model.OwnedGamesEntity
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

//@Database(
//    entities = [OwnedGameEntity::class],
//    version = 1
//)
abstract class SteamRouletteDB {

    abstract fun ownedGamesDao(): OwnedGamesDao
}

abstract class OwnedGamesDao {


}