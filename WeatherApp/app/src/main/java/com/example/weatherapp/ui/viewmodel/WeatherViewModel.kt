package com.example.weatherapp.ui.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.model.WeatherUiState
import com.example.weatherapp.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadWeatherForecast(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            if (_uiState.value is WeatherUiState.Success) return
            if (loadJob?.isActive == true) return
        } else {
            loadJob?.cancel()
        }

        loadJob = viewModelScope.launch {
            _uiState.update { WeatherUiState.Loading }

            repository.getWeatherForecast()
                .onSuccess { forecasts ->
                    val todayWeatherType = forecasts.firstOrNull()?.weatherType ?: WeatherType.SUNNY

                    _uiState.update {
                        WeatherUiState.Success(
                            forecasts = forecasts,
                            weatherType = todayWeatherType
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        WeatherUiState.Error(
                            message = error.message ?: "Unknown error occurred"
                        )
                    }
                }
        }
    }
}