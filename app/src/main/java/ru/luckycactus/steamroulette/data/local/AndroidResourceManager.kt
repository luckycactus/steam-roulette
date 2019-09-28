package ru.luckycactus.steamroulette.data.local

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import ru.luckycactus.steamroulette.domain.common.ResourceManager

class AndroidResourceManager(
    private val context: Context
) : ResourceManager {

    override fun getString(@StringRes strId: Int): String =
        context.getString(strId)

    override fun getString(@StringRes strId: Int, vararg formatArgs: Any) =
        context.getString(strId, *formatArgs)

    override fun getQuantityString(@PluralsRes strId: Int, num: Int): String =
        context.resources.getQuantityString(strId, num)
}