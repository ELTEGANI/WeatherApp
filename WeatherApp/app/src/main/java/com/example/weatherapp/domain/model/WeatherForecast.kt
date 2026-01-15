package com.example.weatherapp.domain.model


data class WeatherForecast(
    val dayName: String,
    val temperature: Int,
    val weatherType: WeatherType,
)
