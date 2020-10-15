package ru.luckycactus.steamroulette.data.repositories.games_filter

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.games_filter.RouletteFilterRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RouletteFilterRepositoryImpl @Inject constructor(
    @Named("roulette-filters") prefs: SharedPreferences,
    moshi: Moshi,
    userSession: UserSession
) : RouletteFilterRepository, GamesFilterRepositoryImpl(prefs, moshi, userSession)