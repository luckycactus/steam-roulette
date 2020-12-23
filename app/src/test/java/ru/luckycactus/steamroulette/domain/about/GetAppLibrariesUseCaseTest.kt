package ru.luckycactus.steamroulette.domain.about

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.luckycactus.steamroulette.data.repositories.about.AboutRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.about.datasource.LocalAboutDataSource
import ru.luckycactus.steamroulette.domain.core.usecase.invoke

@RunWith(AndroidJUnit4::class)
class GetAppLibrariesUseCaseTest {

    private lateinit var getAppLibrariesUseCase: GetAppLibrariesUseCase

    @Before
    fun setup() {
        val moshi = Moshi.Builder().build()
        val assetManager = ApplicationProvider.getApplicationContext<Application>().assets
        val aboutDataSource = LocalAboutDataSource(moshi, assetManager)
        val aboutRepository = AboutRepositoryImpl(aboutDataSource)
        getAppLibrariesUseCase = GetAppLibrariesUseCase(aboutRepository)
    }

    @Test
    fun `when invoke - should return not empty result`(): Unit = runBlocking {
        val libraries = getAppLibrariesUseCase.invoke()
        assertThat(libraries.isNotEmpty()).isTrue()
    }

}