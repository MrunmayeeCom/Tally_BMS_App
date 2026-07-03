package com.bmstally.app.di

import com.bmstally.app.data.MockDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMockDataService(): MockDataService {
        MockDataService.seed()
        return MockDataService
    }
}
