package com.example.weatherapp.presentation.model

import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType


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
