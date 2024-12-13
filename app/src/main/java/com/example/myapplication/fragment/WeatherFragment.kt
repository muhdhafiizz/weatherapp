package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myapplication.ForecastDayData
import com.example.myapplication.ForecastWeatherResponse
import com.example.myapplication.R
import com.example.myapplication.UserViewModel
import com.example.myapplication.WeatherData
import com.example.myapplication.WeatherForecastService
import com.example.myapplication.WeatherResponse
import com.example.myapplication.WeatherService
import com.example.myapplication.databinding.FragmentWeatherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loggedInUsername: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.swipeRefreshLayout.setOnRefreshListener {
            userViewModel.getUserDetails(loggedInUsername).observe(viewLifecycleOwner) { user ->
                user?.city?.let { city ->
                    fetchWeatherData(city)
                    fetch3DaysForecast(city)
                } ?: run {
                    Toast.makeText(requireContext(), "City not found for user", Toast.LENGTH_SHORT).show()
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }


        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        loggedInUsername = sharedPreferences.getString("loggedInUsername", "") ?: ""

        userViewModel.getUserDetails(loggedInUsername).observe(viewLifecycleOwner) { user ->
            user?.city?.let { city ->
                fetchWeatherData(city)
                fetch3DaysForecast(city)
            } ?: run {
                Toast.makeText(requireContext(), "City not found for user", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun fetchWeatherData(city: String) {
        val apiKey = "5daf6b85c08147438a0ecb71663ed5ab"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherbit.io/v2.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        service.getCurrentWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val weatherData = response.body()?.data?.first()
                    weatherData?.let { updateUI(it) }
                } else {
                    Toast.makeText(context, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetch3DaysForecast(city: String) {
        val apiKey = "5daf6b85c08147438a0ecb71663ed5ab"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherbit.io/v2.0/forecast/daily/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherForecastService::class.java)
        service.get3DayForecast(city, apiKey, 3).enqueue(object : Callback<ForecastWeatherResponse> {
            override fun onResponse(
                call: Call<ForecastWeatherResponse>,
                response: Response<ForecastWeatherResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val forecastData = response.body()?.data ?: return
                    if (forecastData.size >= 3) {
                        updateForecastUI(forecastData)
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch weather forecast", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForecastWeatherResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun updateUI(weather: WeatherData) {
        binding.cityName.text = weather.city_name
        binding.humidityDescription.text = weather.rh + "%"
        binding.windSpeedDescription.text = weather.wind_spd + "km/h"
        binding.visibilityDescription.text = "${weather.vis} KM"
        binding.temperature.text = "${weather.temp}°C"
        binding.weatherDescription.text = weather.weather.description
        val iconUrl = "https://www.weatherbit.io/static/img/icons/${weather.weather.icon}.png"
        Glide.with(this).load(iconUrl).into(binding.weatherIcon)
    }

    private fun updateForecastUI(forecastData: List<ForecastDayData>) {
        binding.forecastContainer.visibility = View.VISIBLE

        val day1 = forecastData[0]
        binding.day1Date.text = day1.valid_date
        binding.day1Temp.text = "${day1.temp}°C"
        Glide.with(this).load("https://www.weatherbit.io/static/img/icons/${day1.weather.icon}.png").into(binding.day1Icon)

        val day2 = forecastData[1]
        binding.day2Date.text = day2.valid_date
        binding.day2Temp.text = "${day2.temp}°C"
        Glide.with(this).load("https://www.weatherbit.io/static/img/icons/${day2.weather.icon}.png").into(binding.day2Icon)

        val day3 = forecastData[2]
        binding.day3Date.text = day3.valid_date
        binding.day3Temp.text = "${day3.temp}°C"
        Glide.with(this).load("https://www.weatherbit.io/static/img/icons/${day3.weather.icon}.png").into(binding.day3Icon)
    }
}
