package com.example.weatherapp

import MainDispatcherRule
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.model.WeatherUiState
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
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
    fun `init success sets forecasts and background weatherType from first item`() = runTest {
        val repository = mockk<WeatherRepository>()
        val forecasts = listOf(
            WeatherForecast(dayName = "Monday", temperature = 22, weatherType = WeatherType.RAINY),
            WeatherForecast(dayName = "Tuesday", temperature = 23, weatherType = WeatherType.SUNNY),
        )
        coEvery { repository.getWeatherForecast() } returns Result.success(forecasts)

        val viewModel = WeatherViewModel(repository)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        val successState = state as WeatherUiState.Success
        assertEquals(forecasts, successState.forecasts)
        assertEquals(WeatherType.RAINY, successState.weatherType)
    }

    @Test
    fun `init failure sets error and stops loading`() = runTest {
        val repository = mockk<WeatherRepository>()
        coEvery { repository.getWeatherForecast() } returns Result.failure(Exception("Network error"))

        val viewModel = WeatherViewModel(repository)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is WeatherUiState.Error)
        val errorState = state as WeatherUiState.Error
        assertEquals("Network error", errorState.message)
    }

    @Test
    fun `init success with empty list keeps default SUNNY weatherType`() = runTest {
        val repository = mockk<WeatherRepository>()
        coEvery { repository.getWeatherForecast() } returns Result.success(emptyList())

        val viewModel = WeatherViewModel(repository)
        viewModel.loadWeatherForecast()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        val successState = state as WeatherUiState.Success
        assertEquals(emptyList<WeatherForecast>(), successState.forecasts)
        assertEquals(WeatherType.SUNNY, successState.weatherType)
    }
}