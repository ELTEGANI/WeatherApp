package com.example.weatherapp

import com.example.weatherapp.data.local.LocationProvider
import com.example.weatherapp.data.model.*
import com.example.weatherapp.data.remote.WeatherApiService
import com.example.weatherapp.data.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class WeatherRepositoryTest {

    @Test
    fun `getWeatherForecast returns failure when location is null`() = runTest {
        val apiService = mockk<WeatherApiService>()
        val locationProvider = mockk<LocationProvider>()
        coEvery { locationProvider.getCurrentLocation() } returns null

        val repository = WeatherRepository(apiService, locationProvider)

        val result = repository.getWeatherForecast()

        assertTrue(result.isFailure)
        assertEquals("Please enable location", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { apiService.getWeatherForecast(any(), any(), any()) }
    }

    @Test
    fun `getWeatherForecast returns failure when api throws`() = runTest {
        val apiService = mockk<WeatherApiService>()
        val locationProvider = mockk<LocationProvider>()
        coEvery { locationProvider.getCurrentLocation() } returns (24.0 to 54.0)
        coEvery { apiService.getWeatherForecast(any(), any(), any()) } throws RuntimeException("Boom")

        val repository = WeatherRepository(apiService, locationProvider)

        val result = repository.getWeatherForecast()

        assertTrue(result.isFailure)
        assertEquals("Boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getWeatherForecast success maps few clouds to SUNNY and converts kelvin to celsius`() = runTest {
        val apiService = mockk<WeatherApiService>()
        val locationProvider = mockk<LocationProvider>()

        coEvery { locationProvider.getCurrentLocation() } returns (24.0 to 54.0)

        val response = WeatherApiResponse(
            cod = "200",
            city = City(name = "X", coord = Coord(24.0, 54.0)),
            list = listOf(
                ForecastItem(
                    dt = 1703167200L, // any timestamp
                    dtTxt = "2025-12-21 12:00:00",
                    main = WeatherMain(
                        temp = 295.15,    // 22C
                        tempMin = 295.15,
                        tempMax = 295.15,
                        humidity = 50
                    ),
                    weather = listOf(
                        Weather(id = 801, main = "Clouds", description = "few clouds")
                    )
                )
            )
        )

        coEvery { apiService.getWeatherForecast(24.0, 54.0, any()) } returns response

        val repository = WeatherRepository(apiService, locationProvider)

        val result = repository.getWeatherForecast()

        assertTrue(result.isSuccess)
        val forecasts = result.getOrThrow()

        assertTrue(forecasts.isNotEmpty())
        assertEquals(WeatherType.SUNNY, forecasts.first().weatherType)
        assertEquals(22, forecasts.first().temperature)
    }
}