package com.example.weatherapp.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.model.WeatherUiState
import com.example.weatherapp.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeatherForecast()
    }

    fun loadWeatherForecast() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getWeatherForecast()
                .onSuccess { forecasts ->
                    val dominantWeatherType = determineDominantWeatherType(forecasts)
                    _uiState.value = WeatherUiState(
                        forecasts = forecasts,
                        isLoading = false,
                        weatherType = dominantWeatherType
                    )
                }
                .onFailure { error ->
                    _uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    private fun determineDominantWeatherType(forecasts: List<WeatherForecast>): WeatherType {
        val weatherTypeCounts = forecasts.groupingBy { it.weatherType }.eachCount()
        return weatherTypeCounts.maxByOrNull { it.value }?.key ?: WeatherType.SUNNY
    }
}