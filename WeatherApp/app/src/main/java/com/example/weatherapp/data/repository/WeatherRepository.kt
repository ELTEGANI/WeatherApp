package com.example.weatherapp.data.repository

import com.example.weatherapp.data.local.LocationProvider
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.remote.WeatherApiService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class WeatherRepository(
    private val apiService: WeatherApiService,
    private val locationProvider: LocationProvider,
    private val apiKey: String
) {
    suspend fun getWeatherForecast(): Result<List<WeatherForecast>> {
        return try {
            val location = locationProvider.getCurrentLocation()
                ?: return Result.failure(Exception("Location not available"))

            val response = apiService.getWeatherForecast(
                lat = location.first,
                lon = location.second,
                appId = apiKey
            )

            // Group forecasts by date and get one per day (preferably midday)
            val dailyForecasts = groupForecastsByDay(response.list)
            val forecasts = dailyForecasts.map { item ->
                convertToWeatherForecast(item)
            }

            Result.success(forecasts.take(5)) // Take only 5 days
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun groupForecastsByDay(forecasts: List<ForecastItem>): List<ForecastItem> {
        val grouped = forecasts.groupBy { item ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(item.dt * 1000)
            )
        }

        // Get the forecast closest to 12:00 PM (midday) for each day
        return grouped.values.mapNotNull { dayForecasts ->
            dayForecasts.minByOrNull { item ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = item.dt * 1000
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                kotlin.math.abs(hour - 12) // Closest to 12 PM
            }
        }
    }

    private fun convertToWeatherForecast(item: ForecastItem): WeatherForecast {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = item.dt * 1000
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)

        val temperature = item.main.temp.toInt() - 273 // Convert Kelvin to Celsius
        val weatherType = determineWeatherType(item.weather.firstOrNull()?.main ?: "")
        val icon = item.weather.firstOrNull()?.icon ?: ""

        return WeatherForecast(
            dayName = dayName,
            temperature = temperature,
            weatherType = weatherType,
            icon = icon
        )
    }

    private fun determineWeatherType(main: String): WeatherType {
        return when (main.lowercase()) {
            "clear" -> WeatherType.SUNNY
            "clouds" -> WeatherType.CLOUDY
            "rain", "drizzle", "thunderstorm" -> WeatherType.RAINY
            else -> WeatherType.CLOUDY // Default
        }
    }
}