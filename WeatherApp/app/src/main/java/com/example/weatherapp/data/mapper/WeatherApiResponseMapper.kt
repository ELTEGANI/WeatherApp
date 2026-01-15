package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.model.WeatherType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class WeatherApiResponseMapper @Inject constructor() {

    fun mapToDomain(forecasts: List<ForecastItem>): List<WeatherForecast> {
        val dailyForecasts = groupForecastsByDay(forecasts)
        return dailyForecasts.map { item ->
            convertToWeatherForecast(item)
        }.take(5)
    }

    private fun groupForecastsByDay(forecasts: List<ForecastItem>): List<ForecastItem> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val grouped = forecasts.groupBy { item ->
            dateFormat.format(Date(item.dt * 1000))
        }

        return grouped.values.mapNotNull { dayForecasts ->
            dayForecasts.maxByOrNull { it.main.temp }
        }.sortedBy { it.dt }.take(5)
    }

    private fun convertToWeatherForecast(item: ForecastItem): WeatherForecast {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = item.dt * 1000
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)

        val temperature = item.main.temp.toInt() - 273
        val weatherMain = item.weather.firstOrNull()?.main ?: ""
        val weatherDescription = item.weather.firstOrNull()?.description ?: ""
        val weatherType = determineWeatherType(weatherMain, weatherDescription)

        return WeatherForecast(
            dayName = dayName,
            temperature = temperature,
            weatherType = weatherType,
        )
    }

    private fun determineWeatherType(main: String, description: String? = null): WeatherType {
        val mainLower = main.lowercase().trim()
        val descLower = description?.lowercase()?.trim() ?: ""

        return when (mainLower) {
            "clear" -> WeatherType.SUNNY

            "clouds" -> {
                when {
                    descLower.contains("few") -> WeatherType.SUNNY
                    descLower.contains("scattered") -> WeatherType.CLOUDY
                    descLower.contains("broken") -> WeatherType.CLOUDY
                    descLower.contains("overcast") -> WeatherType.CLOUDY
                    else -> WeatherType.CLOUDY
                }
            }

            "rain", "drizzle", "thunderstorm" -> WeatherType.RAINY

            else -> WeatherType.SUNNY
        }
    }
}
