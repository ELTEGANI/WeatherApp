package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType
import com.example.weatherapp.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class GetWeatherForecastUseCaseTest {

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val repository = mockk<WeatherRepository>()
        val forecasts = listOf(
            WeatherForecast(dayName = "Monday", temperature = 22, weatherType = WeatherType.SUNNY),
            WeatherForecast(dayName = "Tuesday", temperature = 23, weatherType = WeatherType.CLOUDY)
        )
        coEvery { repository.getWeatherForecast() } returns Result.success(forecasts)

        val useCase = GetWeatherForecastUseCase(repository)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(forecasts, result.getOrNull())
        coVerify(exactly = 1) { repository.getWeatherForecast() }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val repository = mockk<WeatherRepository>()
        val error = Exception("Network error")
        coEvery { repository.getWeatherForecast() } returns Result.failure(error)

        val useCase = GetWeatherForecastUseCase(repository)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { repository.getWeatherForecast() }
    }
}
