package ru.luckycactus.steamroulette.domain.user

data class UserSummary(
    val steamId: SteamId,
    val personaName: String,
    val communityVisibleState: CommunityVisibleState,
    val profileState: Boolean,
    val lastLogoff: Long,
    val profileUrl: String,
    val avatar: String,
    val avatarMedium: String,
    val avatarFull: String,
    val personaState: PersonaState
)

enum class CommunityVisibleState(
    private val value: Int
) {
    Private(1),
    FriendsOnly(2),
    Public(3);

    companion object {
        val map = values().associateBy { it.value }

        @JvmStatic
        fun fromInt(value: Int): CommunityVisibleState =
            map[value]
                ?: throw IllegalArgumentException("Illegal value of ${CommunityVisibleState::class.java.simpleName}")
    }
}

enum class PersonaState(
    private val value: Int
) {
    Offline(0),
    Online(1),
    Busy(2),
    Away(3),
    Snooze(4),
    lookingToTrade(5),
    lookingToPlay(6);

    companion object {
        val map = values().associateBy { it.value }

        @JvmStatic
        fun fromInt(value: Int): PersonaState =
            map[value]
                ?: throw java.lang.IllegalArgumentException("Illegal value of ${PersonaState::class.java.simpleName}")
    }
}

