package com.phoenix.companionforcodblackops7.core.di

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.core.data.local.entity.TableMetadata
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRealmConfiguration(): RealmConfiguration {
        return RealmConfiguration.Builder(
            schema = setOf(
                DynamicEntity::class,
                TableMetadata::class
            )
        )
            .name("bo7_companion.realm")
            .schemaVersion(1)
            .build()
    }

    @Provides
    @Singleton
    fun provideRealm(configuration: RealmConfiguration): Realm {
        return Realm.open(configuration)
    }
}
