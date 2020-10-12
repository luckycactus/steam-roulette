package ru.luckycactus.steamroulette.data.repositories.games_filter

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RouletteFilterRepository @Inject constructor(
    @Named("roulette-filters") prefs: SharedPreferences,
    moshi: Moshi,
    userSession: UserSession
) : GamesFilterRepositoryImpl(prefs, moshi, userSession)