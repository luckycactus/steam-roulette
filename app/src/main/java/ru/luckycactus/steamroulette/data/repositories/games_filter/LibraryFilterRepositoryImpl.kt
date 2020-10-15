package ru.luckycactus.steamroulette.data.repositories.games_filter

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.games_filter.LibraryFilterRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

@Reusable
class LibraryFilterRepositoryImpl @Inject constructor(
    @Named("library-filters") prefs: SharedPreferences,
    moshi: Moshi,
    userSession: UserSession
) : LibraryFilterRepository, GamesFilterRepositoryImpl(prefs, moshi, userSession)