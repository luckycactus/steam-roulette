package ru.luckycactus.steamroulette.di.common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import ru.luckycactus.steamroulette.domain.app.migrations.AppMigration
import ru.luckycactus.steamroulette.domain.app.migrations.AppMigration5to6

@Module
@InstallIn(ApplicationComponent::class)
interface AppMigrationModule {
    @Binds
    @IntoMap
    @IntKey(5)
    fun bindMigration5to6(migration5to6: AppMigration5to6): AppMigration
}