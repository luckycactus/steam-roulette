package ru.luckycactus.steamroulette.domain.about

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.repositories.about.AboutRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.about.data_store.LocalAboutDataStore
import ru.luckycactus.steamroulette.domain.about.GetAppLibrariesUseCase
import ru.luckycactus.steamroulette.domain.core.invoke

@RunWith(AndroidJUnit4::class)
class GetAppLibrariesUseCaseTest {

    private lateinit var getAppLibrariesUseCase: GetAppLibrariesUseCase

    @Before
    fun setUp() {
        val moshi = Moshi.Builder().build()
        val assetManager = ApplicationProvider.getApplicationContext<Application>().assets
        val aboutDataStore = LocalAboutDataStore(moshi, assetManager)
        val aboutRepository = AboutRepositoryImpl(aboutDataStore)
        getAppLibrariesUseCase = GetAppLibrariesUseCase(aboutRepository)
    }

    @Test
    fun `should return not empty libraries list`() = runBlocking {
        val libraries = getAppLibrariesUseCase.invoke()
        assertTrue(libraries.isNotEmpty())
    }

}