package ru.luckycactus.steamroulette.test.rules

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.ExternalResource

class RoomTransactionsMockRule: ExternalResource() {

    override fun before() {
        super.before()
        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
    }

    override fun after() {
        super.after()
        unmockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
    }

    fun mockTransactions(dbMock: RoomDatabase) {
        coEvery { dbMock.withTransaction(captureLambda<suspend () -> Any>()) } coAnswers {
            lambda<suspend () -> Any>().captured.invoke()
        }
    }
}