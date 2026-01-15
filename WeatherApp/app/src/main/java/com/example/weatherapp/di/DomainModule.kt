package com.example.weatherapp.di

import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.domain.usecase.GetWeatherForecastUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGetWeatherForecastUseCase(
        repository: WeatherRepository
    ): GetWeatherForecastUseCase {
        return GetWeatherForecastUseCase(repository)
    }
}
