package ru.luckycactus.steamroulette.data.local.db

import androidx.room.*


abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: List<T>): List<Long>

    @Update
    abstract suspend fun update(obj: T): Int

    @Update
    abstract suspend fun update(obj: List<T>): Int

    @Delete
    abstract suspend fun delete(obj: T): Int

    @Transaction
    open suspend fun upsert(obj: T) {
        val id = insert(obj)
        if (id < 0L) {
            update(obj)
        }
    }

    @Transaction
    open suspend fun upsert(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] < 0L) {
                updateList.add(objList[i])
            }
        }

        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}