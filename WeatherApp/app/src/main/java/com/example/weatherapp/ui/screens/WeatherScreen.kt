package com.example.weatherapp.ui.screens

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.ui.theme.Typography
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.util.WeatherIconMapper

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.loadWeatherForecast()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    WeatherBackground(
        modifier = modifier,
        weatherType = uiState.weatherType
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Text(
                text = "5 Day Forecast",
                style = Typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 30.dp, bottom = 18.dp)
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                thickness = 1.dp,
                color = Color.White
            )
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error occurred",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.forecasts) { forecast ->
                            WeatherCard(forecast = forecast)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    weatherType: WeatherType,
    content: @Composable () -> Unit
) {
    val backgroundResId = when (weatherType) {
        WeatherType.SUNNY -> R.drawable.sunny
        WeatherType.CLOUDY -> R.drawable.cloudy
        WeatherType.RAINY -> R.drawable.rainy
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        content()
    }
}

@Composable
fun WeatherCard(forecast: WeatherForecast) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = forecast.dayName,
                    style = Typography.titleMedium,
                    color = Color.Black
                )
                Image(
                    painter = painterResource(
                        id = WeatherIconMapper.getWeatherIconResId(forecast.weatherType)                     ),
                    contentDescription = "${forecast.weatherType} icon",
                    modifier = Modifier.size(60.dp)
                )
            }
            Text(
                text = "${forecast.temperature}°",
                style = Typography.headlineMedium,
                color = Color.Black,
                textAlign = TextAlign.End
            )
        }
    }
}