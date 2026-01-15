package com.example.weatherapp.data.repository

import com.example.weatherapp.data.local.LocationDataSource
import com.example.weatherapp.data.mapper.WeatherApiResponseMapper
import com.example.weatherapp.data.model.*
import com.example.weatherapp.data.remote.WeatherRemoteDataSource
import com.example.weatherapp.domain.model.Location
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test


class WeatherRepositoryImplTest {

    @Test
    fun `getWeatherForecast returns failure when location is null`() = runTest {
        val remoteDataSource = mockk<WeatherRemoteDataSource>()
        val locationDataSource = mockk<LocationDataSource>()
        val mapper = mockk<WeatherApiResponseMapper>()
        coEvery { locationDataSource.getCurrentLocation() } returns null

        val repository = WeatherRepositoryImpl(remoteDataSource, locationDataSource, mapper)

        val result = repository.getWeatherForecast()

        assertTrue(result.isFailure)
        assertEquals("Please enable location", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { remoteDataSource.getWeatherForecast(any(), any()) }
    }

    @Test
    fun `getWeatherForecast returns failure when remote data source throws`() = runTest {
        val remoteDataSource = mockk<WeatherRemoteDataSource>()
        val locationDataSource = mockk<LocationDataSource>()
        val mapper = mockk<WeatherApiResponseMapper>()
        val location = Location(latitude = 24.0, longitude = 54.0)
        
        coEvery { locationDataSource.getCurrentLocation() } returns location
        coEvery { remoteDataSource.getWeatherForecast(24.0, 54.0) } throws RuntimeException("Network error")

        val repository = WeatherRepositoryImpl(remoteDataSource, locationDataSource, mapper)

        val result = repository.getWeatherForecast()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getWeatherForecast success maps and returns forecasts`() = runTest {
        val remoteDataSource = mockk<WeatherRemoteDataSource>()
        val locationDataSource = mockk<LocationDataSource>()
        val mapper = mockk<WeatherApiResponseMapper>()
        val location = Location(latitude = 24.0, longitude = 54.0)

        val response = WeatherApiResponse(
            cod = "200",
            city = City(name = "Dubai", coord = Coord(24.0, 54.0)),
            list = listOf(
                ForecastItem(
                    dt = 1703167200L,
                    dtTxt = "2025-12-21 12:00:00",
                    main = WeatherMain(
                        temp = 295.15,
                        tempMin = 295.15,
                        tempMax = 295.15,
                        humidity = 50
                    ),
                    weather = listOf(
                        Weather(id = 800, main = "Clear", description = "clear sky")
                    )
                )
            )
        )

        val expectedForecasts = listOf(
            WeatherForecast(dayName = "Thursday", temperature = 22, weatherType = WeatherType.SUNNY)
        )

        coEvery { locationDataSource.getCurrentLocation() } returns location
        coEvery { remoteDataSource.getWeatherForecast(24.0, 54.0) } returns response
        every { mapper.mapToDomain(response.list) } returns expectedForecasts

        val repository = WeatherRepositoryImpl(remoteDataSource, locationDataSource, mapper)

        val result = repository.getWeatherForecast()

        assertTrue(result.isSuccess)
        assertEquals(expectedForecasts, result.getOrNull())
        coVerify(exactly = 1) { remoteDataSource.getWeatherForecast(24.0, 54.0) }
        coVerify(exactly = 1) { mapper.mapToDomain(response.list) }
    }
}
