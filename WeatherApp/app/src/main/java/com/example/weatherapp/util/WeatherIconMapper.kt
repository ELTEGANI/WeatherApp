package com.example.weatherapp.util


import androidx.annotation.DrawableRes
import com.example.weatherapp.R
import com.example.weatherapp.domain.model.WeatherType

object WeatherIconMapper {
    @DrawableRes
    fun getWeatherIconResId(weatherType: WeatherType): Int {
        return when (weatherType) {
            WeatherType.SUNNY -> R.drawable.property_1_01_sun_light
            WeatherType.RAINY -> R.drawable.property_1_20_rain_light
            WeatherType.CLOUDY -> R.drawable.property_1_15_cloud_light
        }
    }
}