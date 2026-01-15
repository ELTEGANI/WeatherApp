package com.example.weatherapp.di

import android.content.Context
import com.example.weatherapp.data.local.LocationDataSource
import com.example.weatherapp.data.mapper.WeatherApiResponseMapper
import com.example.weatherapp.data.remote.WeatherApiService
import com.example.weatherapp.data.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLocationDataSource(
        @ApplicationContext context: Context
    ): LocationDataSource {
        return LocationDataSource(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRemoteDataSource(
        apiService: WeatherApiService
    ): WeatherRemoteDataSource {
        return WeatherRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideWeatherApiResponseMapper(): WeatherApiResponseMapper {
        return WeatherApiResponseMapper()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        remoteDataSource: WeatherRemoteDataSource,
        locationDataSource: LocationDataSource,
        mapper: WeatherApiResponseMapper
    ): WeatherRepository {
        return WeatherRepositoryImpl(remoteDataSource, locationDataSource, mapper)
    }
}
