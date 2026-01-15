package com.example.weatherapp.presentation.viewmodel

import MainDispatcherRule
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType
import com.example.weatherapp.domain.usecase.GetWeatherForecastUseCase
import com.example.weatherapp.presentation.model.WeatherUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadWeatherForecast success sets forecasts and background weatherType from first item`() = runTest {
        val useCase = mockk<GetWeatherForecastUseCase>()
        val forecasts = listOf(
            WeatherForecast(dayName = "Monday", temperature = 22, weatherType = WeatherType.RAINY),
            WeatherForecast(dayName = "Tuesday", temperature = 23, weatherType = WeatherType.SUNNY),
        )
        coEvery { useCase() } returns Result.success(forecasts)

        val viewModel = WeatherViewModel(useCase)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        val successState = state as WeatherUiState.Success
        assertEquals(forecasts, successState.forecasts)
        assertEquals(WeatherType.RAINY, successState.weatherType)
    }

    @Test
    fun `loadWeatherForecast failure sets error and stops loading`() = runTest {
        val useCase = mockk<GetWeatherForecastUseCase>()
        coEvery { useCase() } returns Result.failure(Exception("Network error"))

        val viewModel = WeatherViewModel(useCase)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is WeatherUiState.Error)
        val errorState = state as WeatherUiState.Error
        assertEquals("Network error", errorState.message)
    }

    @Test
    fun `loadWeatherForecast success with empty list keeps default SUNNY weatherType`() = runTest {
        val useCase = mockk<GetWeatherForecastUseCase>()
        coEvery { useCase() } returns Result.success(emptyList())

        val viewModel = WeatherViewModel(useCase)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        val successState = state as WeatherUiState.Success
        assertEquals(emptyList<WeatherForecast>(), successState.forecasts)
        assertEquals(WeatherType.SUNNY, successState.weatherType)
    }
}
