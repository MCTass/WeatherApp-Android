package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.weatherapp.model.ActivitySuggestion
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.model.WeatherViewModel

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf(TextFieldValue("")) }
    val weatherResult by viewModel.weatherResult.observeAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    val favoriteCities = viewModel.getFavoriteCities()
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    showDropdown = false
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        showDropdown = focusState.isFocused && city.text.isEmpty()
                    },
                value = city,
                onValueChange = {
                    city = it
                    showDropdown = it.text.isEmpty()
                },
                label = { Text(text = "Search for any Location") }
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    viewModel.getData(city.text)
                    focusRequester.freeFocus()
                }) {
                    Text(text = "Search")
                }
            }

            if (showDropdown && favoriteCities.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    items(favoriteCities.toList()) { favoriteCity ->
                        Text(
                            text = favoriteCity,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable {
                                    city = TextFieldValue(favoriteCity)
                                    showDropdown = false
                                    viewModel.getData(favoriteCity)
                                    focusRequester.freeFocus()
                                }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    when (val result = weatherResult) {
                        is NetworkResponse.Error -> {
                            Text(text = result.message)
                        }
                        NetworkResponse.Loading -> {
                            CircularProgressIndicator()
                        }
                        is NetworkResponse.Success -> {
                            WeatherDetails(data = result.data, viewModel = viewModel)
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { showDialog = true }) {
                                    Text(text = "Activity Suggestion")
                                }
                            }
                            if (showDialog) {
                                ActivitySuggestionsDialog(
                                    suggestions = result.data.activitySuggestions,
                                    onDismiss = { showDialog = false }
                                )
                            }
                        }
                        null -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySuggestionsDialog(suggestions: List<ActivitySuggestion>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.shadow(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Activity Suggestions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = suggestion.activity, fontSize = 10.sp, modifier = Modifier.weight(1f))
                        val likelihoodValue = suggestion.likelihood.removeSuffix("% likelihood").toFloat() / 100
                        val indicatorColor = if (likelihoodValue > 0.75) Color.Green else Color(0xFFFFA500) // Orange color
                        LinearProgressIndicator(
                            progress = { likelihoodValue },
                            modifier = Modifier
                                .weight(2f)
                                .padding(horizontal = 8.dp),
                            color = indicatorColor,
                        )
                        Text(text = suggestion.likelihood, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                }
                Button(onClick = onDismiss) {
                    Text(text = "Close")
                }
            }
        }
    }
}


@Composable
fun WeatherDetails(data: WeatherModel, viewModel: WeatherViewModel) {
    var isFavorite by remember { mutableStateOf(viewModel.getFavoriteCities().contains(data.location.name)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location icon",
                modifier = Modifier.size(40.dp)
            )
            Text(text = data.location.name, fontSize = 30.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = data.location.country, fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                isFavorite = if (isFavorite) {
                    viewModel.removeFavoriteCity(data.location.name)
                    false
                } else {
                    viewModel.addFavoriteCity(data.location.name)
                    true
                }
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFDAA520) else Color.Gray // Dark yellow color for favorite
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${data.current.temp_c} ° c",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        AsyncImage(
            modifier = Modifier.size(160.dp), model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
            contentDescription = "condition icon"
        )
        Text(
            text = data.current.condition.text,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White // Set your desired background color here
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp // Optional elevation
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Weatherkeyval("Humidity", data.current.humidity)
                    Weatherkeyval("Wind Speed", data.current.wind_kph + "km/h")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Weatherkeyval("UV", data.current.uv)
                    Weatherkeyval("Precipation", data.current.precip_mm + "mm")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Weatherkeyval("Local Time", data.location.localtime.split(" ")[1])
                    Weatherkeyval("Date", data.location.localtime.split(" ")[0])
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun Weatherkeyval(key:String,value: String){
    Column(
        modifier = Modifier
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}