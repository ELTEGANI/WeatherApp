package com.example.weatherapp.data.model


enum class WeatherType {
    SUNNY,
    CLOUDY,
    RAINY
}

data class WeatherForecast(
    val dayName: String,
    val temperature: Int,
    val weatherType: WeatherType,
)

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val forecasts: List<WeatherForecast>,
        val weatherType: WeatherType
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}