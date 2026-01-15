package com.example.weatherapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.weatherapp.R
import com.example.weatherapp.data.model.WeatherForecast
import com.example.weatherapp.data.model.WeatherType
import com.example.weatherapp.data.model.WeatherUiState
import com.example.weatherapp.ui.theme.Typography
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.util.WeatherIconMapper


@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherScreen(
        modifier = modifier,
        uiState = uiState,
        onLoadWeather = { viewModel.loadWeatherForecast() }
    )
}


@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    uiState: WeatherUiState,
    onLoadWeather: () -> Unit
) {
    RequestLocationPermission(
        onGranted = onLoadWeather
    )

    when (uiState) {
        is WeatherUiState.Loading -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = WeatherType.SUNNY,
                isLoading = true,
                error = null,
                forecasts = emptyList()
            )
        }
        is WeatherUiState.Success -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = uiState.weatherType,
                isLoading = false,
                error = null,
                forecasts = uiState.forecasts
            )
        }
        is WeatherUiState.Error -> {
            WeatherScreenContent(
                modifier = modifier,
                weatherType = WeatherType.SUNNY,
                isLoading = false,
                error = uiState.message,
                forecasts = emptyList()
            )
        }
    }
}

@Composable
private fun RequestLocationPermission(
    onGranted: () -> Unit
) {
    val context = LocalContext.current

    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            onGranted()
        }
    }

    val isPermissionGranted = run {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        fineGranted || coarseGranted
    }

    LaunchedEffect(isPermissionGranted, hasRequestedPermission) {
        when {
            isPermissionGranted -> {
                onGranted()
            }
            !isPermissionGranted && !hasRequestedPermission -> {
                hasRequestedPermission = true
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

@Composable
private fun WeatherScreenContent(
    modifier: Modifier = Modifier,
    weatherType: WeatherType,
    isLoading: Boolean,
    error: String?,
    forecasts: List<WeatherForecast>
) {
    WeatherBackground(
        modifier = modifier,
        weatherType = weatherType
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            ForecastHeader()

            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(message = error)
                else -> ForecastList(forecasts = forecasts)
            }
        }
    }
}

@Composable
private fun ForecastHeader() {
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
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.ifBlank { "Error occurred" },
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ForecastList(forecasts: List<WeatherForecast>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(forecasts) { forecast ->
            WeatherCard(forecast = forecast)
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
                        id = WeatherIconMapper.getWeatherIconResId(forecast.weatherType)
                    ),
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