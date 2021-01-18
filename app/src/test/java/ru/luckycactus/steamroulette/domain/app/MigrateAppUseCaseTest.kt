package ru.luckycactus.steamroulette.domain.app

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import ru.luckycactus.steamroulette.domain.app.migrations.AppMigration
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import javax.inject.Provider

class MigrateAppUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var appRepositoryMock: AppRepository

    @MockK
    private lateinit var migrationsProviderMock: Provider<Map<Int, @JvmSuppressWildcards Provider<AppMigration>>>

    private lateinit var migrateApp: MigrateAppUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        migrateApp = MigrateAppUseCase(appRepositoryMock, migrationsProviderMock)
    }

    @Test
    fun `when invoke - while first time - should only set last version to current`() =
        runBlocking {
            coEvery { appRepositoryMock getProperty "currentVersion" } returns 3
            coEvery { appRepositoryMock getProperty "lastVersion" } returns 0

            migrateApp()

            coVerify(exactly = 1) { appRepositoryMock setProperty "lastVersion" value 3 }
            coVerify(exactly = 0) { migrationsProviderMock.get() }
        }
}