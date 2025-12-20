package com.example.weatherapp.di

import com.example.weatherapp.data.local.LocationProvider
import com.example.weatherapp.data.remote.WeatherApiService
import android.content.Context
import com.example.weatherapp.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideLocationProvider(@ApplicationContext context: Context): LocationProvider {
        return LocationProvider(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        apiService: WeatherApiService,
        locationProvider: LocationProvider,
        apiKey: String
    ): WeatherRepository {
        return WeatherRepository(apiService, locationProvider, apiKey)
    }
}