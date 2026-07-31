package com.example.weatherapp.model

import com.example.weatherapp.api.Current
import com.example.weatherapp.api.Location

data class WeatherModel(
    val current: Current,
    val location: Location,
    val activitySuggestions: List<ActivitySuggestion>
)