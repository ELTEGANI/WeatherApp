package com.example.weatherapp.data.model


import com.squareup.moshi.Json


data class WeatherApiResponse(
    @param:Json(name = "cod") val cod: String,
    @param:Json(name = "list") val list: List<ForecastItem>,
    @param:Json(name = "city") val city: City
)

data class ForecastItem(
    @param:Json(name = "dt") val dt: Long,
    @param:Json(name = "main") val main: WeatherMain,
    @param:Json(name = "weather") val weather: List<Weather>,
    @param:Json(name = "dt_txt") val dtTxt: String
)

data class WeatherMain(
    @param:Json(name = "temp") val temp: Double,
    @param:Json(name = "temp_min") val tempMin: Double,
    @param:Json(name = "temp_max") val tempMax: Double,
    @param:Json(name = "humidity") val humidity: Int
)

data class Weather(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "main") val main: String,
    @param:Json(name = "description") val description: String,
)

data class City(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "coord") val coord: Coord
)

data class Coord(
    @param:Json(name = "lat") val lat: Double,
    @param:Json(name = "lon") val lon: Double
)