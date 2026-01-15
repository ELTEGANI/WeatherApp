package com.example.weatherapp.data.repository

import com.example.weatherapp.data.local.LocationDataSource
import com.example.weatherapp.data.mapper.WeatherApiResponseMapper
import com.example.weatherapp.data.remote.WeatherRemoteDataSource
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject


class WeatherRepositoryImpl @Inject constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val locationDataSource: LocationDataSource,
    private val mapper: WeatherApiResponseMapper
) : WeatherRepository {

    override suspend fun getWeatherForecast(): Result<List<WeatherForecast>> {
        return try {
            val location = locationDataSource.getCurrentLocation()
                ?: return Result.failure(Exception("Please enable location"))

            val response = remoteDataSource.getWeatherForecast(
                latitude = location.latitude,
                longitude = location.longitude
            )

            val forecasts = mapper.mapToDomain(response.list)

            Result.success(forecasts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
