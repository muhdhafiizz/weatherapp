package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class ZippopotamusResponse(
    @SerializedName("post code") val postCode: String,
    val places: List<Place>
)

data class Place(
    @SerializedName("place name") val placeName: String,
    val state: String,
    @SerializedName("country abbreviation") val stateAbbreviation: String
)


data class WeatherResponse(val data: List<WeatherData>)

data class WeatherData(
    val city_name: String,
    val temp: Double,
    val rh:String,
    val wind_spd:String,
    val vis:Double,
    val weather: WeatherDetails
)
data class WeatherDetails(
    val icon: String,
    val description: String
)

data class ForecastWeatherResponse(
    val city_name: String,
    val data: List<ForecastDayData>
)

data class ForecastDayData(
    val valid_date: String,         // Date for the forecast
    val temp: Double,         // Temperature for that day
    val weather: WeatherDetails
)
