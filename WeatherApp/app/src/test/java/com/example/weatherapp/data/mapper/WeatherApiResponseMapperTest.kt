package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.data.model.WeatherMain
import com.example.weatherapp.domain.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Test


class WeatherApiResponseMapperTest {

    private val mapper = WeatherApiResponseMapper()

    @Test
    fun `mapToDomain converts ForecastItem to WeatherForecast correctly`() {
        val forecasts = listOf(
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

        val result = mapper.mapToDomain(forecasts)

        assertEquals(1, result.size)
        assertEquals(22, result.first().temperature)
        assertEquals(WeatherType.SUNNY, result.first().weatherType)
    }

    @Test
    fun `mapToDomain converts few clouds to SUNNY`() {
        val forecasts = listOf(
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
                    Weather(id = 801, main = "Clouds", description = "few clouds")
                )
            )
        )

        val result = mapper.mapToDomain(forecasts)

        assertEquals(WeatherType.SUNNY, result.first().weatherType)
    }

    @Test
    fun `mapToDomain converts scattered clouds to CLOUDY`() {
        val forecasts = listOf(
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
                    Weather(id = 802, main = "Clouds", description = "scattered clouds")
                )
            )
        )

        val result = mapper.mapToDomain(forecasts)

        assertEquals(WeatherType.CLOUDY, result.first().weatherType)
    }

    @Test
    fun `mapToDomain converts rain to RAINY`() {
        val forecasts = listOf(
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
                    Weather(id = 500, main = "Rain", description = "light rain")
                )
            )
        )

        val result = mapper.mapToDomain(forecasts)

        assertEquals(WeatherType.RAINY, result.first().weatherType)
    }

    @Test
    fun `mapToDomain groups forecasts by day and takes max temperature`() {
        val forecasts = listOf(
            ForecastItem(
                dt = 1703167200L, // Same day
                dtTxt = "2025-12-21 09:00:00",
                main = WeatherMain(temp = 290.15, tempMin = 290.15, tempMax = 290.15, humidity = 50),
                weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky"))
            ),
            ForecastItem(
                dt = 1703178000L, // Same day but higher temp
                dtTxt = "2025-12-21 12:00:00",
                main = WeatherMain(temp = 295.15, tempMin = 295.15, tempMax = 295.15, humidity = 50),
                weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky"))
            ),
            ForecastItem(
                dt = 1703253600L, // Different day
                dtTxt = "2025-12-22 09:00:00",
                main = WeatherMain(temp = 293.15, tempMin = 293.15, tempMax = 293.15, humidity = 50),
                weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky"))
            )
        )

        val result = mapper.mapToDomain(forecasts)

        assertEquals(2, result.size)
        assertEquals(22, result.first().temperature) // Max temp from first day
        assertEquals(20, result[1].temperature)
    }

    @Test
    fun `mapToDomain limits results to 5 forecasts`() {
        val forecasts = (1..10).map { day ->
            ForecastItem(
                dt = 1703167200L + (day * 86400L),
                dtTxt = "2025-12-${21 + day} 12:00:00",
                main = WeatherMain(temp = 295.15, tempMin = 295.15, tempMax = 295.15, humidity = 50),
                weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky"))
            )
        }

        val result = mapper.mapToDomain(forecasts)

        assertEquals(5, result.size)
    }
}
