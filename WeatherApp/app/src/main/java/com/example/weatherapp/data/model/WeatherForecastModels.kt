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
    val icon: String
)

data class WeatherUiState(
    val forecasts: List<WeatherForecast> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val weatherType: WeatherType = WeatherType.SUNNY
)