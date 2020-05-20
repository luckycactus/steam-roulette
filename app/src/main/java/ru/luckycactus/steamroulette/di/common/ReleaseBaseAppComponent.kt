package ru.luckycactus.steamroulette.di.common

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        StorageModule::class,
        AppMigrationModule::class
    ]
)
interface ReleaseBaseAppComponent : BaseAppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): BaseAppComponent
    }
}