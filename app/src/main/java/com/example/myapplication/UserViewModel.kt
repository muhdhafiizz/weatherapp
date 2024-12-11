package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao: UserDao = AppDatabase.getDatabase(application).userDao()

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> get() = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.postValue(userDao.login(username, password))
        }
    }

    fun signup(username: String, password: String, age: Int, postcode: Int, city: String) {
        viewModelScope.launch {
            userDao.insert(User(username = username, password = password, age = age, postcode = postcode, city = city))
        }
    }

    fun getUserDetails(username: String): LiveData<User> {
        val userDetails = MutableLiveData<User>()
        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            userDetails.postValue(user)
        }
        return userDetails
    }

    fun updateUserDetails(username: String, password: String, age: Int, postcode: Int, city: String) {
        viewModelScope.launch {
            // Call the UserDao method with individual parameters
            userDao.updateUser(username, password, age, postcode, city)
        }
    }

    private val cityLiveData = MutableLiveData<String?>()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.zippopotam.us/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ZippopotamusApiService = retrofit.create(ZippopotamusApiService::class.java)

    fun getCityByPostcode(postcode: String): LiveData<String?> {
        viewModelScope.launch {
            try {
                val response = apiService.getCityByPostcode("MY", postcode)
                val cityName = response.places.firstOrNull()?.placeName
                cityLiveData.postValue(cityName)
            } catch (e: Exception) {
                cityLiveData.postValue(null)
            }
        }
        return cityLiveData
    }



}


