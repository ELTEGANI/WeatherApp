package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject


class GetWeatherForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(): Result<List<WeatherForecast>> {
        return repository.getWeatherForecast()
    }
}
