package ru.luckycactus.steamroulette.data.local

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import javax.inject.Inject

@Reusable
class AndroidResourceManager @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceManager {

    override fun getString(@StringRes strId: Int): String =
        context.getString(strId)

    override fun getString(@StringRes strId: Int, vararg formatArgs: Any): String =
        context.getString(strId, *formatArgs)

    override fun getQuantityString(@PluralsRes strId: Int, num: Int): String =
        context.resources.getQuantityString(strId, num)

    override fun getQuantityString(@PluralsRes strId: Int, num: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(strId, num, *formatArgs)
}