package com.example.weatherapp.model

import com.example.weatherapp.api.Current

object ActivitySuggestionProvider {

    private fun calculateActivityLikelihood(current: Current, localtime: String): Map<Activity, Int> {
        val likelihood = mutableMapOf<Activity, Int>()
        val hour = localtime.split(" ")[1].split(":")[0].toInt()
        val month = localtime.split(" ")[0].split("-")[1].toInt()

        fun updateLikelihood(activity: Activity, score: Int) {
            likelihood[activity] = (likelihood[activity] ?: 0) + score
        }

        if (current.precip_mm.toDouble() > 7.0 || current.wind_kph.toDouble() > 15) {
            updateLikelihood(Activity.INDOOR_GYM, 90)
            updateLikelihood(Activity.YOGA, 80)
            updateLikelihood(Activity.BASKETBALL, 70)
        } else {
            if (current.temp_c.toDouble() in 15.0..25.0 && current.humidity.toDouble() < 70 && current.wind_kph.toDouble() < 10) {
                updateLikelihood(Activity.RUNNING, 90)
                updateLikelihood(Activity.FOOTBALL, 85)
                updateLikelihood(Activity.PICNIC, 70)
            }

            if (current.temp_c.toDouble() > 25.0 && current.humidity.toDouble() > 50 && current.uv.toDouble() < 6) {
                updateLikelihood(Activity.SWIMMING, 90)
                updateLikelihood(Activity.FISHING, 70)
            }

            if (current.wind_kph.toDouble() in 5.0..15.0 && current.uv.toDouble() < 8) {
                updateLikelihood(Activity.CYCLING, 80)
                updateLikelihood(Activity.GOLF, 75)
                updateLikelihood(Activity.HIKING, 70)
            }

            if (current.uv.toDouble() >= 8) {
                updateLikelihood(Activity.INDOOR_GYM, 80)
                updateLikelihood(Activity.YOGA, 75)
            }

            if (hour in 6..10 || hour in 17..20) {
                updateLikelihood(Activity.WALKING, 80)
            }

            if (month in 4..9 && current.temp_c.toDouble() in 20.0..30.0) {
                updateLikelihood(Activity.PICNIC, 85)
            }
        }

        return likelihood
    }

    fun suggestActivitiesWithLikelihood(current: Current, localtime: String): List<Pair<Activity, Int>> {
        val likelihood = calculateActivityLikelihood(current, localtime)
        return likelihood.entries
            .sortedByDescending { it.value }
            .map { it.toPair() }
    }

    fun getSuggestions(current: Current, localtime: String): List<ActivitySuggestion> {
        val activitiesWithLikelihood = suggestActivitiesWithLikelihood(current, localtime)
        return activitiesWithLikelihood.map { (activity, likelihood) ->
            ActivitySuggestion(activity.name, "$likelihood% likelihood")
        }
    }
}
