package ru.luckycactus.steamroulette.data.repositories.user.datasource

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class LocalUserDataSource @Inject constructor(
    private val db: AppDatabase
) : UserDataSource.Local {

    override suspend fun getSummary(steamId: SteamId): UserSummaryEntity =
        db.userSummaryDao().get(steamId.as64())

    override suspend fun saveSummary(userSummary: UserSummaryEntity) {
        db.userSummaryDao().upsert(userSummary)
    }

    override suspend fun removeSummary(steamId: SteamId) {
        db.userSummaryDao().delete(steamId.as64())
    }

    override fun observeSummary(steamId: SteamId): Flow<UserSummaryEntity> =
        db.userSummaryDao().observe(steamId.as64()).distinctUntilChanged().filterNotNull()

    companion object {
        const val CURRENT_USER_KEY = "signed_user_key"
        const val CURRENT_USER_DEFAULT_VALUE = 0L
    }
}