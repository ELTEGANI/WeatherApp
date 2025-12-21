package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.local.LocationProvider
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.remote.WeatherApiService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val locationProvider: LocationProvider,
) {
    suspend fun getWeatherForecast(): Result<List<WeatherForecast>> {
        return try {
            val location = locationProvider.getCurrentLocation()
                ?: return Result.failure(Exception("Please enable location"))

            val response = apiService.getWeatherForecast(
                lat = location.first,
                lon = location.second,
                appId = BuildConfig.WEATHER_API_KEY
            )
            val dailyForecasts = groupForecastsByDay(response.list)
            val forecasts = dailyForecasts.map { item ->
                convertToWeatherForecast(item)
            }

            Result.success(forecasts.take(5))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    }}