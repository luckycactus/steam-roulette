package ru.luckycactus.steamroulette.domain.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface ResourceManager {
    fun getString(@StringRes strId: Int): String
    fun getQuantityString(@PluralsRes strId: Int, num: Int): String
    fun getString(@StringRes strId: Int, vararg formatArgs: Any): String
}