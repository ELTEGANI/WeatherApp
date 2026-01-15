package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.WeatherForecast


interface WeatherRepository {
    suspend fun getWeatherForecast(): Result<List<WeatherForecast>>
}
