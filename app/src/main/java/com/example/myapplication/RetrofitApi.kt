package com.example.myapplication

import android.telecom.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZippopotamusApiService {
    @GET("{country}/{postcode}")
    suspend fun getCityByPostcode(
        @Path("country") country: String,
        @Path("postcode") postcode: String
    ): ZippopotamusResponse
}

interface WeatherService {
    @GET("current")
    fun getCurrentWeather(
        @Query("city") city: String,
        @Query("key") apiKey: String
    ): retrofit2.Call<WeatherResponse>
}

interface WeatherForecastService {
    @GET("forecast/daily")
    fun get3DayForecast(
        @Query("city") city: String,
        @Query("key") apiKey: String,
        @Query("days") days: Int = 3
    ): retrofit2.Call<ForecastWeatherResponse>
}

