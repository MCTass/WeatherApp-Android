package com.example.weatherapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.Constant
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.RetrofitInstance
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences

class WeatherViewModel(context: Context) : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> get() = _weatherResult

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    fun getData(city: String) {
        _weatherResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(Constant.apiKey, city)
                if (response.isSuccessful) {
                    response.body()?.let { weatherModel ->
                        val suggestions = ActivitySuggestionProvider.getSuggestions(weatherModel.current, weatherModel.location.localtime)
                        Log.d("WeatherViewModel", "Activity Suggestions: $suggestions")
                        val updatedWeatherModel = weatherModel.copy(activitySuggestions = suggestions)
                        _weatherResult.value = NetworkResponse.Success(updatedWeatherModel)
                    }
                } else {
                    _weatherResult.value = NetworkResponse.Error("Failed to load Data")
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Failed to load Data")
            }
        }
    }

    fun addFavoriteCity(city: String) {
        val favorites = getFavoriteCities().toMutableSet()
        favorites.add(city)
        sharedPreferences.edit().putStringSet("favorite_cities", favorites).apply()
    }

    fun removeFavoriteCity(city: String) {
        val favorites = getFavoriteCities().toMutableSet()
        favorites.remove(city)
        sharedPreferences.edit().putStringSet("favorite_cities", favorites).apply()
    }

    fun getFavoriteCities(): Set<String> {
        return sharedPreferences.getStringSet("favorite_cities", emptySet()) ?: emptySet()
    }
}