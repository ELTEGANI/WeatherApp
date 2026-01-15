package com.example.weatherapp.data.remote

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.model.WeatherApiResponse
import javax.inject.Inject


class WeatherRemoteDataSource @Inject constructor(
    private val apiService: WeatherApiService
) {
    suspend fun getWeatherForecast(latitude: Double, longitude: Double): WeatherApiResponse {
        return apiService.getWeatherForecast(
            lat = latitude,
            lon = longitude,
            appId = BuildConfig.WEATHER_API_KEY
        )
    }
}
