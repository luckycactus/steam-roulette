package ru.luckycactus.steamroulette.domain.games.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OwnedGame(
    val appId: Int,
    val name: String,
    val playtime2Weeks: Int,
    val playtimeForever: Int,
    val iconUrl: String,
    val logoUrl: String
) : Parcelable