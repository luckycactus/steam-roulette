package ru.luckycactus.steamroulette.di.common

import android.app.Application
import android.util.LruCache
import dagger.BindsInstance
import dagger.Component
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.login.LoginViewModel
import ru.luckycactus.steamroulette.presentation.main.MainActivityComponent
import ru.luckycactus.steamroulette.presentation.main.MainFlowViewModel
import ru.luckycactus.steamroulette.presentation.main.MainViewModel
import ru.luckycactus.steamroulette.presentation.menu.MenuViewModel
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.roulette.RouletteViewModel
import ru.luckycactus.steamroulette.presentation.roulette.options.RouletteOptionsViewModel
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class
    ]
)
interface AppComponent: BaseAppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent
    }
}