package ru.luckycactus.steamroulette.data

import android.content.Context
import ru.luckycactus.steamroulette.domain.common.ResourceManager

class AndroidResourceManager(
    private val context: Context
) : ResourceManager {

    override fun getString(strId: Int): String =
        context.getString(strId)
}